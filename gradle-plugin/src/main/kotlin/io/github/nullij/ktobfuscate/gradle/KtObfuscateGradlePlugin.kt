package io.github.nullij.ktobfuscate.gradle

import io.github.nullij.ktobfuscate.compiler.KtObfuscateKeys
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

/**
 * Gradle plugin that wires the KtObfuscate compiler plugin into every Kotlin compilation.
 *
 * Apply in build.gradle.kts:
 *
 * ```kotlin
 * plugins {
 *     id("io.github.nullij.ktobfuscate") version "1.0.0"
 * }
 *
 * // optional — all defaults are sensible
 * ktObfuscate {
 *     enabled.set(true)
 *     obfuscateStrings.set(true)
 *     renameMembers.set(true)
 *     seed.set(0xDEADBEEFL)
 * }
 *
 * dependencies {
 *     // Required — the runtime string decoder
 *     implementation("io.github.nullij.ktobfuscate:runtime:1.0.0")
 * }
 * ```
 */
class KtObfuscateGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        val ext = target.extensions.create("ktObfuscate", KtObfuscateExtension::class.java)
        // Set defaults
        ext.enabled.convention(true)
        ext.obfuscateStrings.convention(true)
        ext.renameMembers.convention(true)
        ext.seed.convention(0xDEADBEEFL)
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

    override fun getCompilerPluginId(): String = KtObfuscateKeys.PLUGIN_ID

    /**
     * Points to the kotlin-plugin artifact that the Kotlin toolchain should load.
     * When publishing to Maven, this must match the actual published coordinates.
     */
    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId    = "io.github.nullij.ktobfuscate",
        artifactId = "kotlin-plugin",
        version    = "1.0.0",
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val ext = project.extensions.getByType(KtObfuscateExtension::class.java)

        return project.provider {
            listOf(
                SubpluginOption(KtObfuscateKeys.KEY_ENABLED,           ext.enabled.get().toString()),
                SubpluginOption(KtObfuscateKeys.KEY_OBFUSCATE_STRINGS, ext.obfuscateStrings.get().toString()),
                SubpluginOption(KtObfuscateKeys.KEY_RENAME_MEMBERS,    ext.renameMembers.get().toString()),
                SubpluginOption(KtObfuscateKeys.KEY_SEED,              ext.seed.get().toString()),
            )
        }
    }
}
