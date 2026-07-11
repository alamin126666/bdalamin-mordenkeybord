package com.modernkey.keyboard.font

enum class FontStyle(val displayName: String, val preview: String) {
    NORMAL("Normal", "Abc"),
    BOLD_SERIF("Bold", "𝐀𝐛𝐜"),
    ITALIC_SERIF("Italic", "𝐴𝑏𝑐"),
    BOLD_ITALIC("Bold Italic", "𝑨𝒃𝒄"),
    SCRIPT("Script", "𝒜𝒷𝒸"),
    BOLD_SCRIPT("Bold Script", "𝓐𝓫𝓬"),
    FRAKTUR("Fraktur", "𝔄𝔟𝔠"),
    DOUBLE_STRUCK("Double", "𝔸𝕓𝕔"),
    MONOSPACE("Mono", "𝙰𝚋𝚌"),
    CIRCLED("Circle", "Ⓐⓑⓒ"),
    SQUARED("Square", "🅰bc"),
    TINY_CAPS("Tiny", "ᴀʙᴄ"),
    UPSIDE_DOWN("Flip", "ɐqɔ"),
    STRIKETHROUGH("Strike", "A̶b̶c̶"),
    UNDERLINE("Under", "A͟b͟c͟")
}
