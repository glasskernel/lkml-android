package app.k9mail.core.ui.compose.designsystem.atom.card

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import app.k9mail.core.ui.compose.designsystem.template.liquidGlass
import androidx.compose.material3.ElevatedCard as Material3ElevatedCard

@Composable
fun CardElevated(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.elevatedShape,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    elevation: CardElevation = CardDefaults.elevatedCardElevation(),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val glassModifier = modifier.liquidGlass(shape)

    if (onClick != null) {
        Material3ElevatedCard(
            onClick = onClick,
            modifier = glassModifier,
            shape = shape,
            colors = colors.toMaterial3CardColors(),
            elevation = elevation.toMaterial3CardElevation(),
            content = content,
        )
    } else {
        Material3ElevatedCard(
            modifier = glassModifier,
            shape = shape,
            colors = colors.toMaterial3CardColors(),
            elevation = elevation.toMaterial3CardElevation(),
            content = content,
        )
    }
}
