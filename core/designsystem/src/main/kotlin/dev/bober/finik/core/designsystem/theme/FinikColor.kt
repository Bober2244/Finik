package dev.bober.finik.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.bober.finik.core.model.HistoryTone
import dev.bober.finik.core.model.LogTone
import dev.bober.finik.core.model.SpendCategory
import dev.bober.finik.core.model.TaskKind

/** Цвета экранов для выбранной системной темы. */
val FinikColor: FinikPalette
    @Composable get() = LocalFinikPalette.current

internal val LocalFinikPalette = staticCompositionLocalOf<FinikPalette> { LightFinikPalette }

/** Цветовые токены, общие для светлой и тёмной палитры. */
interface FinikPalette {

    // ── Фон и поверхности ────────────────────────────────────────────────
    val Background: Color
    val Surface: Color
    val SurfaceMuted: Color
    val SurfaceSoft: Color
    val SurfaceCream: Color
    val Chip: Color
    val ChipTrait: Color
    val ChipKindDay: Color
    val ChipStat: Color
    val IconButton: Color
    val Track: Color
    val Border: Color
    val BorderStrong: Color
    val BorderSoft: Color
    val InputBorder: Color
    val DisabledButton: Color
    val Scrim: Color
    val WhiteGlass: Color
    val WhiteGlassSoft: Color

    // ── Текст ────────────────────────────────────────────────────────────
    val Ink: Color
    val InkDeep: Color
    val Mouth: Color
    val InkBadge: Color
    val Text40: Color
    val Text42: Color
    val Text43: Color
    val Text44: Color
    val Text45: Color
    val Text46: Color
    val Text47: Color
    val Text48: Color
    val Text50: Color
    val Text52: Color
    val Text54: Color
    val Text56: Color
    val Text58: Color
    val Text60: Color
    val TextWarm34: Color
    val TextWarm38: Color
    val TextWarm42: Color
    val TextWarm44: Color
    val Avatar: Color
    val DotInactive: Color
    val QuizKey: Color

    // ── Зелёный (основной акцент) ────────────────────────────────────────
    val Green: Color
    val GreenPressed: Color
    val GreenLink: Color
    val GreenLog: Color
    val GreenStem: Color
    val GreenStageOpen: Color
    val GreenBud: Color
    val GreenJar: Color
    val GreenBadge: Color
    val GreenInk34: Color
    val GreenInk36: Color
    val GreenToast: Color
    val GreenInk38: Color
    val GreenInk40: Color
    val GreenInkLesson: Color
    val GreenInk40s: Color
    val GreenInkQuiz: Color
    val GreenInk42: Color
    val GreenInk42s: Color
    val GreenInk44: Color
    val GreenBorderActive: Color
    val GreenTrack: Color
    val GreenBorderBtn: Color
    val GreenBorderDone: Color
    val GreenGlow: Color
    val GreenChipBonus: Color
    val GreenChipLesson: Color
    val GreenPill: Color
    val GreenChipStage: Color
    val GreenCard: Color
    val GreenSelected: Color
    val GreenHeroTop: Color
    val GreenStat: Color

    // ── Монеты / жёлтый ──────────────────────────────────────────────────
    val Coin: Color
    val CoinBorder: Color
    val CoinChip: Color
    val CoinInk: Color
    val CoinInk36: Color
    val CoinInk34: Color
    val CoinInkStreak: Color
    val CoinInk40: Color
    val CoinInk42: Color
    val CoinInk44: Color
    val CoinInk46: Color
    val CoinInk47: Color
    val CoinInk48: Color
    val Gold: Color
    val GoldLog: Color
    val GoldCard: Color
    val GoldBorder: Color
    val Orange: Color

