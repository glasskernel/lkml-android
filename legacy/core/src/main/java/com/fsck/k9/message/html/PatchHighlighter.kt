package com.fsck.k9.message.html

import com.fsck.k9.helper.TextWrapUtils
import net.thunderbird.core.preference.lkml.LKML_SETTINGS_DEFAULT_PATCH_HIGHLIGHT

internal object PatchHighlighter : TextToHtml.HtmlModifier {
    override fun findModifications(text: CharSequence): List<HtmlModification> {
        return emptyList()
    }
}

internal class PatchLineModification(
    override val startIndex: Int,
    override val endIndex: Int,
    private val type: PatchLineType,
) : HtmlModification.Wrap {
    override fun appendPrefix(textToHtml: TextToHtml) {
        val color = when (type) {
            PatchLineType.ADDITION -> "#2cbe4e" // Green
            PatchLineType.DELETION -> "#cb2431" // Red
            PatchLineType.HEADER -> "#005cc5" // Blue
        }
        textToHtml.appendHtml("<span style=\"color: $color\">")
    }

    override fun appendSuffix(textToHtml: TextToHtml) {
        textToHtml.appendHtml("</span>")
    }
}

internal enum class PatchLineType {
    ADDITION,
    DELETION,
    HEADER,
}
