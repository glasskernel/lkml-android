package app.k9mail.core.ui.compose.designsystem.organism

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import app.k9mail.core.ui.compose.designsystem.atom.button.ButtonText
import app.k9mail.core.ui.compose.designsystem.atom.text.TextBodyMedium
import app.k9mail.core.ui.compose.designsystem.atom.text.TextHeadlineSmall
import app.k9mail.core.ui.compose.designsystem.template.LiquidGlassDefaults
import app.k9mail.core.ui.compose.designsystem.template.liquidGlass
import net.thunderbird.core.ui.compose.designsystem.atom.icon.Icon
import net.thunderbird.core.ui.compose.theme2.MainTheme
import androidx.compose.material3.AlertDialog as MaterialAlertDialog

@Composable
fun AlertDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    dismissText: String? = null,
    onDismissClick: () -> Unit = {},
    confirmButtonEnabled: Boolean = true,
) {
    AlertDialog(
        title = title,
        icon = icon,
        confirmText = confirmText,
        onConfirmClick = onConfirmClick,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        dismissText = dismissText,
        onDismissClick = onDismissClick,
        confirmButtonEnabled = confirmButtonEnabled,
    ) {
        TextBodyMedium(text = text)
    }
}

@Composable
fun AlertDialog(
    title: String,
    confirmText: String,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    dismissText: String? = null,
    onDismissClick: () -> Unit = {},
    confirmButtonEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dialogShape = MainTheme.shapes.extraLarge

    MaterialAlertDialog(
        title = {
            TextHeadlineSmall(
                text = title,
                textAlign = if (icon == null) TextAlign.Start else TextAlign.Center,
            )
        },
        icon = icon?.let {
            {
                Icon(imageVector = it)
            }
        },
        text = { content() },
        confirmButton = {
            ButtonText(
                text = confirmText,
                onClick = onConfirmClick,
                enabled = confirmButtonEnabled,
            )
        },
        dismissButton = dismissText?.let {
            {
                ButtonText(
                    text = it,
                    onClick = onDismissClick,
                )
            }
        },
        onDismissRequest = onDismissRequest,
        modifier = modifier.liquidGlass(dialogShape),
        shape = dialogShape,
        containerColor = LiquidGlassDefaults.emphasizedColor(MainTheme.colors.surfaceContainerHigh),
        iconContentColor = MainTheme.colors.onSurfaceVariant,
        titleContentColor = MainTheme.colors.onSurface,
        textContentColor = MainTheme.colors.onSurfaceVariant,
    )
}