    // ── Красный ──────────────────────────────────────────────────────────
    val Red: Color
    val RedToast: Color
    val RedSaleInk: Color
    val RedSaleBg: Color
    val RedStat: Color
    val RedCost: Color
    val RedBorderCare: Color
    val RedBorderPlan: Color
    val RedBorderSale: Color
    val RedBorderReset: Color
    val RedIcon: Color
    val RedReset: Color
    val RedLog: Color
    val RedPicked: Color
    val RedPickedBorder: Color
    val RedPickedBg: Color
    val RedHistory: Color
    val RedNoteBg: Color
    val RedNoteInk: Color
    val RedMarker: Color

    // ── Категории и предметы ─────────────────────────────────────────────
    val Food: Color
    val FoodBright: Color
    val FoodPot: Color
    val Water: Color
    val WaterCan: Color
    val Play: Color
    val PlayPot: Color
    val Pot: Color
    val Sound: Color
    val WeekChip: Color
    val WeekInk: Color

    // ── История накоплений ───────────────────────────────────────────────
    val HistoryLow: Color
    val HistoryHigh: Color
    val HistoryMid: Color
}

internal object LightFinikPalette : FinikPalette {

    // ── Фон и поверхности ────────────────────────────────────────────────
    override val Background = Color(0xFFFCFAF4)      // oklch(0.985 0.008 95)
    override val Surface = Color.White
    override val SurfaceMuted = Color(0xFFF8F7F1)    // oklch(0.975 0.008 95) — кнопки −/+
    override val SurfaceSoft = Color(0xFFF7F5EF)     // oklch(0.97 0.008 95) — недоступный уход
    override val SurfaceCream = Color(0xFFFAF7EC)    // oklch(0.975 0.014 95) — низ градиента героя
    override val Chip = Color(0xFFF2F0E9)            // oklch(0.955 0.01 95) — метка задания, неактивный день
    override val ChipTrait = Color(0xFFF2EFE0)       // oklch(0.95 0.02 95)
    override val ChipKindDay = Color(0xFFF4F0E1)     // oklch(0.955 0.02 95)
    override val ChipStat = Color(0xFFF6F2E3)        // oklch(0.96 0.02 95)
    override val IconButton = Color(0xFFEDEBE2)      // oklch(0.94 0.012 95) — квадратные кнопки, трек плана
    override val Track = Color(0xFFEAE8DF)           // oklch(0.93 0.012 95) — трек полосок, линия над навигацией
    override val Border = Color(0xFFE7E5DA)          // oklch(0.92 0.015 95) — обводка карточек
    override val BorderStrong = Color(0xFFE4E1D6)    // oklch(0.91 0.015 95) — обводка кнопок и чипов
    override val BorderSoft = Color(0xFFEBE8DD)      // oklch(0.93 0.015 95) — стадии, достижения
    override val InputBorder = Color(0xFFE1DED3)     // oklch(0.9 0.015 95)
    override val DisabledButton = Color(0xFFF0EEE7)  // oklch(0.95 0.01 95)
    override val Scrim = Color(0x732B221A)           // oklch(0.26 0.02 60 / .45)
    override val WhiteGlass = Color(0xCCFFFFFF)      // #ffffffcc
    override val WhiteGlassSoft = Color(0xB3FFFFFF)  // #ffffffb3

