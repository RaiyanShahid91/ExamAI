package com.ai.studyassistant.ads

import org.junit.Assert.*
import org.junit.Test

class MonetizationPolicyTest {
    @Test fun dailyRefillPreservesEarnedCreditsButDoesNotAccumulateFreeCredits() {
        assertEquals(SearchBalance(4, 2), SearchBalance(3, 1).nextDay())
        assertEquals(SearchBalance(2, 2), SearchBalance(2, 2).nextDay())
        assertEquals(SearchBalance(2, 2), SearchBalance(0, 0).nextDay())
    }
    @Test fun consumeUsesFreeSearchesBeforeEarnedSearches() {
        assertEquals(SearchBalance(3, 1), SearchBalance(4, 2).consume())
        assertEquals(SearchBalance(1, 0), SearchBalance(2, 0).consume())
        assertEquals(SearchBalance(0, 0), SearchBalance(0, 0).consume())
    }
    @Test fun rewardAddsExactlyTheDisclosedTwoSearches() {
        assertEquals(SearchBalance(3, 1), SearchBalance(1, 1).reward())
    }
    @Test fun interstitialsRespectRewardsCooldownAndOtherAds() {
        assertTrue(interstitialEligible(false, false, null, 0))
        assertFalse(interstitialEligible(true, false, null, 0))
        assertFalse(interstitialEligible(false, true, null, 0))
        assertFalse(interstitialEligible(false, false, 100, 180_099))
        assertTrue(interstitialEligible(false, false, 100, 180_100))
    }
}
