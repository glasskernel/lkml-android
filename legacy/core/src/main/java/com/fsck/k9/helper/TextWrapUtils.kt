package com.fsck.k9.helper

object TextWrapUtils {
    const val DEFAULT_WRAP_COLUMN = 75

    private const val CRLF = "\r\n"
    private const val SIGNATURE_SEPARATOR = "-- "
    private val TRAILER_OR_HEADER_REGEX = Regex("""^[A-Za-z0-9][A-Za-z0-9-]{0,40}:\s+.*$""")

    @JvmStatic
    fun wrapText(text: String, limit: Int = DEFAULT_WRAP_COLUMN): String {
        val lineBreak = detectLineBreak(text)
        val lines = splitLines(text)
        val result = mutableListOf<String>()
        var signaturePrefix: String? = null

        for (line in lines) {
            val quotePrefix = extractQuotePrefix(line)
            val content = line.removePrefix(quotePrefix)

            if (signaturePrefix != null && quotePrefix == signaturePrefix) {
                result.add(line)
                continue
            } else if (signaturePrefix != null) {
                signaturePrefix = null
            }

            if (content == SIGNATURE_SEPARATOR) {
                signaturePrefix = quotePrefix
                result.add(line)
                continue
            }

            val availableWidth = limit - quotePrefix.length
            result.addAll(
                wrapLineContent(
                    content = content,
                    prefix = quotePrefix,
                    limit = limit,
                    availableWidth = availableWidth,
                ),
            )
        }

        return result.joinToString(lineBreak)
    }

    @JvmStatic
    fun wrapQuotedText(text: String, prefix: String, limit: Int = DEFAULT_WRAP_COLUMN): String {
        val lineBreak = detectLineBreak(text)
        val lines = splitLines(text)
        val result = mutableListOf<String>()
        var inSignature = false
        val availableWidth = limit - prefix.length

        for (line in lines) {
            if (inSignature) {
                result.add(prefix + line)
                continue
            }

            if (line == SIGNATURE_SEPARATOR) {
                inSignature = true
                result.add(prefix + line)
                continue
            }

            result.addAll(
                wrapLineContent(
                    content = line,
                    prefix = prefix,
                    limit = limit,
                    availableWidth = availableWidth,
                ),
            )
        }

        return result.joinToString(lineBreak)
    }

    private fun wrapLineContent(content: String, prefix: String, limit: Int, availableWidth: Int): List<String> {
        val shouldKeepLine = content.isEmpty() ||
            availableWidth <= 0 ||
            prefix.length + content.length <= limit ||
            shouldPreserve(content)

        if (shouldKeepLine) {
            return listOf(prefix + content)
        }

        return wrapLongLine(content, availableWidth).map { prefix + it }
    }

    private fun shouldPreserve(line: String): Boolean {
        return line.startsWith(' ') ||
            line.startsWith('	') ||
            line.contains('	') ||
            isPatchLine(line) ||
            TRAILER_OR_HEADER_REGEX.matches(line)
    }

    private fun isPatchLine(line: String): Boolean {
        return line.startsWith("diff --") ||
            line.startsWith("index ") ||
            line.startsWith("---") ||
            line.startsWith("+++") ||
            line.startsWith("@@") ||
            line.startsWith("+") ||
            line.startsWith("-") ||
            line.startsWith(" ") ||
            line.startsWith("Index: ") ||
            line.startsWith("rename from ") ||
            line.startsWith("rename to ") ||
            line.startsWith("copy from ") ||
            line.startsWith("copy to ") ||
            line.startsWith("new file mode ") ||
            line.startsWith("deleted file mode ") ||
            line.startsWith("old mode ") ||
            line.startsWith("new mode ") ||
            line.startsWith("similarity index ") ||
            line.startsWith("dissimilarity index ") ||
            line.startsWith("Binary files ") ||
            line == "GIT binary patch"
    }

    private fun wrapLongLine(line: String, availableWidth: Int): List<String> {
        val subLines = mutableListOf<String>()
        var remaining = line

        while (remaining.length > availableWidth) {
            var splitIndex = remaining.lastIndexOf(' ', availableWidth)
            if (splitIndex <= 0) {
                splitIndex = remaining.indexOf(' ', availableWidth)
            }

            if (splitIndex == -1) {
                subLines.add(remaining)
                remaining = ""
            } else {
                subLines.add(remaining.substring(0, splitIndex))
                remaining = remaining.substring(splitIndex + 1)
            }
        }

        if (remaining.isNotEmpty()) {
            subLines.add(remaining)
        }

        return subLines
    }

    private fun extractQuotePrefix(line: String): String {
        var index = 0
        while (index < line.length && line[index] == '>') {
            index++
            if (index < line.length && line[index] == ' ') {
                index++
            }
        }
        return line.substring(0, index)
    }

    private fun normalizeLineBreaks(text: String): String {
        return text.replace(CRLF, "\n").replace('\r', '\n')
    }

    private fun splitLines(text: String): List<String> {
        return normalizeLineBreaks(text).split("\n", ignoreCase = false, limit = Int.MAX_VALUE)
    }

    private fun detectLineBreak(text: String): String {
        return when {
            text.contains(CRLF) -> CRLF
            text.contains('\r') -> "\r"
            text.contains('\n') -> "\n"
            else -> CRLF
        }
    }
}
