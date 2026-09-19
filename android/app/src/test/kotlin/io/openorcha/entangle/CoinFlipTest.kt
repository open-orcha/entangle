package io.openorcha.entangle

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoinFlipTest {
    @Test
    fun entangledModeAlwaysAgrees() {
        repeat(200) { assertTrue(CoinFlipModel.flipPair(FlipMode.ENTANGLED).agreed) }
    }

    @Test
    fun classicalModeUsesTwoIndependentDraws() {
        // Seeded so the two draws differ: the pair must not be forced to agree.
        val seeds = (0 until 50).map { Random(it) }
        val results = seeds.map { CoinFlipModel.flipPair(FlipMode.CLASSICAL, it) }
        assertTrue(results.any { !it.agreed })
        assertTrue(results.any { it.agreed })
    }

    @Test
    fun tallyTracksAgreementRate() {
        val t = Tally().record(FlipResult(Face.HEADS, Face.HEADS)).record(FlipResult(Face.HEADS, Face.TAILS))
        assertEquals(Tally(flips = 2, agreements = 1), t)
        assertEquals(0.5, t.rate)
    }
}
