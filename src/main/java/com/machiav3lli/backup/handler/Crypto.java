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
import javax.crypto.spec.SecretKeySpec;

public final class Crypto {
    public static final String TAG = Constants.classTag(".Crypto");

    /**
     * Taken from here. Chosen because of SDK 24+ compatibility
     * https://developer.android.com/guide/topics/security/cryptography#SupportedSecretKeyFactory
     */
    public static final String DEFAULT_SECRET_KEY_FACTORY_ALGORITHM = "HmacSHA256";
    public static final String DEFAULT_CIPHER_ALGORITHM = "AES_128/CBC/NoPadding";
    public static final int DEFAULT_IV_BLOCK_SIZE = 16;

    public static SecretKey generateKeyFromPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return Crypto.generateKeyFromPassword(password, Crypto.DEFAULT_SECRET_KEY_FACTORY_ALGORITHM, Crypto.DEFAULT_CIPHER_ALGORITHM);
    }

    public static SecretKey generateKeyFromPassword(String password, String keyFactoryAlgorithm, String cipherAlgorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(keyFactoryAlgorithm);
        KeySpec spec = new javax.crypto.spec.PBEKeySpec(password.toCharArray());
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), cipherAlgorithm.split("/")[0]);
        return secret;
    }

    public static CipherOutputStream encryptStream(OutputStream os, String password) throws CryptoSetupException {
        try {
            SecretKey secret = Crypto.generateKeyFromPassword(password);
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
