package dev.bober.finik.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import dev.bober.finik.core.model.HistoryTone
import dev.bober.finik.core.model.LogTone
import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.model.TaskKind

/**
 * Цветовые токены макета «Финик 8–11». Значения — sRGB-эквиваленты oklch из макета
 * (в комментариях оригинал), чтобы вёрстка совпадала с прототипом.
 */
object FinikColor {

    // ── Фон и поверхности ────────────────────────────────────────────────
    val Background = Color(0xFFFCFAF4)      // oklch(0.985 0.008 95)
    val Surface = Color.White
    val SurfaceMuted = Color(0xFFF8F7F1)    // oklch(0.975 0.008 95) — кнопки −/+
    val SurfaceSoft = Color(0xFFF7F5EF)     // oklch(0.97 0.008 95) — недоступный уход
    val SurfaceCream = Color(0xFFFAF7EC)    // oklch(0.975 0.014 95) — низ градиента героя
    val Chip = Color(0xFFF2F0E9)            // oklch(0.955 0.01 95) — метка задания, неактивный день
    val ChipTrait = Color(0xFFF2EFE0)       // oklch(0.95 0.02 95)
    val ChipKindDay = Color(0xFFF4F0E1)     // oklch(0.955 0.02 95)
    val ChipStat = Color(0xFFF6F2E3)        // oklch(0.96 0.02 95)
    val IconButton = Color(0xFFEDEBE2)      // oklch(0.94 0.012 95) — квадратные кнопки, трек плана
    val Track = Color(0xFFEAE8DF)           // oklch(0.93 0.012 95) — трек полосок, линия над навигацией
    val Border = Color(0xFFE7E5DA)          // oklch(0.92 0.015 95) — обводка карточек
    val BorderStrong = Color(0xFFE4E1D6)    // oklch(0.91 0.015 95) — обводка кнопок и чипов
    val BorderSoft = Color(0xFFEBE8DD)      // oklch(0.93 0.015 95) — стадии, достижения
    val InputBorder = Color(0xFFE1DED3)     // oklch(0.9 0.015 95)
    val DisabledButton = Color(0xFFF0EEE7)  // oklch(0.95 0.01 95)
    val Scrim = Color(0x732B221A)           // oklch(0.26 0.02 60 / .45)
    val WhiteGlass = Color(0xCCFFFFFF)      // #ffffffcc
    val WhiteGlassSoft = Color(0xB3FFFFFF)  // #ffffffb3

    // ── Текст ────────────────────────────────────────────────────────────
    val Ink = Color(0xFF2B221A)             // oklch(0.26 0.02 60) — основной
    val InkDeep = Color(0xFF25170C)         // oklch(0.22 0.03 60) — глаза
    val Mouth = Color(0xFF372414)           // oklch(0.28 0.04 60)
    val InkBadge = Color(0xFF30271F)        // oklch(0.28 0.02 60)
    val Text40 = Color(0xFF50453D)          // oklch(0.40 0.02 60)
    val Text42 = Color(0xFF564B42)          // oklch(0.42 0.02 60)
    val Text43 = Color(0xFF584D45)          // oklch(0.43 0.02 60)
    val Text44 = Color(0xFF5B5048)          // oklch(0.44 0.02 60)
    val Text45 = Color(0xFF5E534A)          // oklch(0.45 0.02 60)
    val Text46 = Color(0xFF61564D)          // oklch(0.46 0.02 60)
    val Text47 = Color(0xFF645850)          // oklch(0.47 0.02 60)
    val Text48 = Color(0xFF665B53)          // oklch(0.48 0.02 60)
    val Text50 = Color(0xFF6C6158)          // oklch(0.50 0.02 60) — самый частый вторичный
    val Text52 = Color(0xFF72665E)          // oklch(0.52 0.02 60)
    val Text54 = Color(0xFF786C63)          // oklch(0.54 0.02 60)
    val Text56 = Color(0xFF7E7269)          // oklch(0.56 0.02 60) — «Шаг 1 из 2»
    val Text58 = Color(0xFF84786F)          // oklch(0.58 0.02 60)
    val Text60 = Color(0xFF8A7E75)          // oklch(0.60 0.02 60) — зачёркнутая цена
    val TextWarm34 = Color(0xFF473322)      // oklch(0.34 0.04 60) — цифры дохода
    val TextWarm38 = Color(0xFF4F3F32)      // oklch(0.38 0.03 60) — «недель с планом»
    val TextWarm42 = Color(0xFF5A493D)      // oklch(0.42 0.03 60)
    val TextWarm44 = Color(0xFF5F4F42)      // oklch(0.44 0.03 60)
    val Avatar = Color(0xFF887769)          // oklch(0.58 0.03 60)
    val DotInactive = Color(0xFFB2A9A2)     // oklch(0.74 0.015 60)
    val QuizKey = Color(0xFFBBAEA5)         // oklch(0.76 0.02 60)

