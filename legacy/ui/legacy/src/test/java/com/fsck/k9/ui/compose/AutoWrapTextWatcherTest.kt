package com.fsck.k9.ui.compose

import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.fsck.k9.K9RobolectricTest
import org.junit.Test

class AutoWrapTextWatcherTest : K9RobolectricTest() {
    @Test
    fun `wraps prose while preserving the cursor position`() {
        val editText = createEditText("one two four")
        editText.addTextChangedListener(AutoWrapTextWatcher(editText, wrapColumn = 10))
        editText.setSelection("one two".length)

        editText.text.insert(editText.selectionStart, " three")

        assertThat(editText.text.toString()).isEqualTo("one two\nthree four")
        assertThat(editText.selectionStart).isEqualTo("one two\nthree".length)
        assertThat(editText.selectionEnd).isEqualTo("one two\nthree".length)
    }

    @Test
    fun `keeps patch lines untouched`() {
        val editText = createEditText("+this patch")
        editText.addTextChangedListener(AutoWrapTextWatcher(editText, wrapColumn = 10))
        editText.setSelection(editText.text.length)

        editText.text.insert(editText.selectionStart, " line should stay")

        assertThat(editText.text.toString()).isEqualTo("+this patch line should stay")
    }

    @Test
    fun `preserves quote prefixes when wrapping`() {
        val editText = createEditText("> one two")
        editText.addTextChangedListener(AutoWrapTextWatcher(editText, wrapColumn = 10))
        editText.setSelection(editText.text.length)

        editText.text.insert(editText.selectionStart, " three")

        assertThat(editText.text.toString()).isEqualTo("> one two\n> three")
    }

    @Test
    fun `exposes a single argument constructor for Java callers`() {
        // Arrange
        val editText = createEditText("")
        val constructor = AutoWrapTextWatcher::class.java.getConstructor(EditText::class.java)

        // Act
        val testSubject = constructor.newInstance(editText)

        // Assert
        assertThat(testSubject).isNotNull()
    }

    private fun createEditText(text: String): EditText {
        return EditText(ApplicationProvider.getApplicationContext()).apply {
            setText(text)
        }
    }
}
