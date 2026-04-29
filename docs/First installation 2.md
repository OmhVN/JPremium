### First installation BungeeCord or Velocity

**Installation on a proxy server (BungeeCord or Velocity)**

1. Download the `JPremium-XXX.jar` file from spigotmc.org or builtbybit.com.
2. Navigate to the `plugins` directory on your proxy server and place the `JPremium-XXX.jar` file there.
3. Start and then stop your proxy server to generate the default JPremium configuration.
4. Open the JPremium configuration file located at `plugins/JPremium/configuration.yml`.
5. Configure the `limboServerNames` and `mainServerNames` options with the appropriate server names. You can find these names in your proxy configuration file.
   * The server names must be listed inside square brackets (`[]`), not in quotation marks!
   * The limbo server is where players must authenticate (/login or /register).
   * The main server is where players are sent after successful authentication.
   * See this reference [diagram image](https://web.archive.org/web/20250831084256/https://raw.githubusercontent.com/Jakubson/JPremiumCleared/refs/heads/master/images/servers.png).
6. If you are using BungeeCord:
   * Set the `ip_forward` option to `true` in the `config.yml` file on your BungeeCord server.
   * Set the `bungeecord` option to `true` in the `spigot.yml` file on all your Spigot servers.
7. If you are using Velocity:
   * Follow instruction how to configure player information forwarding on your Velocity server: [https://docs.papermc.io/velocity/player-information-forwarding](https://web.archive.org/web/20250831084256/https://docs.papermc.io/velocity/player-information-forwarding)
   * If you want to use the legacy mode, you need to set the `accessTokenDisabled` option to `true` in your JPremium configuration files on all your Spigot servers. Velocity or JPremium will not verify data sent to your Spigot servers, making them operate in an insecure mode!
8. Start your proxy server.

**Installation on a back-end server (Spigot or PaperSpigot) - optional installation**

1. Download the `JPremium-XXX.jar` file from spigotmc.org or builtbybit.com.
2. Navigate to the `plugins` directory on your back-end server and place the `JPremium-XXX.jar` file there.
3. Start and then stop your back-end server to generate the default JPremium configuration.
4. Open the JPremium configuration file located at `plugins/JPremium/configuration.yml`.
5. Copy the JPremium access token from the configuration file on your proxy server and paste it into the `accessToken` option in the JPremium configuration on your back-end server.
6. (For limbo servers only) Set the `captchaMapSlot` option to the inventory slot number where JPremium should place the captcha map for the player.
7. Start your back-end server again.

---

### Data conversion

Please backup your database which you want to convert in order not to lose data! Before converting you have to install correctly the latest JPremium version on your proxy server, so please firstly follow instructions on [this wiki page](https://web.archive.org/web/20250831084256/https://github.com/Jakubson/JPremiumCleared/wiki#first-installation). You have to have default tables and columns names in the database from which you convert! Please remember that the converter has limitation what plugins and password hashing are converted!

Currently supported plugins and password hashing algorithms:

* AuthMe with SHA256 and bcrypt.
* LimboAuth with bcrypt.
* LibreLogin with SHA256, SHA512 and bcrypt.
* nLogin with SHA256, SAH512 and bcrypt.

Steps to migrate data:

* Download a converter plugin JAR from [this site](https://web.archive.org/web/20250831084256/https://github.com/Jakubson/JPremiumCleared/tree/master/converter) (use the latest version).
* Navigate to your `plugins` directory on your proxy server. Then place the JAR file in that directory.
* Fully stop and start your proxy server to allow a default configuration to be generated.
* Open the converter configuration file which is located at `/plugins/jpremium-converter/configuration.properties`.
* Fill configuration values according what data you want to convert.
* Start your proxy server and execute `/migrate-data` command from console and wait to finish.

---

### Database issues

First of all, please remember that database connection issues are **never** caused by JPremium. The plugin uses the official driver. Your issues probably are caused by your wrong database configuration or the connection between your database and your proxy server. Here you have the most common solutions fot the most common issues:

1. `Connection refused` - You probably did not enter database credentials in the JPremium configuration or you entered wrong database credentials (an IP address and a port) in the JPremium configuration or your database is not reachable from your proxy.
2. `Access denied for user 'root'@'localhost' (using password: YES)` - You need to grant permission in your database. Please execute these queries in your database: `GRANT ALL PRIVILEGES ON <database>.* TO '<username>'@'%'` (replace `<database>` with your database name and `<username>` with your database username) and then `FLUSH PRIVILEGES`.
3. `Public Key Retrieval is not allowed` - You can fix it by setting the `storageProperties` option in your JPremium configuration file to `[allowPublicKeyRetrieval=true, useSSL=false]`.
4. `Failed to validate connection com.mysql.jdbc.JDBC4Connection@XXX (No operations allowed after connection closed.).` - The `connectionPoolLifetime` option in the JPremium configuration must be less then the `wait_timeout` setting in your database. It does not matter which value you change, so long as the `connectionPoolLifetime` option is less than the `wait_timeout` setting. Remember that the units of each value are different (`connectionPoolLifetime` is in miliseconds, `wait_timeout` is in seconds)! To check the `wait_timeout` setting, execute this query in your database: `SHOW GLOBAL VARIABLES LIKE "wait_timeout"` - to change it, execute: `SET GLOBAL wait_timeout = <new-time>`.

If you get any other errors which are not described above, please do NOT report it to the JPremium author because it is not related with JPremium. If you do not know how to fix it, please google it!

---

### Website registration

JPremium has the feature which allows to players register from your website.

* Download the website from [this site](https://web.archive.org/web/20250831084256/https://github.com/Jakubson/JPremiumCleared/blob/master/assets/Website.zip).
* Open the `Website` directory in the zip file and upload `index.php` and `background.jpg` files into your website.
* Register your website on [Google re-captcha site](https://web.archive.org/web/20250831084256/https://www.google.com/recaptcha/intro/v3.html) to be able to use re-captcha. You have to register re-captcha v2!
* Open the `index.php` file on your website.
* Enter connection data with your database in the storage section, re-captcha data in the Google re-captcha section.
* Enter the same value which you have in `fixedUniqueIds` in your JPremium configuration file on your proxy server in the JPremium section.
* Set the `registerOnWebsite` option to 1 (registration on server and website) or 2 (registration on website only) in your JPremium configuration file on your proxy server.

---

### Fixed unique ids

Since Minecraft 1.8 all player data is stored using UUIDs (Universal Unique Identifier), not using nicknames. It was added to premium players can change nicknames without losing data. An example UUID looks like: d2affeb6-96a8-4c71-b4d6-b79f962b6309. All servers in the online mode use online UUIDs (got from Mojang servers) and all servers in the offline mode use offline UUIDs (generated using a player nickname).

JPremium alters all player UUIDs to avoid UUID collisions. There are three modes:

* `REAL` uses a Mojang's UUID for premium players and offline for cracked players. **Highly recommended.**
* `OFFLINE` derives a UUID from a username.
* `FIXED` generates a random UUID on the first join. Deprecated and should be only used for backwards compatibility.

Check out the table below to understand the differences between each mode of UUID:

| Feature | REAL | OFFLINE | ~~FIXED~~ |
| --- | --- | --- | --- |
| Premium players can change a username | ✅ | ✴️ (\*) | ✅ |
| Premium players can see cosmetics on modified clients (e.g. LunarClient) | ✅ | ❌ | ❌ |
| Players can switch an account type (/premium, /cracked) | ❌ | ✅ | ✅ |
| Cracked players can use a premium username (\*\*) | ✅ | ✅ | ✅ |
| No issues with signatures | ✅ | ❌ | ❌ |

When you use JPremium on the server where players played before installing and you do not use the converter, you have to correctly set `uniqueIdsType` to represent UUIDs which players have. If you have a server in the offline mode, you probably should select the `OFFLINE` mode. If you have a server in the online mode, you probably should select the `REAL` mode.

The real mode requires to automatically register new premium players otherwise they cannot active the premium mode.

If you convert data from others plugins, all migrated players will have UUIDs from these plugins.

If `floodgateSupport` is enabled, JPremium does not touch UUIDs of Bedrock players.

(\*) UUID collisions may occur and it must be resolved manually. This may happen in the following scenerio: a premium player joins with name "Test" and they get UUID "444cf323-978c-3e83-9288-612345bfec67", then they change the username to "Test2", but the UUID says the same. Then a new player joins with name "Test" and UUID collision occur because they should get the same UUID ("444cf323-978c-3e83-9288-612345bfec67") as the previous player. The only solution for that is removing the original player from the JPremium database, but they lose the account!

(\*\*) By default JPremium does not allow cracked player to use a premium nickname. If you want to change it, please follow this steps: [https://github.com/Jakubson/JPremiumCleared/wiki#joining-issues](https://web.archive.org/web/20250831084256/https://github.com/Jakubson/JPremiumCleared/wiki#joining-issues)

---

### Bedrock support

**JPremium supports only premium Bedrock players. It does NOT support cracked Bedrock players!**

When you enable support for Bedrock Edition in JPremium, all Bedrock players will have a bedrock UUID. So, if you have already some Bedrock players on your server and you want to enable support for it, they will lose all data because they will have a new bedrock UUID, not an offline UUID or a fixed UUID as now. Bedrock players also cannot execute any JPremium commands. Bedrock players are automatically registered and logged, so you need to set Floodgate that only premium Bedrock players can join to your server.

* Install correctly GeyserMC **2.0** and Floodgate **2.0** on your server.
* In your Floodgate configuration file, you have to set a prefix in the `username-prefix` option (you **have to** select `*` or `.` and you have to enable that option otherwise nicknames collisions may occur - you **CANNOT** use `a-z`, `A-Z`, `0-9` and `_` because then Java and Bedrock players may have the same nickname!). Then enable the `replace-spaces` option, disable `player-link.enable` option.
* In your JPremium configuration on your proxy server, enable the `floodgateSupport` option.
* Fully restart your proxy server.

---

### Joining issues

If some players cannot join to your server (they is disconnected with 'Invalid session', 'Not authenticated with Minecraft.net' or similar messages and it can't be changed!), those players are probably cracked players with premium nicknames.
You have two solutions for that (choose only one solution!). If you are using the REAL mode, you can only choose the second solution!

**THE VERY IMPORTANT NOTE! YOU CAN ONLY FOLLOW ONE OF THE BELOW POINTS - YOU CAN'T FOLLOW BOTH!**

1. Please set `registerPremiumUsers` to `false` in your JPremium configuration file on your proxy server. Now all new players have to register and login every joining. If new premium players do not want to use the `/login` command, they can execute the `/premium` command to auto login the next time. That soltuion work correctly on if you have FIXED UUIDs or OFFLINE UUIDs in the JPremium.
2. Please set `secondLoginCracked` to `true` in your JPremium configuration file on your proxy server. Now all cracked players, who use premium player nicknames which are not claimed on your server, can join, but their first connection request will be disconnected. Then they can re-join and play normally as a cracked player with a premium nickname.

Please remember that it will not affect cracked players who already joined (or tried to join) to your server. In such cases you need to execute the `/forcecracked <nickname>` command via your proxy console (you need to execute the `/forcecreatepassword <nickname> <any-password>` command before).
If you are using the REAL mode, you need to remove that player using `/forcepurgeuserprofile <nickname>`.

---

### Nickname change issues

If a premium player changed their nickname, but the player has a different account after join to the server (they need to use the `/login` or `/register` command), the player probably changed the nickname to the nickname which was already taken by a cracked player on the server. Firstly make sure that the player who changed the nickname activated a premium mode in the account. Execute the `/forceviewuserprofile <nickname-before-change>` command.

* If the command returns an account **with** a premium id, execute the `/forcepurgeuserprofile <nickname-after-change>` command.
* If the command returns an account **without** a premium id, execute the `/forcemergepremiumuserprofile <nickname-after-change> <nickname-before-change>` command.

---

### Rate limit

If you get `[JPremium]: Could not verify a player due to rate limit! ...` message in your proxy console, it means that your server sent too many requests to the Mojang API and the Mojang API banned your host for a while. Some players will not be able to join to your server during the ban. The Mojang API automatically will unban your host after several minutes. Please do not report it to the JPremium author because they cannot do anything with that.

If you get that during bots attacks, you should install a good anti-bot plugin which detects and disconnect bots on the pre-login state on the proxy server (or faster). Anti-bots which, disconnect bots later than the pre-login state, will not prevent the rate limit!

---

### Two-factor authentication (2FA)

Non-premium players can enable two-factor authentication. During authentication, they will need to enter a code besides their password.

1. Install Google Authenticator application (or any other similar software) on your mobile ([Google Play](https://web.archive.org/web/20250831084256/https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2&hl=en), [App Store](https://web.archive.org/web/20250831084256/https://apps.apple.com/us/app/google-authenticator/id388497605)).
2. Execute `/requestsecondfactor` command and click the message you get on chat and go to the website.
3. Scan the QR code with the installed application.
4. Execute `/activatesecondfactor <your-password> <code-from-application>` command. Two-factor authentication has been activated. Now, you need to use `/login <your-password> <code-from-application>` command to login.

---

### Legacy servers

Since March 2023 (update CLEARED-1.17.0), JPremium requires Java 17 or higher to run. Older Spigot/PaperSpigot versions (e.g., 1.8.x) do not support Java 17, meaning JPremium will not work on these versions. If you want to use JPremium on a server without Java 17, you have two options:

1. **Use a Spigot/PaperSpigot server that supports Java 17.** Older servers that do not support Java 17 are considered insecure and should no longer be used. (Recommended solution)
2. **Install JPremium only on a proxy with Java 17** and do not install it on back-end servers that do not support Java 17. Some JPremium features may be unavailable in this setup. Remember to secure your back-end servers to prevent direct connections without going through your proxy!

---

### HEX colors

HEX colors can be used on Velocity and BungeeCord, but two different formats are in use:

* For Velocity: **&#aabbcc**
* For BungeeCord: **&x&a&a&b&b&c&c**

---

### Difference between BungeeCord & Velocity

1. BungeeCord supports redirecting players to the main server when the server, where they're, goes offline (disconnectRedirection).
2. Velocity supports redirecting players to the main server when the last server isn't reachable.
3. Velocity supports MiniMessage format.

---

### A plugin tried to cancel a signed chat message / A plugin tried to deny a command with signable component(s)

Since version 1.19.1, Minecraft signs player's messages and commands to add compatibility for the chat report system. Velocity wants to keep compatibility with the chat report system and doesn't allow to edit or cancel any player's messages or commands and that's why you get the error. Velocity probably won't change it ([https://github.com/PaperMC/Velocity/issues/804#issuecomment-1200445270](https://web.archive.org/web/20250831084256/https://github.com/PaperMC/Velocity/issues/804#issuecomment-1200445270)), so the only way to forbid a player to send messages or commands before authorization is disconnecting them.

---

### Commands

All player commands do not require any permissions to use, but all staff commands require permissions. You can edit all command aliases by adding add new option to your JPremium configuration file on your proxy server using this format `<command>CommandAliases: [<alias>, <alias>, ...]` (for example: `registerCommandAliases: [reg, r]`).

**Player commands**:

* `/login <password>`
* `/register <new-password>`
* `/unregister <password>`
* `/changepassword <current-password> <new-password>`
* `/createpassword <new-password>`
* `/premium <password>`
* `/cracked <password>`
* `/startsession`
* `/destroysession`
* `/changeemailaddress <password> <email-address>`
* `/requestpasswordrecovery <email-address>`
* `/confirmpasswordrecovery <recovery-code> <new-password>`
* `/requestsecondfactor`
* `/activatesecondfactor <password> <second-factor-code>`
* `/deactivatesecondfactor <password> <second-factor-code>`

**Staff commands**:

* `/forcelogin <nickname>` > `jpremium.command.forcelogin`
* `/forceregister <nickname> <new-password>` > `jpremium.command.forceregister`
* `/forceunregister <nickname>` > `jpremium.command.forceunregister`
* `/forcechangepassword <nickname> <new-password>` > `jpremium.command.forcechangepassword`
* `/forcecreatepassword <nickname> <new-password>` > `jpremium.command.forcecreatepassword`
* `/forcepremium <nickname>` > `jpremium.command.forcepremium`
* `/forcecracked <nickname>` > `jpremium.command.forcecracked`
* `/forcestartsession <nickname>` > `jpremium.command.forcestartsession`
* `/forcedestroysession <nickname>` > `jpremium.command.forcedestroysession`
* `/forcechangeemailaddress <nickname> <email-address>` > `jpremium.command.forcechangeemailaddress`
* `/forcerequestpasswordrecovery <nickname>` > `jpremium.command.forcerequestpasswordrecovery`
* `/forceconfirmpasswordrecovery <nickname> <new-password>` > `jpremium.command.forceconfirmpasswordrecovery`
* `/forcerequestsecondfactor <nickname>` > `jpremium.command.forcerequestsecondfactor`
* `/forceactivatesecondfactor <nickname>` > `jpremium.command.forceactivatesecondfactor`
* `/forcedeactivatesecondfactor <nickname>` > `jpremium.command.forcedeactivatesecondfactor`
* `/forceviewuserprofile <nickname>` > `jpremium.command.forceviewuserprofile`
* `/forcepurgeuserprofile <nickname>` > `jpremium.command.forcepurgeuserprofile`
* `/forcemergepremiumuserprofile <current-nickname> <prefious-nickname>` > `jpremium.command.forcemergepremiumuserprofile`
* `/jreload` > `jpremium.command.reload`

---

### Placeholder API

Due to data synchronization process with back-end servers, placeholder values works only for online players and they are refreshed only after player joining or successful authorization (executed /login, /register, /forcelogin, /forceregister).

* `%jpremium_unique_id%`
* `%jpremium_premium_id%`
* `%jpremium_last_nickname%`
* `%jpremium_hashed_password%`
* `%jpremium_verification_token%`
* `%jpremium_email_address%`
* `%jpremium_session_expires%`
* `%jpremium_last_server%`
* `%jpremium_last_address%`
* `%jpremium_last_seen%`
* `%jpremium_first_address%`
* `%jpremium_first_seen%`
* `%jpremium_captcha_code%`
* `%jpremium_state%` returns: `PREMIUM`, `LOGGED`, `REGISTERED`, `UNREGISTERED` or `UNKNOWN`

---

### Developer API

To obtain an instance of the `App` class on, call `JPremiumApi.getApp();`.

The API works only on the proxy site. JavaDocs: [https://jakubson.github.io/JPremiumCleared/](https://web.archive.org/web/20250831084256/https://jakubson.github.io/JPremiumCleared/)
If you are a developer who does not have access to the full JPremium version, you can use `JPremiumAPI.jar` file: [https://github.com/Jakubson/JPremiumCleared/blob/master/assets/JPremium-API.jar](https://web.archive.org/web/20250831084256/https://github.com/Jakubson/JPremiumCleared/blob/master/assets/JPremium-API.jar)
