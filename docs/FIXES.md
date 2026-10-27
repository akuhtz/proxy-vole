# Fix Plan: proxy-vole Issues

Scope: **must-fix + should-fix** (from the code analysis). No engine migration
(staying on Rhino).

## Phase 1 — Correctness & safety (must-fix)

### 1. GnomeDConfProxySearchStrategy.java — process/stream leak
File: `src/main/java/com/github/markusbernhardt/proxy/search/desktop/gnome/GnomeDConfProxySearchStrategy.java`
(`parseSettings`, ~291-318)

- Replace `Runtime.getRuntime().exec(String)` (deprecated overload) with
  `new ProcessBuilder("gsettings", "list-recursively", "org.gnome.system.proxy")
  .redirectErrorStream(true)`.
- Wrap the `BufferedReader` in try-with-resources.
- On `InterruptedException` from `p.waitFor()`, restore the interrupt flag with
  `Thread.currentThread().interrupt()` before returning.

### 2. PAC thread-safety — synchronized evaluate()
File: `src/main/java/com/github/markusbernhardt/proxy/selector/pac/JavaxPacScriptParser.java`
(`evaluate()`, ~137-154)

- Add `synchronized` to `evaluate()`. Single shared `RhinoSandbox engine`
  (field, line 31) is not thread-safe but is used concurrently via
  `PacProxySelector` (the JVM default `ProxySelector` may be called from many
  threads). `synchronized` serializes PAC calls (fast) with lowest risk.

## Phase 2 — Robustness (should-fix)

### 3. UrlPacScriptSource.java — resource + debug cleanup
File: `src/main/java/com/github/markusbernhardt/proxy/selector/pac/UrlPacScriptSource.java`

- `downloadPacContent` (~152-155): convert reader handling to try-with-resources
  so the reader always closes on exception; keep `con.disconnect()` in a
  `finally` block.
- Line 114: remove the stray `System.out.println(System.getProperty("user.dir"))`
  (replace with `Logger` debug or drop).

### 4. PacProxySelector.java — surface parser setup failure
File: `src/main/java/com/github/markusbernhardt/proxy/selector/pac/PacProxySelector.java`

- `selectEngine` (~75-83): currently swallows engine-setup failure (logs only),
  leaving `pacScriptParser == null` and all later selections silently returning
  no-proxy. Add an ERROR log and a `parserInitFailed` flag so the failure is
  visible. No API change.

### 5. Deprecated `new URL(...)` sweep (main source)
Replace `new URL(...)` with `URI.create(...).toURL()` (Java 20+ deprecation),
handling `URISyntaxException`/`MalformedURLException` as appropriate:

- `UrlPacScriptSource.java:102, 218`
- `util/ProxyUtil.java:49`
- `wpad/WpadProxySearchStrategy.java:153`
- `wpad/WpadProxySearchStrategyWithDHPC.java:138`
- `ui/ProxyTester.java:135`

### 6. Stale Javadoc
File: `src/main/java/com/github/markusbernhardt/proxy/selector/pac/JavaxPacScriptParser.java:14`

- Reword "PAC parser using the Rhino JavaScript engine bundled with Java 1.6" to
  reflect that Rhino is now an explicit Maven dependency
  (`org.mozilla:rhino-engine` / `org.mozilla:rhino`).

## Phase 3 — Build & repo hygiene (should-fix)

### 7. pom.xml
- Javadoc `<source>8</source>` → `17` (lines 236 and 434), or remove to inherit
  `maven.compiler.release`.
- `osgi-version-qualifier` (line 184): the `_snapshot` value references an
  undefined property. Define it or remove `_snapshot`; verify the generated
  `MANIFEST.MF` version qualifier after a build.

### 8. Repo hygiene
- Add `*.log` to `.gitignore`.
- Delete stale `proxy-vole-test.log` from the repo root.
- `PacProxyDebugging.java` (src/test, `main()` hitting a live host): move out of
  `src/test/java` or guard it so it cannot run accidentally in CI.

## Verification

- `mvn test` must stay green, especially `JavaxPacScriptParserTest`
  (incl. `shouldNotExecuteCodeInPac`, `testReturnTypes`).
- Build javadoc + bundle to confirm `<source>17` and the OSGi qualifier are clean.
