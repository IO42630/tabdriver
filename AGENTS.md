# AGENTS.md

## 1. Overview

`com.olexyn:tabdriver:1.5.0` — a Java library that wraps Selenium's `ChromeDriver` behind a CSS-first, exception-swallowing API, for precise delay-paced interaction with real web UIs (target use: trading/banking flows where precision matters more than raw speed). Stack: Java 25 (temurin), Maven 3.9.11, Selenium 4.49.0, Lombok, checker-qual; parent `com.olexyn:min-root:25.0.3`, BOM `com.olexyn:min-bom:25.1.1`.

Status: the library compiles and is the working implementation in local use; `mvn test` fails on any machine without Chrome plus a ChromeDriver at the hardcoded test path. No feature work in flight.

## 2. Model

- `TabDriver` — the one concrete class; owns exactly one `ChromeDriver` and a `Map<String,Tab>` keyed by window handle; implements `JavascriptExecutor` and `ITabDriver`. Every public method is `synchronized`.
- `Tab` — `handle` (window handle) + `purpose`; Lombok `@Getter/@Setter/@AllArgsConstructor`.
- `Purpose` — `record(String name)` whose `equals` compares `name` only, so tab identity/lookup is by purpose name, not object identity.
- `ITabDriver` — the only driver-behavior interface (`newTab(Purpose)`, `goToTab(Purpose)`); the seam intended for a future engine swap.
- `TabDriverConfigProvider` — `getDriverPath(): Path`, `getDownloadDir(): String`, `isHeadless(): boolean`, `getOptions(): ChromeOptions`.
- `DefaultTabDriverConfig` — abstract base that builds `ChromeOptions`: accept insecure certs, `--start-maximized`; when headless adds `--window-size=1920,1080` + `--headless`; prefs disable popups/prompt and set `download.default_directory`.

Invariants:
- `findByCss`/`findAllByCss` never throw — `Optional.empty()` / `List.of()` on any exception (`TabDriver.java:192`, `TabDriver.java:204`).
- `newTab` is idempotent per `Purpose`; `goToTab` switches to the first matching purpose and silently no-ops if none exists (`TabDriver.java:90`, `TabDriver.java:102`).
- Implicit wait is fixed at 2 s in the constructor (`TabDriver.java:40`).
- No properties-file loading exists; config is programmatic via the provider.

## 3. Contents

- `TabDriver` (`src/main/java/com/olexyn/tabdriver/TabDriver.java`) — main API: navigation, window/tab management, synchronized CSS finders, JS click/script, radio/combo helpers. Depends on Selenium and checker-qual. Working; also carries legacy helpers unused inside this repo (`findFrameContainingCharSeq`, `CRITERIA`, `followContainedLink`, `setRadio`, `setComboByDataValue`, `sendDeleteKeys`), and both `JavascriptExecutor` overrides are stubs returning `null`.
- `ITabDriver` — tab lifecycle contract. Done.
- `Tab` — tab metadata POJO. Done.
- `Purpose` — name-equal value record. Done.
- `TabDriverConfigProvider` / `DefaultTabDriverConfig` — config contract plus `ChromeOptions` base. Done; extend the base, do not reimplement from scratch.
- `Constants` — string literals; not referenced by any in-repo source file.
- `TabDriverTest` (`src/test/java/...`) — enabled integration test opening Google/YouTube tabs. Blocked: requires real Chrome plus the driver binary.
- `TestTabDriverConfig` — points `getDriverPath()` at `${user.home}/home/apps/chrome_155/chromedriver`. Host-path dependent.
- `README.md` — usage docs. Stale (see History).
- `deploy.sh` — `mvn clean install deploy &`.
- Removed since 1.4: `TabDriverBuilder` (deleted in `c8e7f80`); the `org.seleniumhq.webdriver:webdriver-*:0.9.7376` dependencies (deleted in `b188a41`).

## 4. Conventions

Build/test/deploy (there is no lint or formatter config in the repo):
- Toolchain pinned in `.tool-versions`: `java temurin-25`, `maven 3.9.11`.
- Compile/install: `mvn -DskipTests install` (verified compiles on this machine).
- Test: `mvn test` — currently fails here with `NoSuchDriverException: chromedriver must exist: /home/dev/home/apps/chrome_155/chromedriver`. The only test is enabled and needs a real Chrome plus driver.
- Deploy: `./deploy.sh` → `mvn clean install deploy &` (needs GPG plus Central credentials in the `release` profile).

Style and gotchas:
- `pom.xml` uses tabs; Java uses 4 spaces. No `.editorconfig`, no enforced formatter.
- `pom.xml:28` declares `selenium-api` first with the comment "must come before selenium"; versions are pinned only by `xx.selenium.version` (4.49.0). `min-prop` is declared but unused in source.
- CSS-first is the API contract: new accessors follow `findByCss`/`findAllByCss` and return empty on any failure; `getByText` (XPath) is the deliberate exception.
- Lombok supplies boilerplate; checker-qual `@NonNull`/`@Nullable` annotate public parameters.

## 5. History

## 2026-09-14 — Java 25 + Selenium 4.49 + Chrome 155 (`b188a41`)
- Bumped Java 17 → 25 and pinned Maven 3.9.11 in `.tool-versions`.
- Deleted the three `org.seleniumhq.webdriver:webdriver-*:0.9.7376` dependencies; this removes the transitive `junit:junit:3.8.1` that made surefire's `junit-vintage-engine` abort — the earlier "exclude junit from `webdriver-support`" workaround is now obsolete.
- Selenium 4.34.0 → 4.49.0; implicit wait switched from `TimeUnit.SECONDS` to `Duration.ofSeconds`.
- Un-`@Disabled` `TabDriverTest`; test driver path moved to `${user.home}/home/apps/chrome_155/chromedriver`.
- `.gitignore` pattern `**/chromedriver_**` → `**/chromedriver**`; README gained the macOS `xattr -dr com.apple.quarantine` note.

## 2025-07 — CSS-first refocus (`d7a18f6`, `2907b26`, `ef13c45`)
- Dropped the `com.olexyn:min-log` dependency and bumped Selenium 4.21.0 → 4.34.0; added `checker-qual`.
- Made the find helpers private; declared CSS the primary interaction path over older criteria-based lookups.

## 2024-11 — min-root/min-bom migration (`7958b64`, `fddfc8e`, `c8e7f80`)
- Moved to the `min-root`/`min-bom` parent chain and replaced `README.adoc` with `README.md`.
- Removed `TabDriverBuilder` (the `.properties`-loading entrypoint) in favor of constructing `TabDriver` from a config provider.

## 2024-06 — Selenium 4 migration (`b43f99f`, `24c2f6a`, `8958b80`)
- Moved to Selenium 4 and restored tab functionality; added `findAllByCss` and the first tests.
