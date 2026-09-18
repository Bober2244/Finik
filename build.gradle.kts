// Top-level build file. Плагины объявляются здесь с apply false,
// чтобы convention-плагины из build-logic могли применять их в модулях.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}
