-- MUTIGANDA YVAN VPN — Production Multi-VPS Backend Database Schema
-- Compatible with PostgreSQL, MySQL 8.0, and SQLite3

-- 1. Administrators Table
CREATE TABLE IF NOT EXISTS administrators (
    id VARCHAR(64) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'OWNER', -- OWNER, ADMIN, SUPPORT
    pin_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- 2. Users Table
CREATE TABLE IF NOT EXISTS users (
    hwid VARCHAR(128) PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    phone_number VARCHAR(32) NOT NULL,
    vip_tier VARCHAR(32) NOT NULL DEFAULT 'FREE', -- FREE, VIP, PREMIUM
    vip_expires_at TIMESTAMP NULL,
    free_remaining_seconds INTEGER NOT NULL DEFAULT 43200,
    total_upload_bytes BIGINT NOT NULL DEFAULT 0,
    total_download_bytes BIGINT NOT NULL DEFAULT 0,
    is_banned BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone_number);
CREATE INDEX IF NOT EXISTS idx_users_vip ON users(vip_tier, vip_expires_at);

-- 3. VPN Servers Table (Multi-VPS Fleet)
CREATE TABLE IF NOT EXISTS vpn_servers (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    country_code VARCHAR(8) NOT NULL,
    country_name VARCHAR(64) NOT NULL,
    flag_emoji VARCHAR(16) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    protocol VARCHAR(32) NOT NULL DEFAULT 'OVPN', -- OVPN, WIREGUARD, SSH, V2RAY
    transport VARCHAR(16) NOT NULL DEFAULT 'UDP',  -- UDP, TCP
    is_vip_only BOOLEAN NOT NULL DEFAULT FALSE,
    ping_ms INTEGER NOT NULL DEFAULT 45,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    capacity_percent INTEGER NOT NULL DEFAULT 35,
    health_status VARCHAR(32) NOT NULL DEFAULT 'ONLINE', -- ONLINE, OFFLINE, DEGRADED, CHECKING
    priority INTEGER NOT NULL DEFAULT 1,
    ovpn_config_id VARCHAR(64) NULL,
    description TEXT,
    last_checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_servers_active ON vpn_servers(is_active, is_vip_only);

-- 4. OVPN Configurations Table
CREATE TABLE IF NOT EXISTS ovpn_configs (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    encryption_algorithm VARCHAR(32) DEFAULT 'AES-256-GCM',
    remote_host VARCHAR(255) NOT NULL,
    remote_port INTEGER NOT NULL,
    transport VARCHAR(16) NOT NULL DEFAULT 'UDP',
    raw_content TEXT NOT NULL,
    ca_cert TEXT,
    client_cert TEXT,
    client_key TEXT,
    tls_auth TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Subscriptions Table
CREATE TABLE IF NOT EXISTS subscriptions (
    id VARCHAR(64) PRIMARY KEY,
    hwid VARCHAR(128) NOT NULL REFERENCES users(hwid) ON DELETE CASCADE,
    package_id VARCHAR(64) NOT NULL,
    package_name VARCHAR(100) NOT NULL,
    duration_hours BIGINT NOT NULL,
    starts_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_subs_hwid ON subscriptions(hwid);

-- 6. Payment Requests Table (MTN Mobile Money)
CREATE TABLE IF NOT EXISTS payment_requests (
    id VARCHAR(64) PRIMARY KEY,
    hwid VARCHAR(128) NOT NULL REFERENCES users(hwid) ON DELETE CASCADE,
    tx_id VARCHAR(128) NOT NULL UNIQUE,
    amount_rwf BIGINT NOT NULL,
    phone_number VARCHAR(32) NOT NULL,
    package_id VARCHAR(64) NOT NULL,
    package_name VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    admin_id VARCHAR(64) REFERENCES administrators(id),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL,
    notes TEXT
);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payment_requests(status);
CREATE INDEX IF NOT EXISTS idx_payments_tx ON payment_requests(tx_id);

-- 7. Connection Logs Table
CREATE TABLE IF NOT EXISTS connection_logs (
    id BIGSERIAL PRIMARY KEY,
    hwid VARCHAR(128) REFERENCES users(hwid) ON DELETE CASCADE,
    server_id VARCHAR(64) REFERENCES vpn_servers(id) ON DELETE SET NULL,
    log_level VARCHAR(16) NOT NULL, -- INFO, WARN, ERROR, SUCCESS
    message TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Support Messages Table
CREATE TABLE IF NOT EXISTS support_messages (
    id VARCHAR(64) PRIMARY KEY,
    hwid VARCHAR(128) REFERENCES users(hwid) ON DELETE CASCADE,
    sender_phone VARCHAR(32) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN', -- OPEN, ANSWERED, CLOSED
    admin_reply TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    admin_username VARCHAR(100) NOT NULL,
    action VARCHAR(64) NOT NULL,
    target VARCHAR(150) NOT NULL,
    details TEXT,
    ip_address VARCHAR(64),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed Initial Authorized Administrator: Yvan Mutiganda
INSERT INTO administrators (id, username, email, password_hash, role, pin_hash)
VALUES (
    'admin_yvan',
    'Yvan Mutiganda',
    'yvan0780949484@gmail.com',
    'pbkdf2$65536$mutigandavpn2026', -- Hashed in production
    'OWNER',
    '780949484'
) ON CONFLICT (id) DO NOTHING;
