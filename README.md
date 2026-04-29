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
[

![Discord](https://img.shields.io/discord/1276836061573546076?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)

](https://discord.gg/minemumivn-1276836061573546076)

</div>

---

## 📋 Yêu cầu

| Thành phần | Phiên bản tối thiểu |
|---|---|
|  Java | **21+** |
|  Spigot / Paper | 1.21.4 |
|  BungeeCord | 1.21 |
|  Velocity | 3.3.0 |
|  Maven | 3.8+ |

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

## 🙏 Ghi công

| Vai trò | Người thực hiện |
|---|---|
| 🔓 **Giải mã nguồn (Decompile)** | [GigaZelensky](https://github.com/GigaZelensky) |
| 📢 **Chia sẻ mã nguồn** | [MinemumiVN](https://github.com/MinemumiVN) |

---

<div align="center">

Maintained by the community · Based on the original JPremium source

Dự án được duy trì bởi cộng đồng · Dựa trên mã nguồn gốc của JPremium

⭐ **Star the repo if you find it useful! / Nếu thấy hữu ích, hãy để lại một ngôi sao!**

</div>
