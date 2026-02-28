package io.github.nullij.ktobfuscate.gradle

import org.gradle.api.provider.Property

/**
 * DSL extension — configure inside your build.gradle.kts:
 *
 * ```kotlin
 * ktObfuscate {
 *     enabled.set(true)
 *     obfuscateStrings.set(true)
 *     renameMembers.set(true)
 *     seed.set(12345L)
 * }
 * ```
 */
abstract class KtObfuscateExtension {
    /** Master switch. Set to false to disable without removing the plugin. Default: true */
    abstract val enabled: Property<Boolean>

    /** Encrypt all string literals — they appear as garbage in decompiled .dex. Default: true */
    abstract val obfuscateStrings: Property<Boolean>

    /**
     * Rename private/internal functions, properties, fields, and local variables
     * to confusable-looking garbage identifiers. Default: true
     */
    abstract val renameMembers: Property<Boolean>

    /**
     * Seed for the name-mangling RNG.  Use a fixed value for reproducible builds.
     * Change the seed to rotate all generated names.  Default: 0xDEADBEEF
     */
    abstract val seed: Property<Long>
}