    // ── Зелёный (основной акцент) ────────────────────────────────────────
    val Green = Color(0xFF3B9555)           // oklch(0.60 0.13 150)
    val GreenPressed = Color(0xFF218041)    // oklch(0.53 0.13 150)
    val GreenLink = Color(0xFF21763C)       // oklch(0.50 0.12 150)
    val GreenLog = Color(0xFF298646)        // oklch(0.55 0.13 150)
    val GreenStem = Color(0xFF3D8E53)       // oklch(0.58 0.12 150)
    val GreenStageOpen = Color(0xFF5DAD70)  // oklch(0.68 0.12 150)
    val GreenBud = Color(0xFF5CB572)        // oklch(0.70 0.13 150)
    val GreenJar = Color(0xFF6FB07D)        // oklch(0.70 0.10 150)
    val GreenBadge = Color(0xFF75B683)      // oklch(0.72 0.10 150)
    val GreenInk34 = Color(0xFF114320)      // oklch(0.34 0.08 150)
    val GreenInk36 = Color(0xFF0D4A22)      // oklch(0.36 0.09 150) — активная вкладка
    val GreenToast = Color(0xFF1E4729)      // oklch(0.36 0.07 150)
    val GreenInk38 = Color(0xFF1D4E2B)      // oklch(0.38 0.08 150)
    val GreenInk40 = Color(0xFF295233)      // oklch(0.40 0.07 150)
    val GreenInkLesson = Color(0xFF1B552C)  // oklch(0.40 0.09 150)
    val GreenInk40s = Color(0xFF344F39)     // oklch(0.40 0.05 150)
    val GreenInkQuiz = Color(0xFF2F5136)    // oklch(0.40 0.06 150)
    val GreenInk42 = Color(0xFF39553F)      // oklch(0.42 0.05 150)
    val GreenInk42s = Color(0xFF34563B)     // oklch(0.42 0.06 150) — подпись «отложено всего»
    val GreenInk44 = Color(0xFF3E5B44)      // oklch(0.44 0.05 150)
    val GreenBorderActive = Color(0xFFC1E1C6) // oklch(0.88 0.05 150)
    val GreenTrack = Color(0xFFCEE1D1)      // oklch(0.89 0.03 150)
    val GreenBorderBtn = Color(0xFFD1E4D4)  // oklch(0.90 0.03 150)
    val GreenBorderDone = Color(0xFFCCE6D0) // oklch(0.90 0.04 150)
    val GreenGlow = Color(0xFFDCF7E1)       // oklch(0.95 0.04 150) — радиальный градиент welcome
    val GreenChipBonus = Color(0xFFE3F4E6)  // oklch(0.95 0.025 150)
    val GreenChipLesson = Color(0xFFE1F5E4) // oklch(0.95 0.03 150)
    val GreenPill = Color(0xFFDFF6E2)       // oklch(0.95 0.035 150) — активная вкладка
    val GreenChipStage = Color(0xFFE3F6E6)  // oklch(0.955 0.03 150)
    val GreenCard = Color(0xFFE4F8E7)       // oklch(0.96 0.03 150)
    val GreenSelected = Color(0xFFEEFBF0)   // oklch(0.975 0.02 150)
    val GreenHeroTop = Color(0xFFE2F7E2)    // oklch(0.955 0.035 145)
    val GreenStat = Color(0xFF154F27)       // oklch(0.38 0.09 150) — цифра «4 статьи» на welcome

    // ── Монеты / жёлтый ──────────────────────────────────────────────────
    val Coin = Color(0xFFEABE4A)            // oklch(0.82 0.14 88)
    val CoinBorder = Color(0xFFD29922)      // oklch(0.72 0.14 80)
    val CoinChip = Color(0xFFFAF0D2)        // oklch(0.955 0.04 90)
    val CoinInk = Color(0xFF57380F)         // oklch(0.37 0.07 70)
    val CoinInk36 = Color(0xFF54360B)       // oklch(0.36 0.07 70)
    val CoinInk34 = Color(0xFF4C3211)       // oklch(0.34 0.06 70)
    val CoinInkStreak = Color(0xFF4F3005)   // oklch(0.34 0.07 70)
    val CoinInk40 = Color(0xFF604018)       // oklch(0.40 0.07 70)
    val CoinInk42 = Color(0xFF5F482E)       // oklch(0.42 0.05 70)
    val CoinInk44 = Color(0xFF644E33)       // oklch(0.44 0.05 70)
    val CoinInk46 = Color(0xFF6A5339)       // oklch(0.46 0.05 70)
    val CoinInk47 = Color(0xFF6D563C)       // oklch(0.47 0.05 70)
    val CoinInk48 = Color(0xFF6C5A45)       // oklch(0.48 0.04 70)
    val Gold = Color(0xFFE4B750)            // oklch(0.80 0.13 85) — выполненное достижение
    val GoldLog = Color(0xFFA1790C)         // oklch(0.60 0.12 85)
    val GoldCard = Color(0xFFFDF6E4)        // oklch(0.975 0.025 90)
    val GoldBorder = Color(0xFFF1DFBC)      // oklch(0.91 0.05 85)
    val Orange = Color(0xFFF6AB6B)          // oklch(0.80 0.12 60) — иконка «Достижения»

