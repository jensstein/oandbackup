package com.machiav3lli.backup.manager.handler

import android.content.Context
import android.net.Uri
import com.machiav3lli.backup.ui.pages.pref_pgpKey
import com.machiav3lli.backup.ui.pages.pref_pgpPasscode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import org.bouncycastle.jcajce.BCFKSLoadStoreParameter
import org.bouncycastle.openpgp.PGPPublicKeyRing
import org.bouncycastle.openpgp.PGPSecretKeyRing
import org.pgpainless.PGPainless
import org.pgpainless.algorithm.SymmetricKeyAlgorithm
import org.pgpainless.decryption_verification.ConsumerOptions
import org.pgpainless.decryption_verification.DecryptionStream
import org.pgpainless.encryption_signing.EncryptionOptions
import org.pgpainless.encryption_signing.EncryptionResult
import org.pgpainless.encryption_signing.EncryptionStream
import org.pgpainless.encryption_signing.ProducerOptions
import org.pgpainless.key.protection.SecretKeyRingProtector.Companion.unlockAnyKeyWith
import org.pgpainless.key.protection.SecretKeyRingProtector.Companion.unprotectedKeys
import org.pgpainless.util.Passphrase
import java.io.InputStream
import java.io.OutputStream

// https://pgpainless.readthedocs.io
// TODO migrate to 2.0.X when ready https://pgpainless.readthedocs.io/en/latest/pgpainless-core/migration_2.0.html
class PGPHandler(private val context: Context) {
    val cc = Dispatchers.IO + SupervisorJob()

    private var secretKeyRing: PGPSecretKeyRing = runCatching {
        PGPainless.readKeyRing().secretKeyRing(pref_pgpKey.value)
            ?: PGPSecretKeyRing(emptyList())
    }.getOrDefault(PGPSecretKeyRing(emptyList()))
    private var publicKeyRing: PGPPublicKeyRing = PGPainless.extractCertificate(secretKeyRing)

    fun isKeyLoaded() = runCatching { secretKeyRing.size() > 0 }
        .getOrElse { false }

    private fun reloadPublicKey() {
        publicKeyRing = PGPainless.extractCertificate(secretKeyRing)
    }

    fun reloadKeys() = runCatching {
        secretKeyRing = PGPainless.readKeyRing().secretKeyRing(pref_pgpKey.value)
            ?: PGPSecretKeyRing(emptyList())
        reloadPublicKey()
        BCFKSLoadStoreParameter.EncryptionAlgorithm.AES256_KWP
        secretKeyRing.secretKey?.keyEncryptionAlgorithm
    }

    suspend fun loadKeyFromUri(uri: Uri): Result<Boolean> = runCatching {
        withContext(cc) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val byteArray = inputStream.readBytes()
                pref_pgpKey.value = byteArray.decodeToString()
                reloadKeys()
                true
            } == true
        }
    }

    suspend fun generateNewKey(
        userId: String,
        passcode: String,
        saveLocation: Uri
    ): Boolean {
        return withContext(cc) {
            runCatching {
                val keyRing = PGPainless.generateKeyRing()
                    .modernKeyRing(userId, passcode)
                val asciiKey = PGPainless.asciiArmor(keyRing)

                context.contentResolver.openOutputStream(saveLocation)?.use { output ->
                    output.write(asciiKey.toByteArray())
                }
                secretKeyRing = keyRing
                reloadPublicKey()
                pref_pgpKey.value = asciiKey
                pref_pgpPasscode.value = passcode
                true
            }.getOrDefault(false)
        }
    }

    suspend fun encryptFile(inputUri: Uri, outputUri: Uri): Result<EncryptionResult?> =
        runCatching {
            withContext(cc) {
                context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                    context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                        val encryptedOut = PGPainless.encryptAndOrSign()
                            .onOutputStream(outputStream)
                            .withOptions(
                                ProducerOptions.encrypt(
                                    EncryptionOptions.encryptDataAtRest()
                                        .addHiddenRecipient(publicKeyRing)
                                        .overrideEncryptionAlgorithm(SymmetricKeyAlgorithm.AES_256)
                                )
                            )
                        inputStream.copyTo(encryptedOut)
                        encryptedOut.close()
                        encryptedOut.result
                    }
                }
            }
        }

    fun encryptStream(outputStream: OutputStream): Result<EncryptionStream?> =
        runCatching {
            PGPainless.encryptAndOrSign()
                .onOutputStream(outputStream)
                .withOptions(
                    ProducerOptions.encrypt(
                        EncryptionOptions.encryptDataAtRest()
                            .addHiddenRecipient(publicKeyRing)
                            .overrideEncryptionAlgorithm(SymmetricKeyAlgorithm.AES_256)
                    )
                )
        }

    suspend fun decryptFile(inputUri: Uri, outputUri: Uri): Result<Unit> = runCatching {
        withContext(cc) {
            val keyProtector = pref_pgpPasscode.value.takeIf { it.isNotBlank() }
                ?.let { unlockAnyKeyWith(Passphrase(it.toCharArray())) }
                ?: unprotectedKeys()

            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                    PGPainless.decryptAndOrVerify()
                        .onInputStream(inputStream)
                        .withOptions(
                            ConsumerOptions()
                                .addDecryptionKey(secretKeyRing, keyProtector)
                        ).use {
                            it.copyTo(outputStream)
                        }
                }
            }
        }
    }

    fun decryptStream(inputStream: InputStream): Result<DecryptionStream> =
        runCatching {
            val keyProtector = pref_pgpPasscode.value.takeIf { it.isNotBlank() }
                ?.let { unlockAnyKeyWith(Passphrase(it.toCharArray())) }
                ?: unprotectedKeys()

            PGPainless.decryptAndOrVerify()
                .onInputStream(inputStream)
                .withOptions(
                    ConsumerOptions()
                        .addDecryptionKey(secretKeyRing, keyProtector)
                )
        }
}