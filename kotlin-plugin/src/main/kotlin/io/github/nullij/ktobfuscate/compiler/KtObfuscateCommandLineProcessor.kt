package io.github.nullij.ktobfuscate.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class KtObfuscateCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = KtObfuscateKeys.PLUGIN_ID

    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption(
            optionName   = KtObfuscateKeys.KEY_ENABLED,
            valueDescription = "<true|false>",
            description  = "Master switch — disable without removing the plugin",
            required     = false,
        ),
        CliOption(
            optionName   = KtObfuscateKeys.KEY_OBFUSCATE_STRINGS,
            valueDescription = "<true|false>",
            description  = "Encrypt string literals in bytecode",
            required     = false,
        ),
        CliOption(
            optionName   = KtObfuscateKeys.KEY_RENAME_MEMBERS,
            valueDescription = "<true|false>",
            description  = "Rename private/internal functions, fields, and local variables",
            required     = false,
        ),
        CliOption(
            optionName   = KtObfuscateKeys.KEY_SEED,
            valueDescription = "<long>",
            description  = "RNG seed for name generation — use the same value for reproducible builds",
            required     = false,
        ),
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration,
    ) {
        when (option.optionName) {
            KtObfuscateKeys.KEY_ENABLED          -> configuration.put(KtObfuscateComponentRegistrar.KEY_ENABLED, value.toBoolean())
            KtObfuscateKeys.KEY_OBFUSCATE_STRINGS -> configuration.put(KtObfuscateComponentRegistrar.KEY_OBFUSCATE_STRINGS, value.toBoolean())
            KtObfuscateKeys.KEY_RENAME_MEMBERS   -> configuration.put(KtObfuscateComponentRegistrar.KEY_RENAME_MEMBERS, value.toBoolean())
            KtObfuscateKeys.KEY_SEED             -> configuration.put(KtObfuscateComponentRegistrar.KEY_SEED, value.toLong())
        }
    }
}
