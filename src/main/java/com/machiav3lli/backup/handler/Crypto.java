package com.machiav3lli.backup.handler;

import android.util.Log;

import com.machiav3lli.backup.Constants;

import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Crypto. The class to handle encryption and decryption of streams.
 * Call `encryptStream` or `decryptStream` with a password and a salt or a better a secret key
 * (for performance reasons) and the class will wrap the given stream in return.
 * <p>
 * Android Keystore API is not used on purpose, because the key material needs to be portable for
 * uses cases when the device has been wiped or when backups are restored on another device.
 * <p>
 * The IV is static as it may be public.
 */
public final class Crypto {
    public static final String TAG = Constants.classTag(".Crypto");

    /**
     * Taken from here. Chosen because of API Level 24+ compatibility. Newer algorithms are available
     * with API Level 26+.
     * https://developer.android.com/guide/topics/security/cryptography#SupportedSecretKeyFactory
     * <p>
     * The actual choice was inspired by this blog post:
     * https://www.raywenderlich.com/778533-encryption-tutorial-for-android-getting-started
     */
    public static final String DEFAULT_SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA1";
    public static final String DEFAULT_CIPHER_ALGORITHM = "AES_128/CBC/PKCS5Padding";
    public static final int DEFAULT_IV_BLOCK_SIZE = 16;  // 128 bit
    public static final int ITERATION_COUNT = 1000;
    public static final int KEY_LENGTH = 128;

    public static SecretKey generateKeyFromPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return Crypto.generateKeyFromPassword(password, salt, Crypto.DEFAULT_SECRET_KEY_FACTORY_ALGORITHM, Crypto.DEFAULT_CIPHER_ALGORITHM);
    }

    public static SecretKey generateKeyFromPassword(String password, byte[] salt, String keyFactoryAlgorithm, String cipherAlgorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(keyFactoryAlgorithm);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, Crypto.ITERATION_COUNT, Crypto.KEY_LENGTH);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        SecretKey secret = new SecretKeySpec(keyBytes, cipherAlgorithm.split("/")[0]);
        return secret;
    }

    public static CipherOutputStream encryptStream(OutputStream os, String password, byte[] salt) throws CryptoSetupException {
        try {
            SecretKey secret = Crypto.generateKeyFromPassword(password, salt);
            return Crypto.encryptStream(os, secret);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            Log.e(Crypto.TAG, "Could not setup encryption: " + e.getMessage());
            throw new CryptoSetupException("Could not setup encryption", e);
        }
    }

    public static CipherOutputStream encryptStream(OutputStream os, SecretKey secret) throws CryptoSetupException {
        return Crypto.encryptStream(os, secret, Crypto.DEFAULT_CIPHER_ALGORITHM);
    }

    public static CipherOutputStream encryptStream(OutputStream os, SecretKey secret, String cipherAlgorithm) throws CryptoSetupException {
        try {
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            final IvParameterSpec iv = new IvParameterSpec(Crypto.initIv(cipherAlgorithm));
            cipher.init(Cipher.ENCRYPT_MODE, secret, iv);
            return new CipherOutputStream(os, cipher);
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
            Log.e(Crypto.TAG, "Could not setup encryption: " + e.getMessage());
            throw new CryptoSetupException("Could not setup encryption", e);
        }
    }

    private static byte[] initIv(String cipherAlgorithm) {
        int blockSize;
        try {
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            blockSize = cipher.getBlockSize();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            // Fallback if the cipher has issues. Might lead to another exception later, but saves
            // the situation here. The use cipher might not match or will cause other exceptions
            // when used like this.
            blockSize = Crypto.DEFAULT_IV_BLOCK_SIZE;
        }
        // IV is nothing secret. Could also be constant, but why not spend a few cpu cycles to have
        // it dynamic, if the algorithm changes?
        byte[] iv = new byte[blockSize];
        for (int i = 0; i < blockSize; ++i) {
            iv[i] = 0;
        }
        return iv;
    }

    public static class CryptoSetupException extends Exception {
        public CryptoSetupException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
