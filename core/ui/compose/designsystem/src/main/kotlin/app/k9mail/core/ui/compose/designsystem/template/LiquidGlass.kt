package app.k9mail.core.ui.compose.designsystem.template

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeChild
import net.thunderbird.core.ui.compose.theme2.MainTheme

private const val GLASS_ALPHA_SUBTLE = 0.56f
private const val GLASS_ALPHA_DEFAULT = 0.72f
private const val GLASS_ALPHA_EMPHASIZED = 0.84f

internal object LiquidGlassDefaults {
    val appBarShape: Shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
    val drawerShape: Shape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp)
    val sheetShape: Shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    val itemShape: Shape = RoundedCornerShape(20.dp)

    fun subtleColor(baseColor: Color): Color = baseColor.copy(alpha = GLASS_ALPHA_SUBTLE)

    fun glassColor(baseColor: Color): Color = baseColor.copy(alpha = GLASS_ALPHA_DEFAULT)

    fun emphasizedColor(baseColor: Color): Color = baseColor.copy(alpha = GLASS_ALPHA_EMPHASIZED)
}

@Composable
internal fun Modifier.liquidGlass(shape: Shape): Modifier {
    val hazeState = LocalHazeState.current
    return clip(shape).hazeChild(hazeState)
}

@Composable
internal fun Modifier.liquidGlassBackdrop(): Modifier {
    val colors = MainTheme.colors

    return drawWithCache {
        val linearBrush = Brush.linearGradient(
            colors = listOf(
                colors.surfaceBright,
                colors.surface,
                colors.surfaceDim,
            ),
            start = Offset.Zero,
            end = Offset(x = size.width, y = size.height),
        )
        val topGlow = Brush.radialGradient(
            colors = listOf(
                colors.primary.copy(alpha = 0.14f),
                Color.Transparent,
            ),
            center = Offset(x = size.width * 0.88f, y = size.height * 0.08f),
            radius = size.minDimension * 0.7f,
        )
        val bottomGlow = Brush.radialGradient(
            colors = listOf(
                colors.tertiary.copy(alpha = 0.10f),
                Color.Transparent,
            ),
            center = Offset(x = size.width * 0.12f, y = size.height * 0.94f),
            radius = size.minDimension * 0.85f,
        )

        onDrawBehind {
            drawRect(brush = linearBrush)
            drawRect(brush = topGlow)
            drawRect(brush = bottomGlow)
        }
    }
}
