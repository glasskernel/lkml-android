package com.fsck.k9.ui.compose

import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.widget.EditText
import com.fsck.k9.helper.TextWrapUtils
import kotlin.math.max
import kotlin.math.min

private const val NO_INDEX = -1

class AutoWrapTextWatcher(
    private val editText: EditText,
    private val wrapColumn: Int = TextWrapUtils.DEFAULT_WRAP_COLUMN,
) : TextWatcher {
    private var changeStart = NO_INDEX
    private var changeEnd = NO_INDEX
    private var shouldWrap = false
    private var selfChange = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (selfChange || s == null || count == 0) {
            reset()
            return
        }

        val insertedText = s.subSequence(start, start + count)
        shouldWrap = count > 1 || insertedText.any { it == ' ' || it == '\n' || it == '\r' }

        if (!shouldWrap) {
            reset()
            return
        }

        changeStart = start
        changeEnd = start + count
    }

    override fun afterTextChanged(s: Editable?) {
        val changeStart = changeStart
        val changeEnd = changeEnd
        val shouldWrap = shouldWrap
        reset()

        if (selfChange || s == null || !shouldWrap || changeStart == NO_INDEX) return

        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        if (selectionStart == NO_INDEX || selectionEnd == NO_INDEX) return

        // Only reflow the edited block so we don't rewrite unrelated text or move the cursor unexpectedly.
        val segmentStart = findSegmentStart(s, min(changeStart, selectionStart))
        val segmentEnd = findSegmentEnd(s, max(changeEnd, selectionEnd))
        val originalSegment = s.substring(segmentStart, segmentEnd)
        val wrappedSegment = wrapForEditing(originalSegment)

        if (wrappedSegment == originalSegment) return

        val newSelectionStart = segmentStart + mapSelectionOffset(
            originalSegment = originalSegment,
            selectionOffset = selectionStart - segmentStart,
        )
        val newSelectionEnd = segmentStart + mapSelectionOffset(
            originalSegment = originalSegment,
            selectionOffset = selectionEnd - segmentStart,
        )

        selfChange = true
        try {
            s.replace(segmentStart, segmentEnd, wrappedSegment)
            Selection.setSelection(
                s,
                newSelectionStart.coerceIn(0, s.length),
                newSelectionEnd.coerceIn(0, s.length),
            )
        } finally {
            selfChange = false
        }
    }

    private fun mapSelectionOffset(originalSegment: String, selectionOffset: Int): Int {
        return wrapForEditing(
            originalSegment.substring(0, selectionOffset.coerceIn(0, originalSegment.length)),
        ).length
    }

    private fun findSegmentStart(text: CharSequence, index: Int): Int {
        var current = index
        while (current > 0 && text[current - 1] != '\n' && text[current - 1] != '\r') {
            current--
        }
        return current
    }

    private fun findSegmentEnd(text: CharSequence, index: Int): Int {
        var current = index.coerceIn(0, text.length)
        while (current < text.length && text[current] != '\n' && text[current] != '\r') {
            current++
        }
        return current
    }

    private fun reset() {
        changeStart = NO_INDEX
        changeEnd = NO_INDEX
        shouldWrap = false
    }

    private fun wrapForEditing(text: String): String {
        return TextWrapUtils.wrapText(text, wrapColumn)
            .replace("\r\n", "\n")
            .replace('\r', '\n')
    }
}
