package io.github.nullij.ktobfuscate.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@OptIn(ExperimentalCompilerApi::class)
class KtObfuscateComponentRegistrar : CompilerPluginRegistrar() {

    override val pluginId: String = "io.github.nullij.ktobfuscate"

    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val enabled = configuration[KEY_ENABLED] ?: true
        if (!enabled) return

        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)
            ?: MessageCollector.NONE

        messageCollector.report(
            CompilerMessageSeverity.WARNING,
            "[KtObfuscate] Plugin active — strings=${configuration[KEY_OBFUSCATE_STRINGS]}, members=${configuration[KEY_RENAME_MEMBERS]}"
        )

        IrGenerationExtension.registerExtension(
            KtObfuscateIrExtension(
                obfuscateStrings = configuration[KEY_OBFUSCATE_STRINGS] ?: true,
                renameMembers    = configuration[KEY_RENAME_MEMBERS]    ?: true,
                seed             = configuration[KEY_SEED]              ?: 0xDEADBEEFL,
                messageCollector = messageCollector,
            )
        )
    }

    companion object {
        val KEY_ENABLED           = CompilerConfigurationKey<Boolean>(KtObfuscateKeys.KEY_ENABLED)
        val KEY_OBFUSCATE_STRINGS = CompilerConfigurationKey<Boolean>(KtObfuscateKeys.KEY_OBFUSCATE_STRINGS)
        val KEY_RENAME_MEMBERS    = CompilerConfigurationKey<Boolean>(KtObfuscateKeys.KEY_RENAME_MEMBERS)
        val KEY_SEED              = CompilerConfigurationKey<Long>(KtObfuscateKeys.KEY_SEED)
    }
}