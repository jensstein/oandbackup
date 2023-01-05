package com.machiav3lli.backup.utils

import android.content.Context
import android.content.pm.PackageManager
import com.machiav3lli.backup.BuildConfig
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate


object SystemUtils {

    fun Context.getApplicationIssuer() : String? {
        runCatching {
            val packageManager: PackageManager = getPackageManager()
            val packageName = BuildConfig.APPLICATION_ID

            val packageInfo =
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            val signatures = packageInfo.signatures
            val signature = signatures[0]
            val signatureBytes = signature.toByteArray()
            val cf = CertificateFactory.getInstance("X509")
            val x509Certificate: X509Certificate =
                cf.generateCertificate(ByteArrayInputStream(signatureBytes)) as X509Certificate
            var DN = x509Certificate.getIssuerDN().getName()
            val names = DN.split(",").map {
                val (field, value) = it.split("=", limit = 2)
                field to value
            }.toMap()
            var issuer = names["CN"]
            names["O"]?.let { if (issuer != it) issuer = "$issuer / $it"}
            return issuer ?: DN
        }
        return null
    }
}
