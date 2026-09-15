pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Finik"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")

// Core: общие модули без бизнес-логики (модели, дизайн-система, навигация, слот 3D-питомца).
include(":core:model")
include(":core:designsystem")
include(":core:navigation")
include(":core:pet")

// Feature: по одному модулю на экран/флоу. Каждый feature зависит только от core-модулей.
include(":feature:onboarding")
include(":feature:home")
include(":feature:plan")
include(":feature:tasks")
include(":feature:shop")
include(":feature:goal")
include(":feature:growth")
include(":feature:profile")
include(":feature:report")