    // ── Текст ────────────────────────────────────────────────────────────
    override val Ink = Color(0xFF2B221A)             // oklch(0.26 0.02 60) — основной
    override val InkDeep = Color(0xFF25170C)         // oklch(0.22 0.03 60) — глаза
    override val Mouth = Color(0xFF372414)           // oklch(0.28 0.04 60)
    override val InkBadge = Color(0xFF30271F)        // oklch(0.28 0.02 60)
    override val Text40 = Color(0xFF50453D)          // oklch(0.40 0.02 60)
    override val Text42 = Color(0xFF564B42)          // oklch(0.42 0.02 60)
    override val Text43 = Color(0xFF584D45)          // oklch(0.43 0.02 60)
    override val Text44 = Color(0xFF5B5048)          // oklch(0.44 0.02 60)
    override val Text45 = Color(0xFF5E534A)          // oklch(0.45 0.02 60)
    override val Text46 = Color(0xFF61564D)          // oklch(0.46 0.02 60)
    override val Text47 = Color(0xFF645850)          // oklch(0.47 0.02 60)
    override val Text48 = Color(0xFF665B53)          // oklch(0.48 0.02 60)
    override val Text50 = Color(0xFF6C6158)          // oklch(0.50 0.02 60) — самый частый вторичный
    override val Text52 = Color(0xFF72665E)          // oklch(0.52 0.02 60)
    override val Text54 = Color(0xFF786C63)          // oklch(0.54 0.02 60)
    override val Text56 = Color(0xFF7E7269)          // oklch(0.56 0.02 60) — «Шаг 1 из 2»
    override val Text58 = Color(0xFF84786F)          // oklch(0.58 0.02 60)
    override val Text60 = Color(0xFF8A7E75)          // oklch(0.60 0.02 60) — зачёркнутая цена
    override val TextWarm34 = Color(0xFF473322)      // oklch(0.34 0.04 60) — цифры дохода
    override val TextWarm38 = Color(0xFF4F3F32)      // oklch(0.38 0.03 60) — «недель с планом»
    override val TextWarm42 = Color(0xFF5A493D)      // oklch(0.42 0.03 60)
    override val TextWarm44 = Color(0xFF5F4F42)      // oklch(0.44 0.03 60)
    override val Avatar = Color(0xFF887769)          // oklch(0.58 0.03 60)
    override val DotInactive = Color(0xFFB2A9A2)     // oklch(0.74 0.015 60)
    override val QuizKey = Color(0xFFBBAEA5)         // oklch(0.76 0.02 60)

    // ── Зелёный (основной акцент) ────────────────────────────────────────
    override val Green = Color(0xFF3B9555)           // oklch(0.60 0.13 150)
    override val GreenPressed = Color(0xFF218041)    // oklch(0.53 0.13 150)
    override val GreenLink = Color(0xFF21763C)       // oklch(0.50 0.12 150)
    override val GreenLog = Color(0xFF298646)        // oklch(0.55 0.13 150)
    override val GreenStem = Color(0xFF3D8E53)       // oklch(0.58 0.12 150)
    override val GreenStageOpen = Color(0xFF218041)  // открытая стадия на светлой карточке
    override val GreenBud = Color(0xFF5CB572)        // oklch(0.70 0.13 150)
    override val GreenJar = Color(0xFF6FB07D)        // oklch(0.70 0.10 150)
    override val GreenBadge = Color(0xFF75B683)      // oklch(0.72 0.10 150)
    override val GreenInk34 = Color(0xFF114320)      // oklch(0.34 0.08 150)
    override val GreenInk36 = Color(0xFF0D4A22)      // oklch(0.36 0.09 150) — активная вкладка
    override val GreenToast = Color(0xFF1E4729)      // oklch(0.36 0.07 150)
    override val GreenInk38 = Color(0xFF1D4E2B)      // oklch(0.38 0.08 150)
    override val GreenInk40 = Color(0xFF295233)      // oklch(0.40 0.07 150)
    override val GreenInkLesson = Color(0xFF1B552C)  // oklch(0.40 0.09 150)
    override val GreenInk40s = Color(0xFF344F39)     // oklch(0.40 0.05 150)
    override val GreenInkQuiz = Color(0xFF2F5136)    // oklch(0.40 0.06 150)
    override val GreenInk42 = Color(0xFF39553F)      // oklch(0.42 0.05 150)
    override val GreenInk42s = Color(0xFF34563B)     // oklch(0.42 0.06 150) — подпись «отложено всего»
    override val GreenInk44 = Color(0xFF3E5B44)      // oklch(0.44 0.05 150)
    override val GreenBorderActive = Color(0xFFC1E1C6) // oklch(0.88 0.05 150)
    override val GreenTrack = Color(0xFFCEE1D1)      // oklch(0.89 0.03 150)
    override val GreenBorderBtn = Color(0xFFD1E4D4)  // oklch(0.90 0.03 150)
    override val GreenBorderDone = Color(0xFFCCE6D0) // oklch(0.90 0.04 150)
    override val GreenGlow = Color(0xFFDCF7E1)       // oklch(0.95 0.04 150) — радиальный градиент welcome
    override val GreenChipBonus = Color(0xFFE3F4E6)  // oklch(0.95 0.025 150)
    override val GreenChipLesson = Color(0xFFE1F5E4) // oklch(0.95 0.03 150)
    override val GreenPill = Color(0xFFDFF6E2)       // oklch(0.95 0.035 150) — активная вкладка
    override val GreenChipStage = Color(0xFFE3F6E6)  // oklch(0.955 0.03 150)
    override val GreenCard = Color(0xFFE4F8E7)       // oklch(0.96 0.03 150)
    override val GreenSelected = Color(0xFFEEFBF0)   // oklch(0.975 0.02 150)
    override val GreenHeroTop = Color(0xFFE2F7E2)    // oklch(0.955 0.035 145)
    override val GreenStat = Color(0xFF154F27)       // oklch(0.38 0.09 150) — цифра «4 статьи» на welcome

