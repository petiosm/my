package io.github.nullij.ktobfuscate.compiler

object KtObfuscateKeys {
    const val PLUGIN_ID = "com.github.petiosm.ktobfuscate"

    // CLI option keys passed from Gradle plugin → compiler plugin
    const val KEY_ENABLED         = "enabled"
    const val KEY_OBFUSCATE_STRINGS = "obfuscateStrings"
    const val KEY_RENAME_MEMBERS  = "renameMembers"
    const val KEY_SEED            = "seed"
}
