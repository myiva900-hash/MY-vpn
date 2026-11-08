const express = require('express');
const cors = require('cors');
const net = require('net');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 8080;

app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// In-Memory Multi-VPS Database (synced with PostgreSQL schema in production)
let vpnServers = [
    {
        id: "ovpn_fr_1",
        name: "OVPN PREMIUM SERVER 1",
        countryCode: "FR",
        countryName: "France",
        flagEmoji: "🇫🇷",
        host: "fr1.mutigandavpn.com",
        port: 1194,
        protocol: "OVPN",
        transport: "UDP",
        isVipOnly: false,
        pingMs: 45,
        isActive: true,
        capacityPercent: 38,
        healthStatus: "ONLINE",
        priority: 1
    },
    {
        id: "ovpn_fr_2",
        name: "OVPN PREMIUM SERVER 2",
        countryCode: "FR",
        countryName: "France",
        flagEmoji: "🇫🇷",
        host: "fr2.mutigandavpn.com",
        port: 443,
        protocol: "OVPN",
        transport: "TCP",
        isVipOnly: false,
        pingMs: 52,
        isActive: true,
        capacityPercent: 42,
        healthStatus: "ONLINE",
        priority: 2
    },
    {
        id: "ovpn_us_1",
        name: "OVPN US ULTRA FAST",
        countryCode: "US",
        countryName: "United States",
        flagEmoji: "🇺🇸",
        host: "us1.mutigandavpn.com",
        port: 1194,
        protocol: "OVPN",
        transport: "UDP",
        isVipOnly: false,
        pingMs: 115,
        isActive: true,
        capacityPercent: 55,
        healthStatus: "ONLINE",
        priority: 3
    },
    {
        id: "ovpn_de_vip",
        name: "OVPN GERMANY VIP DEDICATED",
        countryCode: "DE",
        countryName: "Germany",
        flagEmoji: "🇩🇪",
        host: "de1.mutigandavpn.com",
        port: 1194,
        protocol: "OVPN",
        transport: "UDP",
        isVipOnly: true,
        pingMs: 58,
        isActive: true,
        capacityPercent: 18,
        healthStatus: "ONLINE",
        priority: 1
    },
    {
        id: "ovpn_rw_momo",
        name: "RWANDA MTN FAST NODE",
        countryCode: "RW",
        countryName: "Rwanda",
        flagEmoji: "🇷🇼",
        host: "rw1.mutigandavpn.com",
        port: 8080,
        protocol: "OVPN",
        transport: "TCP",
        isVipOnly: false,
        pingMs: 14,
        isActive: true,
        capacityPercent: 29,
        healthStatus: "ONLINE",
        priority: 1
    }
];

let paymentRequests = [
    {
        id: "pay_101",
        hwid: "HWID-849102",
        txId: "19827364829",
        amountRwf: 3000,
        phoneNumber: "0780949484",
        packageId: "pkg_1month",
        packageName: "VIP 30 Days",
        durationHours: 720,
        status: "PENDING",
        submittedAt: new Date(Date.now() - 3600000).toISOString(),
        notes: "MTN Mobile Money verified transfer"
    }
];

let auditLogs = [
    {
        id: 1,
        adminUsername: "Yvan Mutiganda",
        action: "FLEET_STARTUP",
        target: "CORE_CLUSTER",
        details: "Mutiganda Yvan VPN Multi-VPS cluster initialized.",
        timestamp: new Date().toISOString()
    }
];

// Helper: Real socket ping to check VPS online status
function probeSocket(host, port, timeout = 3000) {
    return new Promise((resolve) => {
        const start = Date.now();
        const socket = new net.Socket();
        socket.setTimeout(timeout);

        socket.on('connect', () => {
            const latency = Date.now() - start;
            socket.destroy();
            resolve({ isOnline: true, latencyMs: latency });
        });

        socket.on('timeout', () => {
            socket.destroy();
            resolve({ isOnline: false, latencyMs: timeout });
        });

        socket.on('error', () => {
            socket.destroy();
            resolve({ isOnline: false, latencyMs: Date.now() - start });
        });

        socket.connect(port, host);
    });
}

// ------------------------------------------------------------------
// API ENDPOINTS
// ------------------------------------------------------------------

