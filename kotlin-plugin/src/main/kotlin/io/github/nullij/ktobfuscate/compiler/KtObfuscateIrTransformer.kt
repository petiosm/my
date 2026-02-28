package io.github.nullij.ktobfuscate.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.ir.util.hasAnnotation

@OptIn(UnsafeDuringIrConstructionAPI::class)
class KtObfuscateIrTransformer(
    private val context: IrPluginContext,
    private val mangler: NameMangler,
    private val obfuscateStrings: Boolean,
    private val renameMembers: Boolean,
    private val messageCollector: MessageCollector = MessageCollector.NONE,
) : IrElementTransformerVoid() {

    private var insideFieldInitializer = false
    private var constRequiredDepth = 0

    private val runtimeClass by lazy {
        val classId = ClassId(
            FqName("androidx.activity.result.contract"),
            Name.identifier("ActivityResultRegistryOwnerKt")
        )
        context.referenceClass(classId)
            ?: error("[KtObfuscate] Cannot find runtime on classpath.")
    }

    private val runtimeDecodeFunction by lazy {
        runtimeClass.functions.single { it.owner.name.asString() == "registerForActivityResult" }
    }

    // ── Field tracking ────────────────────────────────────────────────────────

    override fun visitField(declaration: IrField): IrStatement {
        if (renameMembers && declaration.shouldRenameField()) {
            declaration.name = Name.identifier(mangler.mangle(declaration.name.asString()))
        }

        if (declaration.correspondingPropertySymbol?.owner?.isConst == true) {
            return declaration
        }

        val initializer = declaration.initializer
        if (initializer != null) {
            val prev = insideFieldInitializer
            insideFieldInitializer = true
            initializer.transform(this, null)
            insideFieldInitializer = prev
            declaration.annotations.forEach { it.transform(this, null) }
            return declaration
        }

        return super.visitField(declaration)
    }

    // ── Annotation tracking — annotation args must be compile-time constants ──

    override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
        // Annotation constructor calls require const args — never obfuscate inside them
        constRequiredDepth++
        val result = super.visitConstructorCall(expression)
        constRequiredDepth--
        return result
    }

    // ── Call tracking ─────────────────────────────────────────────────────────

    override fun visitCall(expression: IrCall): IrExpression {
        val ownerName = expression.symbol.owner.name.asString()
        val parentFqName = expression.symbol.owner.parent.let {
            when (it) {
                is IrClass -> it.fqNameWhenAvailable?.asString() ?: ""
                is IrFile  -> it.packageFqName.asString()
                else       -> ""
            }
        }

        val isConstRequired =
            parentFqName.startsWith("androidx.compose.runtime") ||
            parentFqName.startsWith("androidx.compose.ui") ||
            ownerName == "sourceInformationMarkerStart" ||
            ownerName == "sourceInformationMarkerEnd" ||
            ownerName == "sourceInformation" ||
            ownerName == "traceEventStart" ||
            ownerName == "traceEventEnd" ||
            ownerName == "isTraceInProgress"

        return if (isConstRequired) {
            constRequiredDepth++
            val result = super.visitCall(expression)
            constRequiredDepth--
            result
        } else {
            super.visitCall(expression)
        }
    }

    // ── String obfuscation ────────────────────────────────────────────────────

    override fun visitConst(expression: IrConst): IrExpression {
        if (!obfuscateStrings) return super.visitConst(expression)
        if (insideFieldInitializer) return super.visitConst(expression)
        if (constRequiredDepth > 0) return super.visitConst(expression)
        if (expression.type != context.irBuiltIns.stringType) return super.visitConst(expression)

        val original = expression.value as? String ?: return super.visitConst(expression)
        if (original.isEmpty()) return super.visitConst(expression)

        // Skip Compose compiler-generated source info strings
        if (original.startsWith("C(") || original.startsWith("CC(") || original.startsWith("P(")) return super.visitConst(expression)
        if (original.contains(".kt#")) return super.visitConst(expression)

        messageCollector.report(
            CompilerMessageSeverity.WARNING,
            "[KtObfuscate] OBFUSCATING: '$original'"
        )

        val key = mangler.xorKey(original)
        val encoded = encodeString(original, key)
        val so = expression.startOffset
        val eo = expression.endOffset

        val objectType = IrSimpleTypeImpl(runtimeClass, false, emptyList(), emptyList())

        val call = IrCallImpl(
            startOffset        = so,
            endOffset          = eo,
            type               = context.irBuiltIns.stringType,
            symbol             = runtimeDecodeFunction,
            typeArgumentsCount = 0,
            origin             = null,
        )
        call.arguments[0] = IrGetObjectValueImpl(
            startOffset = so,
            endOffset   = eo,
            type        = objectType,
            symbol      = runtimeClass,
        )
        call.arguments[1] = IrConstImpl.string(so, eo, context.irBuiltIns.stringType, encoded)
        call.arguments[2] = IrConstImpl.int(so, eo, context.irBuiltIns.intType, key)

        return call
    }

    // ── Member renaming ───────────────────────────────────────────────────────

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        if (renameMembers && declaration.shouldRename()) {
            declaration.name = Name.identifier(mangler.mangle(declaration.name.asString()))
        }
        return super.visitSimpleFunction(declaration)
    }

    override fun visitVariable(declaration: IrVariable): IrStatement {
        if (renameMembers) {
            declaration.name = Name.identifier(mangler.mangle(declaration.name.asString()))
        }
        return super.visitVariable(declaration)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun IrSimpleFunction.shouldRename(): Boolean {
        if (overriddenSymbols.isNotEmpty()) return false
        if (isFakeOverride) return false
        if (isInline) return false
        // Never rename functions with @JvmName — the JNI symbol is pinned to that name
        if (hasAnnotation(FqName("kotlin.jvm.JvmName"))) return false
        // Never rename functions with @Keep — explicit opt-out from obfuscation
        if (hasAnnotation(FqName("androidx.annotation.Keep"))) return false
        val vis = visibility.name
        return vis == "private" || vis == "internal" || vis == "local"
    }

    private fun IrField.shouldRenameField(): Boolean {
        if (hasAnnotation(FqName("kotlin.jvm.JvmName"))) return false
        if (hasAnnotation(FqName("androidx.annotation.Keep"))) return false
        val vis = visibility.name
        return vis == "private" || vis == "internal"
    }

    private fun encodeString(plain: String, key: Int): String {
        val bytes = plain.toByteArray(Charsets.UTF_8)
        val xored = ByteArray(bytes.size) { i -> (bytes[i].toInt() xor (key and 0xFF)).toByte() }
        return base64Encode(xored)
    }

    private val b64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    private fun base64Encode(data: ByteArray): String {
        val sb = StringBuilder()
        var i = 0
        while (i < data.size) {
            val b0 = data[i].toInt() and 0xFF
            val b1 = if (i + 1 < data.size) data[i + 1].toInt() and 0xFF else 0
            val b2 = if (i + 2 < data.size) data[i + 2].toInt() and 0xFF else 0
            sb.append(b64[b0 shr 2])
            sb.append(b64[(b0 and 0x03) shl 4 or (b1 shr 4)])
            sb.append(if (i + 1 < data.size) b64[(b1 and 0x0F) shl 2 or (b2 shr 6)] else '=')
            sb.append(if (i + 2 < data.size) b64[b2 and 0x3F] else '=')
            i += 3
        }
        return sb.toString()
    }
}