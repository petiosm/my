package io.github.nullij.ktobfuscate.compiler

import kotlin.random.Random

/**
 * Produces stable obfuscated identifiers.
 * Given the same [seed] and original name, always returns the same mangled name
 * so that multi-pass compilation stays consistent.
 */
class NameMangler(seed: Long) {

    // Confusable-looking alphabet: mix of latin letters that look alike in some fonts,
    // making the decompiled output very hard to read.
    private val alphabet = "lIiOo0QqBbDdRrNnMmWwUuVvYy"
    private val rng = Random(seed)

    // Cache original → mangled so the same name always maps to the same result
    private val cache = mutableMapOf<String, String>()

    fun mangle(original: String): String = cache.getOrPut(original) {
        val len = 8 + rng.nextInt(8) // 8–15 chars
        buildString {
            // First char must be a letter (valid Java identifier start)
            append(alphabet[rng.nextInt(alphabet.length)])
            repeat(len - 1) { append(alphabet[rng.nextInt(alphabet.length)]) }
        }
    }

    /** Returns a random XOR key (1–254, never 0 to keep things non-trivial) */
    fun xorKey(forString: String): Int {
        val r = Random(forString.hashCode().toLong() xor rng.nextLong())
        return 1 + r.nextInt(254)
    }
}
