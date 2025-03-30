package net.tactware.nimbus.appwide.utils

/**
 * Utility functions for handling HTML content.
 */
object HtmlUtils {
    /**
     * Converts HTML content to plain text by removing HTML tags and decoding HTML entities.
     *
     * @param html The HTML content to convert
     * @return The plain text representation of the HTML content
     */
    fun htmlToText(html: String?): String {
        if (html == null || html.isBlank()) {
            return ""
        }

        // Replace common HTML entities
        var result = html
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")

        // Replace <br>, <p>, <div> tags with newlines
        result = result
            .replace("<br>", "\n")
            .replace("<br/>", "\n")
            .replace("<br />", "\n")
            .replace("</p>", "\n")
            .replace("</div>", "\n")

        // Remove all HTML tags
        result = result.replace(Regex("<[^>]*>"), "")

        // Normalize whitespace
        result = result.replace(Regex("\\s+"), " ").trim()

        // Replace multiple newlines with a single newline
        result = result.replace(Regex("\n+"), "\n")

        return result
    }
}