    // ── Монеты / жёлтый ──────────────────────────────────────────────────
    override val Coin = Color(0xFFEABE4A)            // oklch(0.82 0.14 88)
    override val CoinBorder = Color(0xFFD29922)      // oklch(0.72 0.14 80)
    override val CoinChip = Color(0xFFFAF0D2)        // oklch(0.955 0.04 90)
    override val CoinInk = Color(0xFF57380F)         // oklch(0.37 0.07 70)
    override val CoinInk36 = Color(0xFF54360B)       // oklch(0.36 0.07 70)
    override val CoinInk34 = Color(0xFF4C3211)       // oklch(0.34 0.06 70)
    override val CoinInkStreak = Color(0xFF4F3005)   // oklch(0.34 0.07 70)
    override val CoinInk40 = Color(0xFF604018)       // oklch(0.40 0.07 70)
    override val CoinInk42 = Color(0xFF5F482E)       // oklch(0.42 0.05 70)
    override val CoinInk44 = Color(0xFF644E33)       // oklch(0.44 0.05 70)
    override val CoinInk46 = Color(0xFF6A5339)       // oklch(0.46 0.05 70)
    override val CoinInk47 = Color(0xFF6D563C)       // oklch(0.47 0.05 70)
    override val CoinInk48 = Color(0xFF6C5A45)       // oklch(0.48 0.04 70)
    override val Gold = Color(0xFFE4B750)            // oklch(0.80 0.13 85) — выполненное достижение
    override val GoldLog = Color(0xFFA1790C)         // oklch(0.60 0.12 85)
    override val GoldCard = Color(0xFFFDF6E4)        // oklch(0.975 0.025 90)
    override val GoldBorder = Color(0xFFF1DFBC)      // oklch(0.91 0.05 85)
    override val Orange = Color(0xFFF6AB6B)          // oklch(0.80 0.12 60) — иконка «Достижения»

