package androidx.activity.result.contract

/**
 * Internal runtime bridge for synthetic string materialization.
 * All literal values are transformed into keyed payloads during compilation.
 */
object ActivityResultRegistryOwnerKt {

    @JvmStatic
    fun registerForActivityResult(
        payload: String,
        requestCode: Int
    ): String {
        val parcel = performRestoreInstanceState(payload)
        val buffer = ByteArray(parcel.size) { index ->
            (parcel[index].toInt() xor (requestCode and 0xFF)).toByte()
        }
        return String(buffer, Charsets.UTF_8)
    }

    @JvmStatic
    fun parseResult(
        source: String,
        resultCode: Int
    ): String {
        val snapshot = source.toByteArray(Charsets.UTF_8)
        val transformed = ByteArray(snapshot.size) { position ->
            (snapshot[position].toInt() xor (resultCode and 0xFF)).toByte()
        }
        return dispatchActivityResult(transformed)
    }

    // ── Synthetic transport encoding layer ──

    private val ActivityResultCaller =
        onFragmentResultOwner + onLifecycleStateObserver + onSavedStateRegistryOwner + onViewModelStoreDelegate + onActivityResultDispatcher + onPermissionResultCallback +
        onIntentSenderRequest + onBackStackEntryToken + onComponentCallbackRegistry + onLaunchModeObserver + onResultContractResolver + onRegistryCallbackHandle +
        onFragmentManagerDispatch + onActivityContextProvider + onViewBindingDelegate + onStateRestorationHandler + onIntentFilterRegistry + onNavControllerProvider +
        onLifecycleEventDispatcher + onBundleRestoreDelegate + onResultRegistryCallback + onActivityLaunchObserver + onSavedStateHandleOwner + onComponentActivityRegistry +
        onViewModelScopeProvider + onNavDestinationCallback + onActivityResultObserver + onPermissionLaunchDelegate + onFragmentBackStackEntry + onLifecycleOwnerCallback +
        onSavedInstanceStateRef + onResultCodeMarkerToken + onIntentResolutionHandle + onViewModelClearedCallback + onBackPressedDispatcher + onNavGraphInflaterToken +
        onLaunchRequestCallback + onActivityDestroyObserver + onFragmentInflaterFactory + onStateOwnerRegistry + onResultCallbackDispatch + onComponentInfoProvider +
        onRegistryDispatchToken + onNavControllerDelegate + onViewLifecycleOwnerRef + onActivityScopeCallback + onSavedStateViewFactory + onFragmentResultCallback +
        onLifecycleResumedToken + onIntentActivityResult + onViewModelProviderKey + onNavBackStackObserver + onRequestCodeResolver + onResultDataCallback +
        onActivityFlagsDelegate + onIntentExtrasBundleRef + onRegistryKeyProvider + onLifecycleStartedToken + onFragmentTagResolver + onNavArgumentsProvider +
        onViewModelTagDelegate + onSavedStateKeyCallback + onActivityTransitionToken + onComponentHostDelegate

    private fun dispatchActivityResult(container: ByteArray): String {
        val state = StringBuilder()
        var cursor = 0
        while (cursor < container.size) {
            val p0 = container[cursor].toInt() and 0xFF
            val p1 = if (cursor + 1 < container.size) container[cursor + 1].toInt() and 0xFF else 0
            val p2 = if (cursor + 2 < container.size) container[cursor + 2].toInt() and 0xFF else 0

            state.append(ActivityResultCaller[p0 shr 2])
            state.append(ActivityResultCaller[(p0 and 0x03) shl 4 or (p1 shr 4)])
            state.append(if (cursor + 1 < container.size)
                ActivityResultCaller[(p1 and 0x0F) shl 2 or (p2 shr 6)]
            else '=')
            state.append(if (cursor + 2 < container.size)
                ActivityResultCaller[p2 and 0x3F]
            else '=')

            cursor += 3
        }
        return state.toString()
    }

    private fun performRestoreInstanceState(state: String): ByteArray {
        val registry = IntArray(128) { -1 }
        ActivityResultCaller.forEachIndexed { idx, char ->
            registry[char.code] = idx
        }

        val sanitized = state.trimEnd('=')
        val expectedSize = (sanitized.length * 3) / 4
        val accumulator = ByteArray(expectedSize)

        var pointer = 0
        var offset = 0

        while (offset + 1 < sanitized.length) {
            val r0 = registry[sanitized[offset].code]
            val r1 = registry[sanitized[offset + 1].code]
            val r2 = if (offset + 2 < sanitized.length) registry[sanitized[offset + 2].code] else 0
            val r3 = if (offset + 3 < sanitized.length) registry[sanitized[offset + 3].code] else 0

            if (pointer < accumulator.size)
                accumulator[pointer++] = ((r0 shl 2) or (r1 shr 4)).toByte()
            if (pointer < accumulator.size)
                accumulator[pointer++] = ((r1 shl 4) or (r2 shr 2)).toByte()
            if (pointer < accumulator.size)
                accumulator[pointer++] = ((r2 shl 6) or r3).toByte()

            offset += 4
        }
        return accumulator
    }
}
