package com.machiav3lli.backup.pages

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.preferences.extendedInfo
import com.machiav3lli.backup.preferences.textLogShare
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.LockOpen
import com.machiav3lli.backup.ui.compose.icons.phosphor.ShareNetwork
import com.machiav3lli.backup.ui.compose.icons.phosphor.Warning
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.utils.SystemUtils
import kotlin.system.exitProcess

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SplashPage() {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.6f))
            Image(
                modifier = Modifier
                    .fillMaxSize(0.5f),
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(id = R.string.app_name)
            )
            Spacer(modifier = Modifier.weight(0.4f))
            Text(
                text = listOf(
                    BuildConfig.APPLICATION_ID,
                    BuildConfig.VERSION_NAME,
                    SystemUtils.applicationIssuer?.let { "signed by $it" } ?: "",
                ).joinToString("\n"),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RootMissing(activity: Activity? = null) {
    Scaffold {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(50.dp)
        ) {
            Text(
                text = stringResource(R.string.root_missing),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = stringResource(R.string.root_is_mandatory),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = stringResource(R.string.see_faq),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(80.dp))
            ElevatedActionButton(
                text = "try to share a support log",
                icon = Phosphor.ShareNetwork,
                fullWidth = true,
                modifier = Modifier
            ) {
                textLogShare(extendedInfo(), temporary = true)
            }
            Spacer(modifier = Modifier.height(80.dp))
            ElevatedActionButton(
                text = stringResource(id = R.string.dialogOK),
                icon = Phosphor.Warning,
                fullWidth = true,
                modifier = Modifier
            ) {
                activity?.finishAffinity()
                exitProcess(0)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LockPage(launchMain: () -> Unit) {
    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.Center
            ) {
                ElevatedActionButton(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = stringResource(id = R.string.dialog_unlock),
                    icon = Phosphor.LockOpen,
                ) {
                    launchMain()
                }
            }
        }
    ) {
        BackHandler {
            OABX.main?.finishAffinity()
        }
        Box(modifier = Modifier.fillMaxSize()) {}
    }
}

@Preview
@Composable
private fun SplashPreview() {
    OABX.fakeContext = LocalContext.current.applicationContext
    SplashPage()
    OABX.fakeContext = null
}

@Preview
@Composable
private fun NoRootPreview() {
    OABX.fakeContext = LocalContext.current.applicationContext
    RootMissing()
    OABX.fakeContext = null
}