    // ── Красный ──────────────────────────────────────────────────────────
    override val Red = Color(0xFF9D3533)             // oklch(0.48 0.14 25)
    override val RedToast = Color(0xFF9D3533)
    override val RedSaleInk = Color(0xFF932B2A)      // oklch(0.45 0.14 25)
    override val RedSaleBg = Color(0xFFFFDCD7)       // oklch(0.94 0.06 25)
    override val RedStat = Color(0xFF7A3430)         // oklch(0.42 0.10 25)
    override val RedCost = Color(0xFFA74541)         // oklch(0.52 0.13 25)
    override val RedBorderCare = Color(0xFFFCE1DE)   // oklch(0.93 0.03 25)
    override val RedBorderPlan = Color(0xFFF8DDDB)   // oklch(0.92 0.03 25)
    override val RedBorderSale = Color(0xFFFEDBD7)   // oklch(0.92 0.04 25)
    override val RedBorderReset = Color(0xFFF8D9D6)  // oklch(0.91 0.035 25)
    override val RedIcon = Color(0xFFE47C75)         // oklch(0.70 0.13 25)
    override val RedReset = Color(0xFF8C2D2B)        // oklch(0.44 0.13 25)
    override val RedLog = Color(0xFFB54A46)          // oklch(0.55 0.14 25)
    override val RedPicked = Color(0xFFC65954)       // oklch(0.60 0.14 25)
    override val RedPickedBorder = Color(0xFFDB6C66) // oklch(0.66 0.14 25)
    override val RedPickedBg = Color(0xFFFFF1EE)     // oklch(0.975 0.025 25)
    override val RedHistory = Color(0xFFF2A7A1)      // oklch(0.80 0.09 25)
    override val RedNoteBg = Color(0xFFFFEEEB)       // oklch(0.97 0.03 25)
    override val RedNoteInk = Color(0xFF822B2A)      // oklch(0.42 0.12 25)
    override val RedMarker = Color(0xFFA43B38)       // oklch(0.50 0.14 25)

    // ── Категории и предметы ─────────────────────────────────────────────
    override val Food = Color(0xFFE5946F)            // oklch(0.74 0.11 45)
    override val FoodBright = Color(0xFFEB9A75)      // oklch(0.76 0.11 45)
    override val FoodPot = Color(0xFFE9A679)         // oklch(0.78 0.10 55)
    override val Water = Color(0xFF49ABD6)           // oklch(0.70 0.11 230)
    override val WaterCan = Color(0xFF3BA9BA)        // oklch(0.68 0.10 210)
    override val Play = Color(0xFFBA8AC5)            // oklch(0.70 0.10 320)
    override val PlayPot = Color(0xFFAE96DA)         // oklch(0.72 0.10 300)
    override val Pot = Color(0xFFDC9A6C)             // oklch(0.74 0.10 55)
    override val Sound = Color(0xFF56C4CA)           // oklch(0.76 0.10 200)
    override val WeekChip = Color(0xFFE1F0FF)        // oklch(0.95 0.03 255)
    override val WeekInk = Color(0xFF234E82)         // oklch(0.42 0.10 255)

    // ── История накоплений ───────────────────────────────────────────────
    override val HistoryLow = Color(0xFF93C69D)      // oklch(0.78 0.08 150)
    override val HistoryHigh = Color(0xFF69B27A)     // oklch(0.70 0.11 150)
    override val HistoryMid = Color(0xFF48A260)      // oklch(0.64 0.13 150)
}

internal object DarkFinikPalette : FinikPalette {

    // ── Фон и поверхности ────────────────────────────────────────────────
    override val Background = Color(0xFF151B17)
    override val Surface = Color(0xFF202922)
    override val SurfaceMuted = Color(0xFF28342C)
    override val SurfaceSoft = Color(0xFF263129)
    override val SurfaceCream = Color(0xFF253129)
    override val Chip = Color(0xFF29352C)
    override val ChipTrait = Color(0xFF303A2D)
    override val ChipKindDay = Color(0xFF30382D)
    override val ChipStat = Color(0xFF303B2F)
    override val IconButton = Color(0xFF2C382F)
    override val Track = Color(0xFF5D735E)
    override val Border = Color(0xFF607A64)
    override val BorderStrong = Color(0xFF718B75)
    override val BorderSoft = Color(0xFF607A64)
    override val InputBorder = Color(0xFF718B75)
    override val DisabledButton = Color(0xFF303A32)
    override val Scrim = Color(0xB3000000)
    override val WhiteGlass = Color(0xE026382C)
    override val WhiteGlassSoft = Color(0xC02B3C30)

