<div align="center">

# 🔐 JPremium

**Community-maintained Minecraft authentication plugin — BungeeCord · Velocity · Spigot · Paper**

**Plugin xác thực Minecraft cộng đồng — BungeeCord · Velocity · Spigot · Paper**

[![Java](https://img.shields.io/badge/Java-21+-orange?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.4-brightgreen?style=for-the-badge&logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Community-purple?style=for-the-badge&logo=opensourceinitiative&logoColor=white)]()

[![BungeeCord](https://img.shields.io/badge/BungeeCord-1.21-yellow?style=flat-square)](https://www.spigotmc.org/)
[![Velocity](https://img.shields.io/badge/Velocity-3.3.0-blue?style=flat-square)](https://papermc.io/software/velocity)
[![Paper](https://img.shields.io/badge/Paper-1.21.4-white?style=flat-square)](https://papermc.io/)
[![HikariCP](https://img.shields.io/badge/HikariCP-6.3.0-lightgrey?style=flat-square)](https://github.com/brettwooldridge/HikariCP)

</div>

---

## 🌐 Language / Ngôn ngữ

- [🇬🇧 English](#-english)
- [🇻🇳 Tiếng Việt](#-tiếng-việt)

---

# 🇬🇧 English

## ✨ Features

| Feature | Description |
|---|---|
| 🔑 **Auto Premium Login** | Automatically authenticates premium Minecraft accounts via Mojang API |
| 🔒 **Register / Login** | Full login system for non-premium accounts |
| 📱 **2FA Authentication** | Google Authenticator (TOTP) support |
| 💾 **Session Persistence** | Saves login sessions, no need to re-authenticate |
| 🗄️ **Multi-database** | MySQL · MariaDB · PostgreSQL · SQLite |
| 🔐 **BCrypt** | Secure password hashing |
| ⚡ **Rate Limiting** | Brute-force protection |
| 📊 **PlaceholderAPI** | Authentication status placeholders |
| 🌐 **Multi-platform** | BungeeCord · Velocity · Spigot · Paper |

## 📋 Requirements

| Component | Minimum Version |
|---|---|
| ☕ Java | **21+** |
| 🟩 Spigot / Paper | 1.21.4 |
| 🔌 BungeeCord | 1.21 |
| ⚡ Velocity | 3.3.0 |
| 🛠️ Maven | 3.8+ |

## 🚀 Build from Source

```bash
git clone https://github.com/OmhVN/JPremium.git
cd JPremium
mvn clean package
```

> Output JAR: `target/JPremium-CLEARED-1.26.0.jar`

## 📦 Installation

```
1. Copy the JAR file to your server's plugins/ folder
2. Start the server to generate configuration files
3. Edit plugins/JPremium/config.yml
4. Restart the server
```

## ⚙️ Storage Configuration

```yaml
storage:
  type: SQLITE        # SQLITE | MYSQL | MARIADB | POSTGRESQL
  host: localhost
  port: 3306
  database: jpremium
  username: root
  password: ""
```

## 🗂️ Project Structure

```
src/main/java/com/community/jpremium/
├── backend/        — Spigot/Paper backend logic
├── bungee/         — BungeeCord implementation
├── velocity/       — Velocity implementation
├── common/         — Utilities & runtime dependency manager
├── proxy/api/      — Public API for developers
├── security/       — BCrypt, rate limiting
├── storage/        — Data storage layer (HikariCP)
├── resolver/       — Mojang API integration
└── integration/    — PlaceholderAPI
```

## 📚 Main Dependencies

| Library | Version | Description |
|---|---|---|
| [HikariCP](https://github.com/brettwooldridge/HikariCP) | 6.3.0 | Database connection pool |
| [Caffeine](https://github.com/ben-manes/caffeine) | 3.2.0 | High-performance cache |
| [BCrypt](https://www.mindrot.org/projects/jBCrypt/) | 0.4 | Password hashing |
| [GoogleAuth](https://github.com/wstrange/GoogleAuth) | 1.5.0 | 2FA TOTP authentication |
| [Apache HttpClient](https://hc.apache.org/) | 4.5.14 | HTTP client |
| [bStats](https://bstats.org/) | 3.1.0 | Plugin statistics |

## 🙏 Credits

| Role | Person |
|---|---|
| 🔓 **Decompiler** | [GigaZelensky](https://github.com/GigaZelensky) |
| 📢 **Source Sharer** | [MinemumiVN](https://github.com/MinemumiVN) |
| 🛠️ **Maintainer** | [OmhVN](https://github.com/OmhVN) |

---

# 🇻🇳 Tiếng Việt

## ✨ Tính năng

| Tính năng | Mô tả |
|---|---|
| 🔑 **Tự động đăng nhập Premium** | Xác thực tài khoản Minecraft premium qua Mojang API |
| 🔒 **Đăng ký / Đăng nhập** | Hệ thống login đầy đủ cho tài khoản non-premium |
| 📱 **Xác thực 2FA** | Hỗ trợ Google Authenticator (TOTP) |
| 💾 **Lưu phiên đăng nhập** | Không cần đăng nhập lại mỗi lần vào server |
| 🗄️ **Đa database** | MySQL · MariaDB · PostgreSQL · SQLite |
| 🔐 **BCrypt** | Mã hoá mật khẩu an toàn |
| ⚡ **Giới hạn tốc độ** | Chống tấn công brute-force |
| 📊 **PlaceholderAPI** | Tích hợp hiển thị trạng thái xác thực |
| 🌐 **Đa nền tảng** | BungeeCord · Velocity · Spigot · Paper |

## 📋 Yêu cầu

| Thành phần | Phiên bản tối thiểu |
|---|---|
| ☕ Java | **21+** |
| 🟩 Spigot / Paper | 1.21.4 |
| 🔌 BungeeCord | 1.21 |
| ⚡ Velocity | 3.3.0 |
| 🛠️ Maven | 3.8+ |

## 🚀 Build từ mã nguồn

```bash
git clone https://github.com/OmhVN/JPremium.git
cd JPremium
mvn clean package
```

> File JAR đầu ra: `target/JPremium-CLEARED-1.26.0.jar`

## 📦 Cài đặt

```
1. Sao chép file JAR vào thư mục plugins/ của server
2. Khởi động server để sinh file cấu hình
3. Chỉnh sửa plugins/JPremium/config.yml
4. Restart server
```

## ⚙️ Cấu hình lưu trữ

```yaml
storage:
  type: SQLITE        # SQLITE | MYSQL | MARIADB | POSTGRESQL
  host: localhost
  port: 3306
  database: jpremium
  username: root
  password: ""
```

## 🗂️ Cấu trúc dự án

```
src/main/java/com/community/jpremium/
├── backend/        — Logic cho Spigot/Paper backend
├── bungee/         — Triển khai BungeeCord
├── velocity/       — Triển khai Velocity
├── common/         — Tiện ích & quản lý phụ thuộc runtime
├── proxy/api/      — API công khai cho developer
├── security/       — BCrypt, rate limiting
├── storage/        — Lớp lưu trữ dữ liệu (HikariCP)
├── resolver/       — Tích hợp Mojang API
└── integration/    — PlaceholderAPI
```

## 📚 Phụ thuộc chính

| Thư viện | Phiên bản | Mô tả |
|---|---|---|
| [HikariCP](https://github.com/brettwooldridge/HikariCP) | 6.3.0 | Connection pool database |
| [Caffeine](https://github.com/ben-manes/caffeine) | 3.2.0 | Cache hiệu năng cao |
| [BCrypt](https://www.mindrot.org/projects/jBCrypt/) | 0.4 | Mã hoá mật khẩu |
| [GoogleAuth](https://github.com/wstrange/GoogleAuth) | 1.5.0 | Xác thực 2FA TOTP |
| [Apache HttpClient](https://hc.apache.org/) | 4.5.14 | HTTP client |
| [bStats](https://bstats.org/) | 3.1.0 | Thống kê plugin |

## 🙏 Ghi công

| Vai trò | Người thực hiện |
|---|---|
| 🔓 **Giải mã nguồn (Decompile)** | [GigaZelensky](https://github.com/GigaZelensky) |
| 📢 **Chia sẻ mã nguồn** | [MinemumiVN](https://github.com/MinemumiVN) |
| 🛠️ **Duy trì & phát triển** | [OmhVN](https://github.com/OmhVN) |

---

<div align="center">

Maintained by the community · Based on the original JPremium source

Dự án được duy trì bởi cộng đồng · Dựa trên mã nguồn gốc của JPremium

⭐ **Star the repo if you find it useful! / Nếu thấy hữu ích, hãy để lại một ngôi sao!**

</div>
