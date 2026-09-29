package com.shuttletechnologies.speakercleaner.localization

data class SupportedLanguage(
    val code: String,
    val nameEnglish: String,
    val nameNative: String,
    val flagEmoji: String,
    val isRtl: Boolean = false
)

object Locales {
    val supportedLanguages = listOf(
        SupportedLanguage("en_US", "English", "English", "🇺🇸"),
        SupportedLanguage("es_ES", "Spanish", "Español", "🇪🇸"),
        SupportedLanguage("de_DE", "German", "Deutsch", "🇩🇪"),
        SupportedLanguage("fr_FR", "French", "Français", "🇫🇷"),
        SupportedLanguage("ja_JP", "Japanese", "日本語", "🇯🇵"),
        SupportedLanguage("ar_SA", "Arabic", "العربية", "🇸🇦", isRtl = true),
        SupportedLanguage("hi_IN", "Hindi", "हिन्दी", "🇮🇳"),
        SupportedLanguage("pt_BR", "Portuguese", "Português", "🇧🇷"),
        SupportedLanguage("zh_CN", "Chinese Simplified", "简体中文", "🇨🇳"),
        SupportedLanguage("zh_TW", "Chinese Traditional", "繁體中文", "🇹🇼"),
        SupportedLanguage("ru_RU", "Russian", "Русский", "🇷🇺"),
        SupportedLanguage("ko_KR", "Korean", "한국어", "🇰🇷"),
        SupportedLanguage("it_IT", "Italian", "Italiano", "🇮🇹"),
        SupportedLanguage("id_ID", "Indonesian", "Bahasa Indonesia", "🇮🇩"),
        SupportedLanguage("tr_TR", "Turkish", "Türkçe", "🇹🇷"),
        SupportedLanguage("vi_VN", "Vietnamese", "Tiếng Việt", "🇻🇳")
    )

    fun getLanguage(code: String): SupportedLanguage {
        return supportedLanguages.find { it.code.equals(code, ignoreCase = true) }
            ?: supportedLanguages.first()
    }
}
