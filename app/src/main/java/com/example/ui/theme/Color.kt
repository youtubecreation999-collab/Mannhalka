package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Core Static Colors
val MannhalkaGreen = Color(0xFF075E54)
val MannhalkaTeal = Color(0xFF128C7E)
val MannhalkaLightGreen = Color(0xFF25D366)
val MannhalkaBackground = Color(0xFFECE5DD)
val ChatBubbleOut = Color(0xFFDCF8C6)
val ChatBubbleIn = Color(0xFFFFFFFF)
val TextGray = Color(0xFF707070)
val CleanWhite = Color(0xFFFFFFFF)
val DeepCharcoal = Color(0xFF191C1B)
val ErrorRed = Color(0xFFFF5E7E)

// Theme Palette Data Class
data class ThemePalette(
    val id: String,
    val name: String,
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val chatBubbleOut: Color,
    val chatBubbleIn: Color = Color(0xFFFFFFFF)
)

// 21 Predefined Distinctive Mannhalka-style Color Palettes
val ThemePalettes = listOf(
    ThemePalette("mannhalka_classic", "Mannhalka Classic", Color(0xFF075E54), Color(0xFF128C7E), Color(0xFFECE5DD), Color(0xFFDCF8C6)),
    ThemePalette("mannhalka_teal", "Mannhalka Teal", Color(0xFF00796B), Color(0xFF009688), Color(0xFFE0F2F1), Color(0xFFB2DFDB)),
    ThemePalette("forest_moss", "Forest Moss", Color(0xFF2E5A44), Color(0xFF3F7A5C), Color(0xFFEAF2EE), Color(0xFFC7E2D6)),
    ThemePalette("royal_blue", "Royal Blue", Color(0xFF0F4C81), Color(0xFF1F6CAF), Color(0xFFE8F1F5), Color(0xFFC2D9E8)),
    ThemePalette("midnight_navy", "Midnight Navy", Color(0xFF1A2B4C), Color(0xFF2B4370), Color(0xFFECEFF4), Color(0xFFD3DEF0)),
    ThemePalette("lavender_bloom", "Lavender Bloom", Color(0xFF5E35B1), Color(0xFF7E57C2), Color(0xFFF3E5F5), Color(0xFFE1BEE7)),
    ThemePalette("plum_wine", "Plum Wine", Color(0xFF4A148C), Color(0xFF6A1B9A), Color(0xFFF3E5F5), Color(0xFFE1BEE7)),
    ThemePalette("crimson_rose", "Crimson Rose", Color(0xFF880E4F), Color(0xFFAD1457), Color(0xFFFCE4EC), Color(0xFFF8BBD0)),
    ThemePalette("rust_orange", "Rust Orange", Color(0xFFD84315), Color(0xFFE64A19), Color(0xFFFBE9E7), Color(0xFFFFCCBC)),
    ThemePalette("sunset_coral", "Sunset Coral", Color(0xFFE91E63), Color(0xFFF48FB1), Color(0xFFFCE4EC), Color(0xFFF8BBD0)),
    ThemePalette("charcoal_dark", "Charcoal Dark", Color(0xFF212121), Color(0xFF424242), Color(0xFFEEEEEE), Color(0xFFE0E0E0)),
    ThemePalette("steel_gray", "Steel Gray", Color(0xFF37474F), Color(0xFF455A64), Color(0xFFECEFF1), Color(0xFFCFD8DC)),
    ThemePalette("cool_slate", "Cool Slate", Color(0xFF455A64), Color(0xFF546E7A), Color(0xFFECEFF1), Color(0xFFCFD8DC)),
    ThemePalette("olive_drab", "Olive Drab", Color(0xFF558B2F), Color(0xFF689F38), Color(0xFFF1F8E9), Color(0xFFDCEDC8)),
    ThemePalette("emerald_isle", "Emerald Isle", Color(0xFF00695C), Color(0xFF00796B), Color(0xFFE0F2F1), Color(0xFFB2DFDB)),
    ThemePalette("ocean_breeze", "Ocean Breeze", Color(0xFF00838F), Color(0xFF0097A7), Color(0xFFE0F7FA), Color(0xFFB2EBF2)),
    ThemePalette("sky_blue", "Sky Blue", Color(0xFF0277BD), Color(0xFF039BE5), Color(0xFFE1F5FE), Color(0xFFB3E5FC)),
    ThemePalette("cocoa_brown", "Cocoa Brown", Color(0xFF4E342E), Color(0xFF5D4037), Color(0xFFEFEBE9), Color(0xFFD7CCC8)),
    ThemePalette("sakura_pink", "Sakura Pink", Color(0xFFD81B60), Color(0xFFE91E63), Color(0xFFFCE4EC), Color(0xFFF8BBD0)),
    ThemePalette("cyber_purple", "Cyber Purple", Color(0xFF311B92), Color(0xFF4527A0), Color(0xFFEDE7F6), Color(0xFFD1C4E9)),
    ThemePalette("mint_fresh", "Mint Fresh", Color(0xFF00897B), Color(0xFF4DB6AC), Color(0xFFE0F2F1), Color(0xFFB2DFDB)),
    ThemePalette("midnight_obsidian", "Midnight Obsidian", Color(0xFF2C2C2C), Color(0xFF1A1A1A), Color(0xFF000000), Color(0xFF1A1A1A))
)
