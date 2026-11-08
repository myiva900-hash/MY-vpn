# MUTIGANDA YVAN VPN — Production Architecture & Deployment Guide

**Application Name:** MUTIGANDA YVAN VPN  
**Developer & Owner:** Yvan Mutiganda  
**Client Version:** 1.3.35 (Production Ready)  
**Carrier Target:** MTN Rwanda [RW] & Global High-Speed Nodes  

---

## 1. Application Overview & Architecture
MUTIGANDA YVAN VPN is a native Android VPN application engineered with:
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3) with custom Cyberpunk / Dark Futuristic styling (Deep Navy, Neon Cyan, Neon Turquoise, Electric Blue, Neon Purple, and Cyber Red)
- **Engine:** Android `android.net.VpnService` with genuine virtual TUN interface creation (`10.8.0.2/24`), socket protection (`protect(socket)`), MTU handling, DNS routing, and live I/O telemetry
- **Local Persistence:** Android Jetpack Room Database for VPN servers, user subscriptions, custom configs, payment submissions, and connection logs
- **Protocols Supported:**
  1. OpenVPN (UDP/TCP)
  2. WireGuard (Kernel Fast Encryption)
  3. SSH Direct & SSL Tunneling
  4. V2Ray / Xray (VMess, VLESS, WebSocket TLS)
  5. SlowDNS / DNSTT
  6. UDP Hysteria (Congestion-resistant protocol)

---

## 2. Multi-VPS Server Fleet & Real-Time Health Probing
Each authorized VPS server contains:
- **Server ID:** Unique identifier (e.g. `ovpn_fr_1`, `ovpn_de_vip`).
- **Endpoint:** Domain name or IPv4 address.
- **Port & Transport:** `1194` (UDP) or `443`/`8080` (TCP).
- **Protocol:** OpenVPN (`OVPN`), WireGuard (`WG`), SSH, or V2Ray.
- **Health Probe:** Real socket connectivity test measuring millisecond latency (`pingMs`) and online/offline reachability. Never displays fake simulated online states.
- **Capacity & Load:** Real capacity percentage balancing incoming user connections.
- **Priority & Tier:** Free vs. VIP-only server access gating.

### Auto Select Server Scoring Algorithm:
$$Score = (Latency_{ms} \times 0.65) + (Capacity_{\%} \times 0.35)$$
- Excludes offline servers and servers marked disabled by administrators.
- Gated by user VIP tier (free users cannot be routed to VIP-only nodes).

---

## 3. Encrypted Configuration File System (`.myvpn` Format)
For backing up and distributing configurations securely without exposing server credentials:
- **Cipher:** `AES-256-GCM` (Galois/Counter Mode with 128-bit authentication tag).
- **Key Derivation:** `PBKDF2WithHmacSHA256` with `65,536` iterations and a 16-byte cryptographically secure random salt.
- **File Structure:**
  ```
  [0..5]   Magic Header: "MYVPN1" (6 bytes ASCII)
  [6..21]  Salt: 16 random bytes
  [22..33] IV: 12 random bytes
  [34..]   Ciphertext + GCM Tag (16 bytes)
  ```
- **Integrity Validation:** If an incorrect password is typed or the file is modified, decryption immediately fails with `IllegalArgumentException: Failed to decrypt configuration. Incorrect password or file has been tampered with.`
- **Standard Compatibility:** Standard `.ovpn` files are preserved as plain OpenVPN text. Unencrypted files are never falsely claimed to be encrypted.

---

## 4. MTN Mobile Money VIP Payment System
### Verified Merchant Information:
- **Phone Number:** `0780949484`
- **Payment / Merchant Code:** `2238763`
- **Account Name:** `Yvan Mutiganda`
- **USSD Quick Dial:** `*182*8*1*2238763#`

### VIP Packages:
1. **VIP 30 Days (1 Month):** `3,000 RWF` — Unlimited high-speed bandwidth, all European & US servers, zero connection drops, VIP WhatsApp support.
2. **VIP 90 Days (3 Months):** `7,500 RWF` — Ultra-low ping gaming nodes, priority bandwidth allocation, bypass strict firewalls.
3. **VIP Lifetime Pass:** `15,000 RWF` — Permanent access to private VPS nodes, custom SNI & payload tuning, lifetime updates.

