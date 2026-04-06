package net.thunderbird.core.ui.compose.designsystem.organism

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.k9mail.core.ui.compose.designsystem.template.LiquidGlassDefaults
import app.k9mail.core.ui.compose.designsystem.template.liquidGlass
import net.thunderbird.core.ui.compose.theme2.MainTheme
import androidx.compose.material3.ModalBottomSheet as MaterialModalBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val sheetShape = LiquidGlassDefaults.sheetShape

    MaterialModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier.liquidGlass(sheetShape),
        shape = sheetShape,
        containerColor = LiquidGlassDefaults.emphasizedColor(MainTheme.colors.surfaceContainerHigh),
        content = content,
    )
}
