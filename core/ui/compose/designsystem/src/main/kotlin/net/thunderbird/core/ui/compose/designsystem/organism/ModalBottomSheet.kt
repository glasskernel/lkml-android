package net.thunderbird.core.ui.compose.designsystem.organism

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.k9mail.core.ui.compose.designsystem.template.LocalHazeState
import dev.chrisbanes.haze.hazeChild
import androidx.compose.material3.ModalBottomSheet as MaterialModalBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    MaterialModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier.hazeChild(LocalHazeState.current),
        containerColor = Color.Transparent,
        content = content,
    )
}