---

## 3. Secure Backend API Structure
For production multi-server orchestration, deploy this REST API on your central VPS (e.g., using Node.js/Express, Go, or Python/FastAPI):

### Base URL: `https://api.mutigandavpn.com/v1`

### Endpoints:
#### 1. Server List
- `GET /servers`
  - Response:
    ```json
    {
      "version": "1.3.35",
      "servers": [
        {
          "id": "ovpn_fr_1",
          "name": "OVPN PREMIUM SERVER 1",
          "country": "France",
          "flag": "🇫🇷",
          "host": "fr1.mutigandavpn.com",
          "port": 1194,
          "protocol": "OVPN",
          "isVipOnly": false,
          "pingMs": 45
        }
      ]
    }
    ```

#### 2. Payment Verification
- `POST /payments/submit`
  - Payload:
    ```json
    {
      "hwid": "HWID-849102",
      "txId": "19827364829",
      "amountRwf": 3000,
      "phoneNumber": "0780949484",
      "packageId": "pkg_1month"
    }
    ```
- `POST /payments/admin/verify` (Admin Only: Requires `X-Admin-Pin` header)
  - Approves payment, generates signed JWT voucher or extends account expiry date.

#### 3. Client Telemetry & HWID Auth
- `POST /client/handshake`
  - Validates HWID status, checks VIP expiry, and returns allowed server credentials without ever embedding VPS root passwords into the client APK.

---

## 4. Room Database Schema (Local SQLite)
The database `mutiganda_yvan_vpn.db` contains 5 core tables:
1. `vpn_servers`:
   - `id` (TEXT PRIMARY KEY)
   - `name` (TEXT)
   - `countryCode`, `countryName`, `flagEmoji` (TEXT)
   - `host` (TEXT), `port` (INTEGER), `protocol` (TEXT)
   - `isVipOnly` (BOOLEAN), `pingMs` (INTEGER), `isActive` (BOOLEAN)
2. `payment_records`:
   - `id` (INTEGER PRIMARY KEY AUTOINCREMENT)
   - `txId` (TEXT), `amountRwf` (INTEGER), `phoneNumber` (TEXT)
   - `packageId`, `packageName` (TEXT), `durationHours` (INTEGER)
   - `status` (TEXT: PENDING, APPROVED, REJECTED)
   - `submittedAt`, `verifiedAt` (INTEGER)
3. `user_account`:
   - `hwid` (TEXT PRIMARY KEY)
   - `username`, `phoneNumber` (TEXT)
   - `vipTier` (TEXT), `vipExpiresAt` (INTEGER)
   - `freeRemainingSeconds` (INTEGER)
   - `totalUploadBytes`, `totalDownloadBytes` (INTEGER)
   - `isAdmin` (BOOLEAN)
4. `custom_configs`:
   - Custom imported or user-created profiles (`.myvpn`, SNI, payload headers).
5. `connection_logs`:
   - Real-time logging of socket handshakes, interface state changes, and diagnostics.

---

## 5. Administrator Portal
- **Default Owner PIN:** `780949484` (or `2238763` / `admin2026`)
- **Capabilities:**
  - View all user-submitted MTN Mobile Money payments.
  - One-tap `APPROVE & ACTIVATE` button that automatically credits VIP days to the user's HWID.
  - Add, toggle online/offline, and delete VPN servers in real time.
  - Broadcast system notifications to all connected clients.
  - Monitor user data traffic and telemetry.

---

## 6. Official WhatsApp Customer Support
- **Support Number:** `+250787699523`
- Direct one-tap launch to WhatsApp with prefilled topics:
  - Contact Support
  - Report Connection Problem
  - VIP Payment Support
  - Help Center & Setup Tutorials

---

## 7. How to Build a Release Signed APK
To build the signed production APK for distribution:
```bash
# 1. Clean and build release APK
gradle :app:assembleRelease

# The generated APK will be available at:
# app/build/outputs/apk/release/app-release.apk
```
