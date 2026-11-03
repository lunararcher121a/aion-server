# Silkroad Remastered — isolated Windows + XAMPP test environment

**Status:** setup guide only. No Windows server or Aion client has been launched or tested.
**Never use production databases or production server configuration.**

## 1. Prerequisites

- Windows PC with Java **JDK 25**, Maven and Git (root `pom.xml` uses `maven.compiler.release=25`).
- XAMPP with MySQL/MariaDB running. Apache/phpMyAdmin is optional; Aion itself does **not** run on Apache/PHP.
- Matching, legally obtained Aion 4.8 client. The server does not contain the client.
- This GitHub branch: `silkroad-remastered`. Do not edit branch `4.8`.

## 2. Isolated directories and databases

Use a dedicated folder such as `C:\SilkroadRemasteredTest\`, not `C:\xampp\htdocs\` and not the NEFRIA directory.
Clone with:

```powershell
git clone --branch silkroad-remastered --single-branch https://github.com/lunararcher121a/aion-server.git C:\SilkroadRemasteredTest\aion-server
cd C:\SilkroadRemasteredTest\aion-server
java -version
mvn -version
mvn -B -DskipTests package
```

Create **three separate** test databases, e.g. `srotest_ls`, `srotest_gs`, `srotest_cs` using phpMyAdmin or SQL:

```sql
CREATE DATABASE srotest_ls CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE srotest_gs CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE srotest_cs CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Import the original schema SQL from each module's `sql` folder into its **matching test database** (identify actual schema filenames after checkout; do not guess names). Apply `game-server/sql/silkroad_remastered_v001*.sql` and `game-server/sql/silkroad_remastered_v002_monsters.sql` only to `srotest_gs` if the feature requires them. Never run migrations against an existing `aion_gs` / NEFRIA database.

Use a **separate test DB user** with access only to these three databases. Avoid using XAMPP's default root user for the game servers.

## 3. Configure separate test server copies

The project's README documents separate `config/mygs.properties`, `config/myls.properties`, and `config/mycs.properties` overrides in the corresponding module/runtime folders.

Configure the test-only database URLs, username/password, and local addresses there. Examples of the relevant database keys:

```properties
database.url = jdbc:mysql://127.0.0.1:3306/srotest_gs?serverTimezone=UTC&characterEncoding=UTF-8
database.user = YOUR_TEST_DB_USER
database.password = YOUR_TEST_DB_PASSWORD
```

Replace `srotest_gs` with `srotest_ls` or `srotest_cs` for the other modules. Never commit real credentials. For a local-only setup, bind services to `127.0.0.1` and keep Windows Firewall closed to inbound public connections.

The checked-in defaults are: login client `2106`, login↔game `9014`, game client `7777`, chat `9021`. Ensure no other server uses these ports. Configure the login DB `gameserver` whitelist (ID/IP/password) to match `gameserver.network.login.gsid` and `gameserver.network.login.password` on the game server.

## 4. Build and start

```powershell
mvn -B -DskipTests package
```

The README says each module's `target` contains deployable ZIP archives. Extract the login, chat and game servers into **three separate test directories** and preserve their expected `config` and `data` paths. Read the included launch scripts / README in each extracted distribution before starting; run login, chat and game with Java 25 and the correct working directory. Do not point these services to production databases.

## 5. Verify model and controlled combat

Start with Silkroad monster spawning **disabled** (no JVM flag) and confirm the servers start and a test GM can log in.

1. As a GM with access level 9, use `//sromangyang check`. It checks if candidate Aion NPC template **210671** is loaded. A positive result does **not** confirm the client mesh, AI, hostility, or animations.
2. Only if the model is loaded and safe, restart the **test game server only** with `-Dsilkroad.monsters.enabled=true`. Keep this flag **off** on production.
3. At a safe, empty test location use `//sromangyang spawn`. This creates one test NPC at the GM position (which may overlap the player; use caution).
4. Confirm 100 max HP, physical attack 10, physical defense 0; these are **placeholder test values**, not authenticated OpenSRO values. Native Aion NPC name, level, mesh and AI remain in effect.
5. Kill it, wait for the native respawn interval (preset: 60 seconds), and verify stats again. Note that a native NPC may disallow respawn or be delayed by loot/decay.
6. Stop the test game server after the test; the GM command currently has no dedicated despawn/cleanup subcommand.

If `check` reports missing template, **do not spawn**. Replace the candidate only after confirming a valid NPC ID in actual server data and matching client assets.

## 6. Evidence and limitations

Capture the game server log, `//sromangyang check` output, combat screenshots and post-respawn stats. The GitHub Actions compile workflow is useful but does not validate runtime database contents, Aion 4.8 client assets, or actual combat. No real in-game Mangyang test has been performed yet.
