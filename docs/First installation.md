### First installation BungeeCord/Waterfall/Velocity

**Installation on a proxy server (BungeeCord/Waterfall/Velocity)**

1. Download a `JPremium-XXX.jar` file from spigotmc.org or builtbybit.com.
2. Navigate to your `plugins` directory on your proxy server and then place the `JPremium-XXX.jar` file in that directory.
3. Fully start and stop your proxy server to allow default JPremium configurations to be generated.
4. Open the JPremium configuration file which is located at `plugins/JPremium/configuration.yml`.
5. Enter limbo and main server names in `limboServerNames` and `mainServerNames` options. All server names, you can find in the `config.yml` or `config.toml` file in the directory where you have your proxy server. **They are stored in a list, so they have to be between parenthesis - `[]`, not apostrophes!** The limbo server is the server where players need to authorize. The main server is the server where players go after success authorization. Look at these photos: [PHOTO1](https://web.archive.org/web/20240426212925/https://raw.githubusercontent.com/Jakubson/JPremiumCleared/master/images/network2.png), [PHOTO2](https://web.archive.org/web/20240426212925/https://raw.githubusercontent.com/Jakubson/JPremiumCleared/master/images/network1.png).
6. If you are using BungeeCord:
   * Set the `ip_forward` setting to `true` in the `config.yml` file on your BungeeCord server.
   * Set the `bungeecord` setting to `true` in the `spigot.yml` file on every Spigot server.
7. If you are using Velocity:
   * Follow instruction how to configure player information forwarding on your Velocity server: [https://docs.papermc.io/velocity/player-information-forwarding](https://web.archive.org/web/20240426212925/https://docs.papermc.io/velocity/player-information-forwarding)
   * If you want to use the legacy mode, you need to set `accessTokenDisabled` to true in your JPremium configuration files on your back-end servers. JPremium won't check any passing data to your back-end servers, so your servers will work in an insecure mode!
8. Start your server again.

**Installation on a back-end server (Spigot/PaperSpigot) - Optional installation**

1. Download a `JPremium-XXX.jar` file from spigotmc.org or builtbybit.com.
2. Navigate to your `plugins` directory on your back-end server and then place the `JPremium-XXX.jar` file in that directory.
3. Fully start and stop your back-end server to allow default JPremium configurations to be generated.
4. Open the JPremium configuration file which is located at `plugins/JPremium/configuration.yml`.
5. Copy the JPremium access token from your JPremium configuration on your proxy server and paste it into the `accessToken` option in your JPremium configuration in the back-end server.
6. Set the `captchaMapSlot` option to the slot number where JPremium should place the captcha map in a player inventory.
7. Start your server again.

### Data conversion

Please backup your database which you want to convert in order not to lose data! Before converting you have to install correctly the latest JPremium version on your proxy server, so please firstly follow instructions on [this wiki page](https://web.archive.org/web/20240426212925/https://github.com/Jakubson/JPremiumCleared/wiki#first-installation). You have to have default tables and columns names in the database from which you convert! The converter converts data only to a MySQL database.
Please remember that converting from AuthMe works only if you have passwords hashed in SHA-256 or Bcrypt.
Converting from DynamicBungeeAuth may not work correctly in all cases! Converting from DynamicBungeeAuth works only if you have password hashed in SHA-256 or SHA-512 and all premium players have online UUIDs.

* Download a `Converter-<VERSION>.jar` file from [this site](https://web.archive.org/web/20240426212925/https://github.com/Jakubson/JPremiumCleared/blob/master/assets/Converter-1.0-SNAPSHOT.jar).
* Navigate to your `plugins` directory on your BungeeCord server. Then place the `Converter.jar` file in that directory.
* Fully stop and start your BungeeCord server to allow a default configuration to be generated.
* The configuration will be located at `plugins/Converter/configuration.yml`.
* Open the configuration file on your BungeeCord server.
* Enter a plugin name in `convertFrom` which a database you want to convert.
* Enter your JPremium database credentials in `newStorage`.
* Enter database credentials from which you want to convert in `<plugin-name>Storage`. If you convert a local database, enter a database file path in `local`.
* Start your BungeeCord server and wait until converting will finish.
* After successful converting, remove the `Converter.jar` file from your BungeeCord server.
* Start your BungeeCord server again.

---

### Database issues

First of all, please remember that database connection issues are **never** caused by JPremium. The plugin uses the official driver. Your issues probably are caused by your wrong database configuration or the connection between your database and your proxy server. Here you have the most common solutions fot the most common issues:

1. `Connection refused` - You probably did not enter database credentials in the JPremium configuration or you entered wrong database credentials (an IP address and a port) in the JPremium configuration or your database is not reachable from your proxy.
2. `Access denied for user 'root'@'localhost' (using password: YES)` - You need to grant permission in your database. Please execute these queries in your database: `GRANT ALL PRIVILEGES ON <database>.* TO '<username>'@'%'` (replace `<database>` with your database name and `<username>` with your database username) and then `FLUSH PRIVILEGES`.
3. `Public Key Retrieval is not allowed` - You can fix it by setting the `storageProperties` option in your JPremium configuration file to `[allowPublicKeyRetrieval=true, useSSL=false]`.
4. `Failed to validate connection com.mysql.jdbc.JDBC4Connection@XXX (No operations allowed after connection closed.).` - The `connectionPoolLifetime` option in the JPremium configuration must be less then the `wait_timeout` setting in your database. It does not matter which value you change, so long as the `connectionPoolLifetime` option is less than the `wait_timeout` setting. Remember that the units of each value are different (`connectionPoolLifetime` is in miliseconds, `wait_timeout` is in seconds)! To check the `wait_timeout` setting, execute this query in your database: `SHOW GLOBAL VARIABLES LIKE "wait_timeout"` - to change it, execute: `SET GLOBAL wait_timeout = <new-time>`.

If you get any other errors which are not described above, please do NOT report it to the JPremium author because it is not related with JPremium. If you do not know how to fix it, please google it!

---

### BungeeCord and MariaDB

If you want to use MariaDB on BungeeCord, please follow below steps:

* Open `JPremium-CLEARED-XXX.jar` using WinRAR or any different similar software.
* Open `bungee.yml`, remove `#` from the last line (`#- org.mariadb.jdbc:mariadb-java-client:3.1.4`) and save the file.

---

### Website registration

JPremium has the feature which allows to players register from your website.

* Download the website from [this site](https://web.archive.org/web/20240426212925/https://github.com/Jakubson/JPremiumCleared/blob/master/assets/Website.zip).
* Open the `Website` directory in the zip file and upload `index.php` and `background.jpg` files into your website.
* Register your website on [Google re-captcha site](https://web.archive.org/web/20240426212925/https://www.google.com/recaptcha/intro/v3.html) to be able to use re-captcha. You have to register re-captcha v2!
* Open the `index.php` file on your website.
* Enter connection data with your database in the storage section, re-captcha data in the Google re-captcha section.
* Enter the same value which you have in `fixedUniqueIds` in your JPremium configuration file on your proxy server in the JPremium section.
* Set the `registerOnWebsite` option to 1 (registration on server and website) or 2 (registration on website only) in your JPremium configuration file on your proxy server.

---

### Fixed unique ids

Since Minecraft 1.8 all player data is stored using UUIDs (a universally unique identifier), not using nicknames. It was added to premium players can change nicknames without losing data. An example UUID looks like: d2affeb6-96a8-4c71-b4d6-b79f962b6309. All servers in the online mode use online UUIDs (got from Mojang servers) and all servers in the offline mode use offline UUIDs (generated using a player nickname).

JPremium alters all player UUIDs to work correctly and to avoid UUIDs collisions. There are three modes in which JPremium assings UUIDs for players. The three modes are: `FIXED`, `REAL`, `OFFLINE`. Each mode assigns different UUIDs and has different restrictions. Please read the below table.

| Mode | What unique ids players have? | Can cracked players with premium nicknames join to the server? (\*) | Can premium players join to the server after nicknames change? | Can players switch account types from cracked to premium and vice versa (/premium and /cracked)? |
| --- | --- | --- | --- | --- |
| FIXED | all players > random | Yes | Yes | Yes |
| REAL | premium players > online; cracked players > offline | Yes | Yes | No |
| OFFLINE | all players > offline | Yes | No | Yes |

(\*) By default JPremium doesn't allow cracked player with premium nickname to join. If you want to change it, please follow this steps: [https://github.com/Jakubson/JPremiumCleared/wiki#joining-issues](https://web.archive.org/web/20240426212925/https://github.com/Jakubson/JPremiumCleared/wiki#joining-issues)

When players are already registered in your JPremium database and you change the `uniqueIdsType` option, players will not lose any data, but they will have UUID from the previous configuration. Only new players will have another UUID type. You shouldn't change the mode more than one time because unexpected issues may occur!

When you install JPremium on the server where players played before installing, you have to correctly adjust the `uniqueIdsType` option to represent UUIDs which players have. If you have a server in the offline mode, you probably should select `OFFLINE`. If you have a server in the online mode, you probably should select `REAL`.

If you convert data from a AuthMe database, you can select `FIXED`, `REAL` or `OFFLINE`. It is not relevant because UUIDs are converted from the AuthMe database (converted players will have offline UUIDs). It is only relevant for new players.

**Please remember that cosmetics in custom clients (launchers) may NOT work in `FIXED` or `OFFLINE` because premium players have to have online UUIDs, so you need to use `REAL` to see cosmetics.**

If you enable the `floodgateSupport` option in your JPremium configuration on your proxy server, all Bedrock players will have a bedrock unique id regardless of the `uniqueIdsType` option.

---

### Bedrock support

**JPremium supports only premium Bedrock players. It does NOT support cracked Bedrock players!**

When you enable support for Bedrock Edition in JPremium, all Bedrock players will have a bedrock UUID. So, if you have already some Bedrock players on your server and you want to enable support for it, they will lose all data because they will have a new bedrock UUID, not an offline UUID or a fixed UUID as now. Bedrock players also cannot execute any JPremium commands. Bedrock players are automatically registered and logged, so you need to set Floodgate that only premium Bedrock players can join to your server.

* Install correctly GeyserMC **2.0** and Floodgate **2.0** on your server.
* In your Floodgate configuration file, you have to set a prefix in the `username-prefix` option (you **must** select `*` or `.` and you have to enable that option otherwise nicknames collisions may occur - you **CANNOT** use `a-z`, `A-Z`, `0-9` and `_` because then Java and Bedrock players may have the same nickname!). Then enable the `replace-spaces` option, disable `player-link.enable` option.
* In your JPremium configuration on your proxy server, enable the `floodgateSupport` option.
* Fully restart your proxy server.

---

### Joining issues

If some players cannot join to your server, those players are probably cracked players with premium nicknames.
You have two solutions for that (choose only one solution!). If you are using the REAL mode, you can only choose the second solution!

**THE VERY IMPORTANT NOTE! YOU CAN ONLY FOLLOW ONE OF THE BELOW POINTS - YOU CAN'T FOLLOW BOTH!**

1. Please set `registerPremiumUsers` to `false` in your JPremium configuration file on your proxy server. Now all new players have to register and login every joining. If new premium players do not want to use the `/login` command, they can execute the `/premium` command to auto login the next time. That soltuion work correctly on if you have FIXED UUIDs or OFFLINE UUIDs in the JPremium.
2. Please set `secondLoginCracked` to `true` in your JPremium configuration file on your proxy server. Now all cracked players, who use premium player nicknames which are not claimed on your server, can join, but their first connection request will be disconnected. Then they can re-join and play normally as a cracked player with a premium nickname.

Please remember that it will not affect cracked players who already joined (or tried to join) to your server. In such cases you need to execute the `/forcecracked <nickname>` command via your proxy console (you need to execute the `/forcecreatepassword <nickname> <any-password>` command before).

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

### A plugin tried to cancel a signed chat message / A plugin tried to deny a command with signable component(s)

Since version 1.19.1, Minecraft signs player's messages and commands to add compatibility for the chat report system. Velocity wants to keep compatibility with the chat report system and doesn't allow to edit or cancel any player's messages or commands and that's why you get the error. Velocity probably won't change it ([https://github.com/PaperMC/Velocity/issues/804#issuecomment-1200445270](https://web.archive.org/web/20240426212925/https://github.com/PaperMC/Velocity/issues/804#issuecomment-1200445270)), so the only way to forbid a player to send messages or commands before authorization is disconnecting them.

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

Due to data synchronization process with back-end servers, placeholder values are refreshed only after player joining or successful authorization (executed /login, /register, /forcelogin, /forceregister).

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

To obtain an instance of the `App` class on BungeeCord, call `com.jakub.premium.JPremium.getApplication();`.  
To obtain an instance of the `App` class on Velocity, call `com.jakub.jpremium.velocity.JPremiumVelocity.getApplication();`.

The API works only on the proxy site. JavaDocs: [https://jakubson.github.io/JPremiumCleared/](https://web.archive.org/web/20240426212925/https://jakubson.github.io/JPremiumCleared/)
If you are a developer who does not have access to the full JPremium version, you can use `JPremiumAPI.jar` file: [https://github.com/Jakubson/JPremiumCleared/blob/master/assets/JPremium-API.jar](https://web.archive.org/web/20240426212925/https://github.com/Jakubson/JPremiumCleared/blob/master/assets/JPremium-API.jar)