    // ── Текст ────────────────────────────────────────────────────────────
    override val Ink = Color(0xFFF6F5EC)
    override val InkDeep = Color(0xFF132219)
    override val Mouth = Color(0xFF372414)
    override val InkBadge = Color(0xFFF3F2E8)
    override val Text40 = Color(0xFFE6EBE1)
    override val Text42 = Color(0xFFD9E2D7)
    override val Text43 = Color(0xFFD4DED2)
    override val Text44 = Color(0xFFCBD7CA)
    override val Text45 = Color(0xFFC5D1C3)
    override val Text46 = Color(0xFFBCCAB9)
    override val Text47 = Color(0xFFB9C7B5)
    override val Text48 = Color(0xFFB5C3B3)
    override val Text50 = Color(0xFFADBCAA)
    override val Text52 = Color(0xFFA9B8A5)
    override val Text54 = Color(0xFFA4B5A2)
    override val Text56 = Color(0xFFA2B29E)
    override val Text58 = Color(0xFF9EAD9A)
    override val Text60 = Color(0xFFA0AD9A)
    override val TextWarm34 = Color(0xFFF3DEC3)
    override val TextWarm38 = Color(0xFFEBD7BD)
    override val TextWarm42 = Color(0xFFDCCAB5)
    override val TextWarm44 = Color(0xFFD4C3B0)
    override val Avatar = Color(0xFFBAA48F)
    override val DotInactive = Color(0xFF859787)
    override val QuizKey = Color(0xFF647865)

    // ── Зелёный (основной акцент) ────────────────────────────────────────
    override val Green = Color(0xFF73CE90)
    override val GreenPressed = Color(0xFF9DE8AE)
    override val GreenLink = Color(0xFFA4E7B7)
    override val GreenLog = Color(0xFF7CDA99)
    override val GreenStem = Color(0xFF5AAB71)
    override val GreenStageOpen = Color(0xFF76D78F)
    override val GreenBud = Color(0xFF71C58A)
    override val GreenJar = Color(0xFF62A87A)
    override val GreenBadge = Color(0xFF83CD94)
    override val GreenInk34 = Color(0xFFC4F4CE)
    override val GreenInk36 = Color(0xFFBBF1C6)
    override val GreenToast = Color(0xFF1F5530)
    override val GreenInk38 = Color(0xFFB8EEC4)
    override val GreenInk40 = Color(0xFFB2E8BD)
    override val GreenInkLesson = Color(0xFFB2E8BD)
    override val GreenInk40s = Color(0xFFAADDB5)
    override val GreenInkQuiz = Color(0xFFBAE8C3)
    override val GreenInk42 = Color(0xFFA9DCB3)
    override val GreenInk42s = Color(0xFFA9DCB3)
    override val GreenInk44 = Color(0xFFA5D4AE)
    override val GreenBorderActive = Color(0xFF62AA75)
    override val GreenTrack = Color(0xFF647D67)
    override val GreenBorderBtn = Color(0xFF528361)
    override val GreenBorderDone = Color(0xFF67A276)
    override val GreenGlow = Color(0xFF193A27)
    override val GreenChipBonus = Color(0xFF2A4731)
    override val GreenChipLesson = Color(0xFF284431)
    override val GreenPill = Color(0xFF284731)
    override val GreenChipStage = Color(0xFF1D3324)
    override val GreenCard = Color(0xFF233D2B)
    override val GreenSelected = Color(0xFF27442E)
    override val GreenHeroTop = Color(0xFF264631)
    override val GreenStat = Color(0xFFB5EEBE)

    // ── Монеты / жёлтый ──────────────────────────────────────────────────
    override val Coin = Color(0xFFF2C760)
    override val CoinBorder = Color(0xFFBA8430)
    override val CoinChip = Color(0xFF3B3421)
    override val CoinInk = Color(0xFFFFEAB3)
    override val CoinInk36 = Color(0xFFFFE6A1)
    override val CoinInk34 = Color(0xFFFFF0C5)
    override val CoinInkStreak = Color(0xFF442B0C)
    override val CoinInk40 = Color(0xFFF6DC91)
    override val CoinInk42 = Color(0xFFEBD49D)
    override val CoinInk44 = Color(0xFFE2CCA0)
    override val CoinInk46 = Color(0xFFDBC59E)
    override val CoinInk47 = Color(0xFFD7C39F)
    override val CoinInk48 = Color(0xFFD4C19E)
    override val Gold = Color(0xFFE4B750)
    override val GoldLog = Color(0xFFDCC46D)
    override val GoldCard = Color(0xFF3B3322)
    override val GoldBorder = Color(0xFF947E57)
    override val Orange = Color(0xFFF6AB6B)

