package io.github.nullij.ktobfuscate.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class KtObfuscateIrExtension(
    private val obfuscateStrings: Boolean,
    private val renameMembers: Boolean,
    private val seed: Long,
    private val messageCollector: MessageCollector = MessageCollector.NONE,
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val mangler = NameMangler(seed)

        val runtimeAvailable = obfuscateStrings && pluginContext.referenceClass(
            ClassId(
                FqName("androidx.activity.result.contract"),
                Name.identifier("ActivityResultRegistryOwnerKt")
            )
        ) != null

        messageCollector.report(
            CompilerMessageSeverity.WARNING,
            "[KtObfuscate] generate() — module=${moduleFragment.name}, obfuscateStrings=$runtimeAvailable, renameMembers=$renameMembers"
        )

        val transformer = KtObfuscateIrTransformer(
            context          = pluginContext,
            mangler          = mangler,
            obfuscateStrings = runtimeAvailable,
            renameMembers    = renameMembers,
            messageCollector = messageCollector,
        )
        moduleFragment.transform(transformer, null)
    }
}