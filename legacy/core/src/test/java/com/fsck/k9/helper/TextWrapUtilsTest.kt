package com.fsck.k9.helper

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class TextWrapUtilsTest {
    @Test
    fun `wrapText preserves CRLF while wrapping long prose`() {
        val text = "one two three four five six seven eight nine ten eleven twelve thirteen fourteen\r\nsecond line"

        val result = TextWrapUtils.wrapText(text)

        assertThat(result).isEqualTo(
            "one two three four five six seven eight nine ten eleven twelve thirteen\r\nfourteen\r\nsecond line",
        )
    }

    @Test
    fun `wrapText uses CRLF when wrapping a single long line`() {
        val text = "> one two three four five six seven eight nine ten eleven twelve thirteen fourteen"

        val result = TextWrapUtils.wrapText(text)

        assertThat(result).isEqualTo(
            "> one two three four five six seven eight nine ten eleven twelve\r\n> thirteen fourteen",
        )
    }

    @Test
    fun `wrapText preserves trailing blank lines`() {
        val text = "one two three four five six seven eight nine ten eleven twelve thirteen fourteen\r\n\r\n"

        val result = TextWrapUtils.wrapText(text)

        assertThat(result).isEqualTo(
            "one two three four five six seven eight nine ten eleven twelve thirteen\r\nfourteen\r\n\r\n",
        )
    }

    @Test
    fun `wrapText preserves patch and trailer lines`() {
        val text =
            "Fixes: https://example.com/this-link-should-not-be-broken-even-when-it-is-very-long\n" +
                "+this patch line should stay exactly as it is even when it is long long long long"

        val result = TextWrapUtils.wrapText(text, 20)

        assertThat(result).isEqualTo(text)
    }

    @Test
    fun `wrapText limits signature handling to the matching quote prefix`() {
        val text =
            "> -- \n> Alice Example\none two three four five six seven eight nine ten eleven twelve thirteen fourteen"

        val result = TextWrapUtils.wrapText(text, 20)

        assertThat(result).isEqualTo(
            "> -- \n> Alice Example\none two three four\nfive six seven eight\nnine ten eleven\n" +
                "twelve thirteen\nfourteen",
        )
    }

    @Test
    fun `wrapQuotedText preserves custom prefixes while wrapping`() {
        val text = "one two three four five six seven eight nine ten eleven twelve thirteen fourteen"

        val result = TextWrapUtils.wrapQuotedText(text, "$1\t ", 24)

        assertThat(result).isEqualTo(
            "$1\t one two three four\r\n$1\t five six seven eight\r\n" +
                "$1\t nine ten eleven\r\n$1\t twelve thirteen\r\n$1\t fourteen",
        )
    }
}