    // ── Красный ──────────────────────────────────────────────────────────
    override val Red = Color(0xFFF3A099)
    override val RedToast = Color(0xFF8C3732)
    override val RedSaleInk = Color(0xFFFFD1C8)
    override val RedSaleBg = Color(0xFF4A2C2A)
    override val RedStat = Color(0xFFF1B5A8)
    override val RedCost = Color(0xFFF3A099)
    override val RedBorderCare = Color(0xFFAA6660)
    override val RedBorderPlan = Color(0xFFAA6660)
    override val RedBorderSale = Color(0xFFAA6660)
    override val RedBorderReset = Color(0xFFAA6660)
    override val RedIcon = Color(0xFFE47C75)
    override val RedReset = Color(0xFFF5B2A9)
    override val RedLog = Color(0xFFE38D88)
    override val RedPicked = Color(0xFFC4675E)
    override val RedPickedBorder = Color(0xFFDC7A71)
    override val RedPickedBg = Color(0xFF472B28)
    override val RedHistory = Color(0xFFF2A7A1)
    override val RedNoteBg = Color(0xFF382725)
    override val RedNoteInk = Color(0xFFFFC9C1)
    override val RedMarker = Color(0xFFD97068)

    // ── Категории и предметы ─────────────────────────────────────────────
    override val Food = Color(0xFFE5946F)
    override val FoodBright = Color(0xFFEB9A75)
    override val FoodPot = Color(0xFFE9A679)
    override val Water = Color(0xFF65BCE0)
    override val WaterCan = Color(0xFF55BDD0)
    override val Play = Color(0xFFCDA0D7)
    override val PlayPot = Color(0xFFBEA7E8)
    override val Pot = Color(0xFFDC9A6C)
    override val Sound = Color(0xFF70D5DA)
    override val WeekChip = Color(0xFF263A50)
    override val WeekInk = Color(0xFFB9D9FF)

    // ── История накоплений ───────────────────────────────────────────────
    override val HistoryLow = Color(0xFF93C69D)
    override val HistoryHigh = Color(0xFF69B27A)
    override val HistoryMid = Color(0xFF48A260)
}

val SpendCategory.color: Color
    @Composable get() = when (this) {
        SpendCategory.FOOD -> FinikColor.Food
        SpendCategory.WATER -> FinikColor.Water
        SpendCategory.PLAY -> FinikColor.Play
        SpendCategory.SAVE -> FinikColor.Green
    }

val TaskKind.chipBackground: Color
    @Composable get() = when (this) {
        TaskKind.WEEK -> FinikColor.WeekChip
        TaskKind.LESSON -> FinikColor.GreenChipLesson
        else -> FinikColor.ChipKindDay
    }

val TaskKind.chipInk: Color
    @Composable get() = when (this) {
        TaskKind.WEEK -> FinikColor.WeekInk
        TaskKind.LESSON -> FinikColor.GreenInkLesson
        else -> FinikColor.Text45
    }

val HistoryTone.color: Color
    @Composable get() = when (this) {
        HistoryTone.LOW -> FinikColor.HistoryLow
        HistoryTone.MID -> FinikColor.HistoryMid
        HistoryTone.HIGH -> FinikColor.HistoryHigh
        HistoryTone.BAD -> FinikColor.RedHistory
    }

val LogTone.color: Color
    @Composable get() = when (this) {
        LogTone.GOOD -> FinikColor.GreenLog
        LogTone.NEUTRAL -> FinikColor.GoldLog
        LogTone.BAD -> FinikColor.RedLog
    }
