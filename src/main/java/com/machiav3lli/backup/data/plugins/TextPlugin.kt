package com.machiav3lli.backup.data.plugins

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.machiav3lli.backup.data.preferences.tracePlugin
import java.io.File

@Composable
fun TextEditor(
    text: String = "",
    modifier: Modifier = Modifier,
    placeholder: String = "",
    onChanged: (String) -> Unit = {},
) {
    var input by remember {
        mutableStateOf(
            TextFieldValue(
                text = text,
                selection = TextRange(text.length)
            )
        )
    }
    val textFieldFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        textFieldFocusRequester.requestFocus()
    }

    OutlinedTextField(
        modifier = modifier
            .testTag("input")
            .focusRequester(textFieldFocusRequester),
        textStyle = MaterialTheme.typography.bodySmall,
        value = input,
        placeholder = { Text(text = placeholder, color = Color.Gray) },
        singleLine = false,
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false
        ),
        onValueChange = {
            input = it
            onChanged(it.text)
        }
    )
}

open class TextPlugin(file: File) : Plugin(name = file.nameWithoutExtension, file = file) {

    var text: String = ""

    init {
        try {
            text = file.readText()
            //tracePlugin { "${this.javaClass.simpleName} $name loaded <- ${file.name}" }
        } catch (e: Throwable) {
            text = ""
            tracePlugin { "${this.javaClass.simpleName} $name created -> ${file.name}" }
        }
    }

    override fun save() {
        try {
            file.parentFile?.mkdirs()
            file.writeText(text)
            tracePlugin { "${this.javaClass.simpleName} $name saved -> ${file.name}" }
        } catch (e: Throwable) {
            tracePlugin { "${this.javaClass.simpleName} $name failed to save -> ${file.name}" }
        }
    }

    override fun delete() {
        try {
            file.delete()
            tracePlugin { "${this.javaClass.simpleName} $name deleted -> ${file.name}" }
        } catch (e: Throwable) {
            tracePlugin { "${this.javaClass.simpleName} $name failed to delete -> ${file.name}" }
        }
    }

    @Composable
    override fun Editor() {
        TextEditor(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            onChanged = { text = it }
        )
    }

}
