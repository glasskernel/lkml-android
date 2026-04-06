package app.k9mail.core.ui.compose.designsystem.atom.textfield

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import app.k9mail.core.ui.compose.designsystem.template.LiquidGlassDefaults
import app.k9mail.core.ui.compose.designsystem.template.liquidGlass
import net.thunderbird.core.ui.compose.theme2.MainTheme
import androidx.compose.material3.OutlinedTextField as Material3OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults

@Suppress("LongParameterList")
@Composable
fun TextFieldOutlined(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isEnabled: Boolean = true,
    isReadOnly: Boolean = false,
    isRequired: Boolean = false,
    hasError: Boolean = false,
    isSingleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Material3OutlinedTextField(
        value = value,
        onValueChange = if (isSingleLine) stripLineBreaks(onValueChange) else onValueChange,
        modifier = modifier.liquidGlass(MainTheme.shapes.large),
        enabled = isEnabled,
        label = selectLabel(label, isRequired),
        trailingIcon = trailingIcon,
        readOnly = isReadOnly,
        isError = hasError,
        singleLine = isSingleLine,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LiquidGlassDefaults.glassColor(MainTheme.colors.surfaceContainerHigh),
            unfocusedContainerColor = LiquidGlassDefaults.subtleColor(MainTheme.colors.surfaceContainerLow),
            disabledContainerColor = LiquidGlassDefaults.subtleColor(MainTheme.colors.surfaceContainerLowest),
            errorContainerColor = LiquidGlassDefaults.glassColor(MainTheme.colors.errorContainer),
        ),
    )
}

/**
 * Overload of [TextFieldOutlined] that accepts a [TextFieldValue] instead of a [String].
 */
@Suppress("LongParameterList")
@Composable
fun TextFieldOutlined(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isEnabled: Boolean = true,
    isReadOnly: Boolean = false,
    isRequired: Boolean = false,
    hasError: Boolean = false,
    isSingleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Material3OutlinedTextField(
        value = value,
        onValueChange = if (isSingleLine) stripTextFieldValueLineBreaks(onValueChange) else onValueChange,
        modifier = modifier.liquidGlass(MainTheme.shapes.large),
        enabled = isEnabled,
        label = selectLabel(label, isRequired),
        trailingIcon = trailingIcon,
        readOnly = isReadOnly,
        isError = hasError,
        singleLine = isSingleLine,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LiquidGlassDefaults.glassColor(MainTheme.colors.surfaceContainerHigh),
            unfocusedContainerColor = LiquidGlassDefaults.subtleColor(MainTheme.colors.surfaceContainerLow),
            disabledContainerColor = LiquidGlassDefaults.subtleColor(MainTheme.colors.surfaceContainerLowest),
            errorContainerColor = LiquidGlassDefaults.glassColor(MainTheme.colors.errorContainer),
        ),
    )
}
