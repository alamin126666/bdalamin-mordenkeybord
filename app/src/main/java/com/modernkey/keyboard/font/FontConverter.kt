package com.modernkey.keyboard.font

object FontConverter {

    fun convert(text: String, style: FontStyle): String {
        if (style == FontStyle.NORMAL) return text
        return when (style) {
            FontStyle.BOLD_SERIF    -> transform(text, FontCharMaps.boldSerif)
            FontStyle.ITALIC_SERIF  -> transform(text, FontCharMaps.italicSerif)
            FontStyle.BOLD_ITALIC   -> transform(text, BOLD_ITALIC)
            FontStyle.SCRIPT        -> transform(text, SCRIPT)
            FontStyle.BOLD_SCRIPT   -> transform(text, FontCharMaps.boldScript)
            FontStyle.FRAKTUR       -> transform(text, FRAKTUR)
            FontStyle.DOUBLE_STRUCK -> transform(text, FontCharMaps.doubleStruck)
            FontStyle.MONOSPACE     -> transform(text, FontCharMaps.monospace)
            FontStyle.CIRCLED       -> transform(text, FontCharMaps.circled)
            FontStyle.SQUARED       -> transformSquared(text)
            FontStyle.TINY_CAPS     -> transform(text, FontCharMaps.tinyCaps)
            FontStyle.UPSIDE_DOWN   -> transform(text, FontCharMaps.upsideDown).reversed()
            FontStyle.STRIKETHROUGH -> addCombining(text, '\u0336')
            FontStyle.UNDERLINE     -> addCombining(text, '\u0332')
            FontStyle.NORMAL        -> text
        }
    }

    private fun transform(text: String, map: Map<Char, String>): String =
        text.map { c -> map[c] ?: c.toString() }.joinToString("")

    private fun addCombining(text: String, combiner: Char): String =
        text.map { c -> if (c == ' ') c.toString() else "$c$combiner" }.joinToString("")

    private fun transformSquared(text: String): String {
        val squaredUpper = mapOf(
            'A' to "\uD83C\uDD70", 'B' to "\uD83C\uDD71", 'C' to "\uD83C\uDD72",
            'D' to "\uD83C\uDD73", 'E' to "\uD83C\uDD74", 'F' to "\uD83C\uDD75",
            'G' to "\uD83C\uDD76", 'H' to "\uD83C\uDD77", 'I' to "\uD83C\uDD78",
            'J' to "\uD83C\uDD79", 'K' to "\uD83C\uDD7A", 'L' to "\uD83C\uDD7B",
            'M' to "\uD83C\uDD7C", 'N' to "\uD83C\uDD7D", 'O' to "\uD83C\uDD7E",
            'P' to "\uD83C\uDD7F", 'Q' to "\uD83C\uDD80", 'R' to "\uD83C\uDD81",
            'S' to "\uD83C\uDD82", 'T' to "\uD83C\uDD83", 'U' to "\uD83C\uDD84",
            'V' to "\uD83C\uDD85", 'W' to "\uD83C\uDD86", 'X' to "\uD83C\uDD87",
            'Y' to "\uD83C\uDD88", 'Z' to "\uD83C\uDD89"
        )
        return text.map { c ->
            squaredUpper[c.uppercaseChar()] ?: squaredUpper[c] ?: c.toString()
        }.joinToString("")
    }

    // ── Static maps with proper surrogate pairs ────────────────────────────────

    private val BOLD_ITALIC = mapOf(
        'A' to "\uD835\uDC68", 'B' to "\uD835\uDC69", 'C' to "\uD835\uDC6A",
        'D' to "\uD835\uDC6B", 'E' to "\uD835\uDC6C", 'F' to "\uD835\uDC6D",
        'G' to "\uD835\uDC6E", 'H' to "\uD835\uDC6F", 'I' to "\uD835\uDC70",
        'J' to "\uD835\uDC71", 'K' to "\uD835\uDC72", 'L' to "\uD835\uDC73",
        'M' to "\uD835\uDC74", 'N' to "\uD835\uDC75", 'O' to "\uD835\uDC76",
        'P' to "\uD835\uDC77", 'Q' to "\uD835\uDC78", 'R' to "\uD835\uDC79",
        'S' to "\uD835\uDC7A", 'T' to "\uD835\uDC7B", 'U' to "\uD835\uDC7C",
        'V' to "\uD835\uDC7D", 'W' to "\uD835\uDC7E", 'X' to "\uD835\uDC7F",
        'Y' to "\uD835\uDC80", 'Z' to "\uD835\uDC81",
        'a' to "\uD835\uDC82", 'b' to "\uD835\uDC83", 'c' to "\uD835\uDC84",
        'd' to "\uD835\uDC85", 'e' to "\uD835\uDC86", 'f' to "\uD835\uDC87",
        'g' to "\uD835\uDC88", 'h' to "\uD835\uDC89", 'i' to "\uD835\uDC8A",
        'j' to "\uD835\uDC8B", 'k' to "\uD835\uDC8C", 'l' to "\uD835\uDC8D",
        'm' to "\uD835\uDC8E", 'n' to "\uD835\uDC8F", 'o' to "\uD835\uDC90",
        'p' to "\uD835\uDC91", 'q' to "\uD835\uDC92", 'r' to "\uD835\uDC93",
        's' to "\uD835\uDC94", 't' to "\uD835\uDC95", 'u' to "\uD835\uDC96",
        'v' to "\uD835\uDC97", 'w' to "\uD835\uDC98", 'x' to "\uD835\uDC99",
        'y' to "\uD835\uDC9A", 'z' to "\uD835\uDC9B"
    )

