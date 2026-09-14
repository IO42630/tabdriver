# AGENTS.md

Java 25 / Maven library (`com.olexyn:tabdriver`): a thin, opinionated wrapper around Selenium's `ChromeDriver`.

## Build & verify

- Toolchain: Java 25 (temurin), Maven 3.9.11 — see `.tool-versions` (mise/asdf).
- Parent is `com.olexyn:min-root:25.0.3`; the build needs network access to resolve it (not always in `~/.m2`).
- Compile/package: `mvn -DskipTests install`.
- Test: `mvn test` (passes; the only test is `@Disabled`, so expect `Tests run: 1, Skipped: 1`).
- Deploy: `./deploy.sh` — runs `mvn clean install deploy &` (backgrounded; requires GPG + Central credentials in the `release` profile).
- **Keep the `junit:junit` exclusion on `webdriver-support` in `pom.xml`.** `org.seleniumhq.webdriver:webdriver-support:0.9.7376` transitively pulls `junit:junit:3.8.1`. Surefire sees JUnit 4 on the classpath, activates `junit-vintage-engine`, and aborts discovery with `Unsupported version of junit:junit: 3.8.1`. Removing the exclusion brings the failure back.

## Tests

- `TabDriverTest` hits the real Chrome driver and its `test()` is `@Disabled`; it only proves wiring.
- `TestTabDriverConfig` expects the driver binary at `src/test/resources/chromedriver_124`, which is **not committed** (`.gitignore` ignores `**/chromedriver_**`). Integration tests are effectively unrunnable until you drop a matching chromedriver there.

## Runtime gotchas

- Must use **Chrome**, not Chromium.
- Config comes from a `.properties` file exposing `chrome.driver.path`, `headless`, `download.dir` — see `README.md`. `DefaultTabDriverConfig` builds `ChromeOptions` from `isHeadless()` / `getDownloadDir()`; extend it rather than implementing `TabDriverConfigProvider` from scratch.
- Selenium/webdriver versions are pinned in `pom.xml` properties (`xx.selenium.version`, `xx.webdriver.version`); the `selenium-api` dependency must stay ordered before the others.

## Code conventions

- API is intentionally CSS-first. Add accessors in the `findByCss` / `findAllByCss` style: return `Optional`/empty-list on any exception, never throw for a missing element. `getByText` (XPath) is the exception, not the norm.
- `purpose`-keyed tabs via `newTab`/`goToTab`; `Purpose` is a `record` with name-based `equals`.
- Lombok and `checker-qual` nullness annotations are used throughout (parent POM supplies their versions).
