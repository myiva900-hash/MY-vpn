package com.example

import com.example.data.local.entities.VpnServerEntity
import com.example.vpn.AutoServerSelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AutoServerSelectorTest {

    private val sampleServers = listOf(
        VpnServerEntity(
            id = "server_offline",
            name = "Offline Node",
            countryCode = "US",
            countryName = "US",
            flagEmoji = "🇺🇸",
            host = "offline.com",
            port = 1194,
            pingMs = 15,
            healthStatus = "OFFLINE",
            isActive = true
        ),
        VpnServerEntity(
            id = "server_vip",
            name = "VIP Germany",
            countryCode = "DE",
            countryName = "Germany",
            flagEmoji = "🇩🇪",
            host = "de.com",
            port = 1194,
            isVipOnly = true,
            pingMs = 25,
            healthStatus = "ONLINE",
            capacityPercent = 10,
            isActive = true
        ),
        VpnServerEntity(
            id = "server_free_fast",
            name = "Free France",
            countryCode = "FR",
            countryName = "France",
            flagEmoji = "🇫🇷",
            host = "fr.com",
            port = 1194,
            isVipOnly = false,
            pingMs = 40,
            healthStatus = "ONLINE",
            capacityPercent = 30,
            isActive = true
        ),
        VpnServerEntity(
            id = "server_free_congested",
            name = "Free High Load",
            countryCode = "RW",
            countryName = "Rwanda",
            flagEmoji = "🇷🇼",
            host = "rw.com",
            port = 1194,
            isVipOnly = false,
            pingMs = 120,
            healthStatus = "ONLINE",
            capacityPercent = 90,
            isActive = true
        )
    )

    @Test
    fun autoSelectFiltersOfflineAndSelectsBestForFreeUser() {
        // Free user cannot access VIP server, offline server is excluded
        val best = AutoServerSelector.selectBestServer(sampleServers, isUserVip = false)
        assertNotNull(best)
        assertEquals("server_free_fast", best?.id)
    }

    @Test
    fun autoSelectAllowsVipServerForVipUser() {
        // VIP user has access to VIP Germany which has lower latency & load
        val best = AutoServerSelector.selectBestServer(sampleServers, isUserVip = true)
        assertNotNull(best)
        assertEquals("server_vip", best?.id)
    }
}
