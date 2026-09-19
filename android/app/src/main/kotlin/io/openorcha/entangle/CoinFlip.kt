package io.openorcha.entangle

import kotlin.random.Random

// Toy model. See content/lessons.json -> coinFlip.disclaimer.

enum class FlipMode { ENTANGLED, CLASSICAL }

enum class Face(val label: String) { HEADS("Heads"), TAILS("Tails") }

data class FlipResult(val a: Face, val b: Face) {
    val agreed: Boolean get() = a == b
}

data class Tally(val flips: Int = 0, val agreements: Int = 0) {
    val rate: Double get() = if (flips == 0) 0.0 else agreements.toDouble() / flips

    fun record(result: FlipResult) = Tally(flips + 1, agreements + if (result.agreed) 1 else 0)
}

object CoinFlipModel {
    private fun face(random: Random) = if (random.nextBoolean()) Face.HEADS else Face.TAILS

    /** One measurement of the pair. `random` is injectable for tests. */
    fun flipPair(mode: FlipMode, random: Random = Random.Default): FlipResult = when (mode) {
        // One shared random outcome: the two coins always agree.
        FlipMode.ENTANGLED -> face(random).let { FlipResult(it, it) }
        // Two independent flips: they agree about half the time.
        FlipMode.CLASSICAL -> FlipResult(face(random), face(random))
    }
}