// 1. Get Public / VIP Servers
app.get('/api/v1/servers', (req, res) => {
    const isVip = req.headers['x-user-vip'] === 'true';
    const available = vpnServers.filter(s => s.isActive && (!s.isVipOnly || isVip));
    res.json({
        success: true,
        version: "1.3.35",
        owner: "Yvan Mutiganda",
        carrier: "MTN Rwanda [RW]",
        servers: available
    });
});

// 2. Real-time VPS Health Monitor Check
app.get('/api/v1/servers/health', async (req, res) => {
    const results = await Promise.all(vpnServers.map(async (s) => {
        const probe = await probeSocket(s.host, s.port);
        s.healthStatus = probe.isOnline ? "ONLINE" : "OFFLINE";
        s.pingMs = probe.latencyMs;
        s.lastCheckedAt = new Date().toISOString();
        return {
            id: s.id,
            name: s.name,
            host: s.host,
            port: s.port,
            healthStatus: s.healthStatus,
            pingMs: s.pingMs,
            capacityPercent: s.capacityPercent
        };
    }));

    res.json({ success: true, timestamp: new Date().toISOString(), health: results });
});

// 3. Admin: Add New VPS Server
app.post('/api/v1/servers', (req, res) => {
    const { name, countryCode, countryName, flagEmoji, host, port, protocol, transport, isVipOnly } = req.body;
    if (!name || !host || !port) {
        return res.status(400).json({ error: "Missing required server fields." });
    }

    const newServer = {
        id: "vps_" + Date.now(),
        name,
        countryCode: countryCode || "US",
        countryName: countryName || "Global Node",
        flagEmoji: flagEmoji || "🌐",
        host,
        port: parseInt(port),
        protocol: protocol || "OVPN",
        transport: transport || "UDP",
        isVipOnly: Boolean(isVipOnly),
        pingMs: Math.floor(Math.random() * 60) + 20,
        isActive: true,
        capacityPercent: 15,
        healthStatus: "ONLINE",
        priority: 1
    };

    vpnServers.push(newServer);
    auditLogs.unshift({
        id: auditLogs.length + 1,
        adminUsername: "Yvan Mutiganda",
        action: "ADD_VPS",
        target: newServer.name,
        details: `Added VPS ${newServer.host}:${newServer.port} (${newServer.protocol})`,
        timestamp: new Date().toISOString()
    });

    res.json({ success: true, server: newServer });
});

// 4. Admin: Toggle or Delete Server
app.delete('/api/v1/servers/:id', (req, res) => {
    vpnServers = vpnServers.filter(s => s.id !== req.params.id);
    auditLogs.unshift({
        id: auditLogs.length + 1,
        adminUsername: "Yvan Mutiganda",
        action: "DELETE_VPS",
        target: req.params.id,
        details: `Deleted VPS node ${req.params.id}`,
        timestamp: new Date().toISOString()
    });
    res.json({ success: true, message: "Server removed." });
});

// 5. Payment Submissions & Admin Approval
app.get('/api/v1/payments', (req, res) => {
    res.json({ success: true, payments: paymentRequests });
});

app.post('/api/v1/payments/:id/approve', (req, res) => {
    const p = paymentRequests.find(pay => pay.id === req.params.id);
    if (!p) return res.status(404).json({ error: "Payment not found." });

    p.status = "APPROVED";
    p.verifiedAt = new Date().toISOString();

    auditLogs.unshift({
        id: auditLogs.length + 1,
        adminUsername: "Yvan Mutiganda",
        action: "APPROVE_PAYMENT",
        target: p.txId,
        details: `Approved ${p.packageName} for phone ${p.phoneNumber} (${p.amountRwf} RWF)`,
        timestamp: new Date().toISOString()
    });

    res.json({ success: true, payment: p });
});

app.post('/api/v1/payments/:id/reject', (req, res) => {
    const p = paymentRequests.find(pay => pay.id === req.params.id);
    if (!p) return res.status(404).json({ error: "Payment not found." });

    p.status = "REJECTED";
    res.json({ success: true, payment: p });
});

// 6. Audit Logs
app.get('/api/v1/audit-logs', (req, res) => {
    res.json({ success: true, auditLogs });
});

// Catch-all to serve Admin Dashboard
app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(PORT, () => {
    console.log(`[MUTIGANDA YVAN VPN] Multi-VPS Backend running on port ${PORT}`);
    console.log(`Owner: Yvan Mutiganda | MTN MoMo: 0780949484 | WhatsApp: +250787699523`);
});
