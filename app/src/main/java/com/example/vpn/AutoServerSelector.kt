package com.example.vpn

import com.example.data.local.entities.VpnServerEntity

object AutoServerSelector {

    /**
     * Intelligently selects the best available authorized VPS server.
     * Criteria:
     * 1. Must be enabled (isActive == true).
     * 2. Must not be OFFLINE.
     * 3. Must match user membership permissions:
     *    - If user does not have VIP, filter out VIP-only servers.
     * 4. Multi-factor scoring:
     *    score = (pingMs * 0.65) + (capacityPercent * 0.35)
     *    Lowest score wins!
     * Returns null if no eligible server exists.
     */
    fun selectBestServer(
        servers: List<VpnServerEntity>,
        isUserVip: Boolean
    ): VpnServerEntity? {
        val eligible = servers.filter { server ->
            server.isActive &&
            server.healthStatus != "OFFLINE" &&
            (!server.isVipOnly || isUserVip)
        }

        if (eligible.isEmpty()) {
            // Fallback to any active server if all reporting offline temporarily
            return servers.firstOrNull { it.isActive && (!it.isVipOnly || isUserVip) }
        }

        return eligible.minByOrNull { server ->
            val latencyWeight = server.pingMs.coerceAtLeast(1) * 0.65
            val capacityWeight = server.capacityPercent.coerceIn(0, 100) * 0.35
            latencyWeight + capacityWeight
        }
    }
}
