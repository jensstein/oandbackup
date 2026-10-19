package tests.research

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.tooling.preview.Preview
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Try_composeTest {

    @get:Rule
    val test = createComposeRule()

    @Test
    fun testComposableDisplaysCorrectText() {
        // Arrange
        val testName = "TestUser"

        // Act
        test.setContent {
            TestComposable(name = testName)
        }

        // Assert
        test.onNodeWithText("Hello $testName!")
            .assertIsDisplayed()
    }
}

@Preview(showBackground = true)
@Composable
fun TestComposablePreview() {
    TestComposable("Preview")
}

@Composable
fun TestComposable(name: String, modifier: Modifier = Modifier) {

    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
