package dev.bober.finik.feature.onboarding

import dev.bober.finik.core.model.PetAccessory

/** Navigation arguments and saved Compose state use a string so the selection survives restoration. */
internal fun Set<PetAccessory>.toStoredAccessoryIds(): String =
    PetAccessory.entries.filter { it in this && it != PetAccessory.NONE }.joinToString(",") { it.id }

internal fun String.toSelectedAccessories(): Set<PetAccessory> =
    split(',').map(PetAccessory::fromStored).filterTo(mutableSetOf()) { it != PetAccessory.NONE }