    // Mathematical Script (U+1D49C..U+1D4CF) → surrogate pairs
    private val SCRIPT = mapOf(
        'A' to "\uD835\uDC9C", 'B' to "\u212C",       'C' to "\uD835\uDC9E",
        'D' to "\uD835\uDC9F", 'E' to "\u2130",       'F' to "\u2131",
        'G' to "\uD835\uDCA2", 'H' to "\u210B",       'I' to "\u2110",
        'J' to "\uD835\uDCA5", 'K' to "\uD835\uDCA6", 'L' to "\u2112",
        'M' to "\u2133",       'N' to "\uD835\uDCA9", 'O' to "\uD835\uDCAA",
        'P' to "\uD835\uDCAB", 'Q' to "\uD835\uDCAC", 'R' to "\u211B",
        'S' to "\uD835\uDCAE", 'T' to "\uD835\uDCAF", 'U' to "\uD835\uDCB0",
        'V' to "\uD835\uDCB1", 'W' to "\uD835\uDCB2", 'X' to "\uD835\uDCB3",
        'Y' to "\uD835\uDCB4", 'Z' to "\uD835\uDCB5",
        'a' to "\uD835\uDCB6", 'b' to "\uD835\uDCB7", 'c' to "\uD835\uDCB8",
        'd' to "\uD835\uDCB9", 'e' to "\u212F",       'f' to "\uD835\uDCBB",
        'g' to "\u210A",       'h' to "\uD835\uDCBD", 'i' to "\uD835\uDCBE",
        'j' to "\uD835\uDCBF", 'k' to "\uD835\uDCC0", 'l' to "\uD835\uDCC1",
        'm' to "\uD835\uDCC2", 'n' to "\uD835\uDCC3", 'o' to "\u2134",
        'p' to "\uD835\uDCC5", 'q' to "\uD835\uDCC6", 'r' to "\uD835\uDCC7",
        's' to "\uD835\uDCC8", 't' to "\uD835\uDCC9", 'u' to "\uD835\uDCCA",
        'v' to "\uD835\uDCCB", 'w' to "\uD835\uDCCC", 'x' to "\uD835\uDCCD",
        'y' to "\uD835\uDCCE", 'z' to "\uD835\uDCCF"
    )

    // Mathematical Fraktur (U+1D504..U+1D537) → surrogate pairs
    private val FRAKTUR = mapOf(
        'A' to "\uD835\uDD04", 'B' to "\uD835\uDD05", 'C' to "\u212D",
        'D' to "\uD835\uDD07", 'E' to "\uD835\uDD08", 'F' to "\uD835\uDD09",
        'G' to "\uD835\uDD0A", 'H' to "\u210C",       'I' to "\u2111",
        'J' to "\uD835\uDD0D", 'K' to "\uD835\uDD0E", 'L' to "\uD835\uDD0F",
        'M' to "\uD835\uDD10", 'N' to "\uD835\uDD11", 'O' to "\uD835\uDD12",
        'P' to "\uD835\uDD13", 'Q' to "\uD835\uDD14", 'R' to "\u211C",
        'S' to "\uD835\uDD16", 'T' to "\uD835\uDD17", 'U' to "\uD835\uDD18",
        'V' to "\uD835\uDD19", 'W' to "\uD835\uDD1A", 'X' to "\uD835\uDD1B",
        'Y' to "\uD835\uDD1C", 'Z' to "\u2128",
        'a' to "\uD835\uDD1E", 'b' to "\uD835\uDD1F", 'c' to "\uD835\uDD20",
        'd' to "\uD835\uDD21", 'e' to "\uD835\uDD22", 'f' to "\uD835\uDD23",
        'g' to "\uD835\uDD24", 'h' to "\uD835\uDD25", 'i' to "\uD835\uDD26",
        'j' to "\uD835\uDD27", 'k' to "\uD835\uDD28", 'l' to "\uD835\uDD29",
        'm' to "\uD835\uDD2A", 'n' to "\uD835\uDD2B", 'o' to "\uD835\uDD2C",
        'p' to "\uD835\uDD2D", 'q' to "\uD835\uDD2E", 'r' to "\uD835\uDD2F",
        's' to "\uD835\uDD30", 't' to "\uD835\uDD31", 'u' to "\uD835\uDD32",
        'v' to "\uD835\uDD33", 'w' to "\uD835\uDD34", 'x' to "\uD835\uDD35",
        'y' to "\uD835\uDD36", 'z' to "\uD835\uDD37"
    )
}