    // ── Красный ──────────────────────────────────────────────────────────
    val Red = Color(0xFF9D3533)             // oklch(0.48 0.14 25)
    val RedSaleInk = Color(0xFF932B2A)      // oklch(0.45 0.14 25)
    val RedSaleBg = Color(0xFFFFDCD7)       // oklch(0.94 0.06 25)
    val RedStat = Color(0xFF7A3430)         // oklch(0.42 0.10 25)
    val RedCost = Color(0xFFA74541)         // oklch(0.52 0.13 25)
    val RedBorderCare = Color(0xFFFCE1DE)   // oklch(0.93 0.03 25)
    val RedBorderPlan = Color(0xFFF8DDDB)   // oklch(0.92 0.03 25)
    val RedBorderSale = Color(0xFFFEDBD7)   // oklch(0.92 0.04 25)
    val RedBorderReset = Color(0xFFF8D9D6)  // oklch(0.91 0.035 25)
    val RedIcon = Color(0xFFE47C75)         // oklch(0.70 0.13 25)
    val RedReset = Color(0xFF8C2D2B)        // oklch(0.44 0.13 25)
    val RedLog = Color(0xFFB54A46)          // oklch(0.55 0.14 25)
    val RedPicked = Color(0xFFC65954)       // oklch(0.60 0.14 25)
    val RedPickedBorder = Color(0xFFDB6C66) // oklch(0.66 0.14 25)
    val RedPickedBg = Color(0xFFFFF1EE)     // oklch(0.975 0.025 25)
    val RedHistory = Color(0xFFF2A7A1)      // oklch(0.80 0.09 25)
    val RedNoteBg = Color(0xFFFFEEEB)       // oklch(0.97 0.03 25)
    val RedNoteInk = Color(0xFF822B2A)      // oklch(0.42 0.12 25)
    val RedMarker = Color(0xFFA43B38)       // oklch(0.50 0.14 25)

    // ── Категории и предметы ─────────────────────────────────────────────
    val Food = Color(0xFFE5946F)            // oklch(0.74 0.11 45)
    val FoodBright = Color(0xFFEB9A75)      // oklch(0.76 0.11 45)
    val FoodPot = Color(0xFFE9A679)         // oklch(0.78 0.10 55)
    val Water = Color(0xFF49ABD6)           // oklch(0.70 0.11 230)
    val WaterCan = Color(0xFF3BA9BA)        // oklch(0.68 0.10 210)
    val Play = Color(0xFFBA8AC5)            // oklch(0.70 0.10 320)
    val PlayPot = Color(0xFFAE96DA)         // oklch(0.72 0.10 300)
    val Pot = Color(0xFFDC9A6C)             // oklch(0.74 0.10 55)
    val Sound = Color(0xFF56C4CA)           // oklch(0.76 0.10 200)
    val WeekChip = Color(0xFFE1F0FF)        // oklch(0.95 0.03 255)
    val WeekInk = Color(0xFF234E82)         // oklch(0.42 0.10 255)

    // ── История накоплений ───────────────────────────────────────────────
    val HistoryLow = Color(0xFF93C69D)      // oklch(0.78 0.08 150)
    val HistoryHigh = Color(0xFF69B27A)     // oklch(0.70 0.11 150)
    val HistoryMid = Color(0xFF48A260)      // oklch(0.64 0.13 150)
}

val SpendCategory.color: Color
    get() = when (this) {
        SpendCategory.FOOD -> FinikColor.Food
        SpendCategory.WATER -> FinikColor.Water
        SpendCategory.PLAY -> FinikColor.Play
        SpendCategory.SAVE -> FinikColor.Green
    }

val TaskKind.chipBackground: Color
    get() = when (this) {
        TaskKind.WEEK -> FinikColor.WeekChip
        TaskKind.LESSON -> FinikColor.GreenChipLesson
        else -> FinikColor.ChipKindDay
    }

val TaskKind.chipInk: Color
    get() = when (this) {
        TaskKind.WEEK -> FinikColor.WeekInk
        TaskKind.LESSON -> FinikColor.GreenInkLesson
        else -> FinikColor.Text45
    }

val HistoryTone.color: Color
    get() = when (this) {
        HistoryTone.LOW -> FinikColor.HistoryLow
        HistoryTone.MID -> FinikColor.HistoryMid
        HistoryTone.HIGH -> FinikColor.HistoryHigh
        HistoryTone.BAD -> FinikColor.RedHistory
    }

val LogTone.color: Color
    get() = when (this) {
        LogTone.GOOD -> FinikColor.GreenLog
        LogTone.NEUTRAL -> FinikColor.GoldLog
        LogTone.BAD -> FinikColor.RedLog
    }
