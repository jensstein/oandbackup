package tests.research

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.component.BalancedWrapRow
import com.machiav3lli.backup.ui.compose.component.balancedWrap
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.Star
import org.junit.Before
import org.junit.Rule
import org.junit.Test


val rowMod
    get() = Modifier
        .padding(1.dp)
        .border(0.5.dp, Color.Blue)
        .padding(2.dp)
        .fillMaxWidth()

val textMod
    get() = Modifier
        .padding(1.dp)
        .border(0.5.dp, Color.Red)
        .padding(2.dp)

val textLeft = "a much much much much longer text"
val textRight = "short text"

@Composable
fun TwoTexts(
    textLeft: String, modifierLeft: Modifier,
    textRight: String, modifierRight: Modifier,
    prefix: @Composable () -> Unit = {},
    infix: @Composable () -> Unit = {},
    postfix: @Composable () -> Unit = {},
) {
    Row(modifier = rowMod) {
        prefix()
        Text(textLeft, maxLines = 10, modifier = modifierLeft)
        infix()
        Text(textRight, maxLines = 10, modifier = modifierRight)
        postfix()
    }
}

@Composable
fun TestBalancedWrapText(text: String) {
    Text(text = text, modifier = textMod.balancedWrap())
}

@Composable
fun TheComposable() {

    val icon = @Composable {
        Icon(
            Phosphor.Star,
            contentDescription = null,
            modifier = Modifier
                .size(10.dp)
                .padding(0.dp)
        )
    }

    Column(
        modifier = Modifier
            .width(230.dp)
            .height(600.dp)
    ) {
        TwoTexts(
            "short", textMod,
            "also short", textMod,
            prefix = {
                icon()
                icon()
            },
            infix = {
                icon()
            },
            postfix = {
                icon()
                icon()
            }
        )
        TwoTexts(
            "short enough",
            textMod.weight(0.5f),
            "also short",
            textMod.weight(0.5f),
            prefix = {
                icon()
                icon()
            },
            infix = {
                icon()
            },
            postfix = {
                icon()
                icon()
            }
        )
        TwoTexts(
            textLeft,
            textMod.weight(0.5f),
            textRight,
            textMod.weight(0.5f),
            prefix = {
                icon()
                icon()
            },
            infix = {
                icon()
            },
            postfix = {
                icon()
                icon()
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        BalancedWrapRow(
            modifier = rowMod,
        ) {
            //Text("fix")
            icon()
            icon()
            //Text("fix")
            TestBalancedWrapText(text = "short")
            icon()
            //Text("fix")
            TestBalancedWrapText(text = "also short")
            //Text("fix")
            icon()
            icon()
        }
        BalancedWrapRow(
            modifier = rowMod,
        ) {
            //Text("fix")
            icon()
            icon()
            //Text("fix")
            TestBalancedWrapText(text = "short enough")
            icon()
            //Text("fix")
            TestBalancedWrapText(text = "also short")
            //Text("fix")
            icon()
            icon()
        }
        BalancedWrapRow(
            modifier = rowMod,
        ) {
            icon()
            icon()
            //Text("fix")
            TestBalancedWrapText(text = textLeft)
            //Text("fix")
            icon()
            TestBalancedWrapText(text = textRight)
            //Text("fix")
            icon()
            icon()
        }
        BalancedWrapRow(
            modifier = rowMod,
        ) {
            //Text("fix")
            icon()
            icon()
            //Text("fix")
            TestBalancedWrapText(text = "only three words")
            icon()
            //Text("fix")
            TestBalancedWrapText(text = "many more words for a thorough test")
            //Text("fix")
            icon()
            icon()
        }
        BalancedWrapRow(
            modifier = rowMod,
        ) {
            //Text("fix")
            icon()
            icon()
            //Text("fix")
            TestBalancedWrapText(text = "only three words")
            icon()
            //Text("fix")
            TestBalancedWrapText(text = "many more words for a thorough test and even more words")
            //Text("fix")
            icon()
            icon()
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    TheComposable()
}


class Try_BalancedWrapRow {

    @get:Rule
    val test = createComposeRule()

    @Before
    fun setUp() {

        test.setContent {
            TheComposable()
        }
        test.onRoot().printToLog("root")
    }

    @Test
    fun test_BalancedWrapRow() {

        test.waitForIdle()
        test.waitUntil(10000) { false }
    }
}
