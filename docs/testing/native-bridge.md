# Optional Rust bridge without preview bytecode

The previous Java 21 FFM imports required `--enable-preview`, producing a
`RustNativeBridge.class` with major 65 / minor 65535. That preview class cannot
load on Java 25. The bridge now resolves the FFM API reflectively only during
`initialize`; the existing `long ()` native ABI and zero fallback are preserved.
No reflection occurs in the tick call. Failed reinitialization clears the binding.

The bridge has no callers in the current plugin sources. This change does not
enable native energy processing or alter divine progression or stored data.

## Reproduce

On Star, compile sequentially through the shared build lock:

```bash
~/ai-hub/scripts/build_seguro.sh 21 "$PWD" mvn -B -nsu clean package
~/ai-hub/scripts/build_seguro.sh 21 "$PWD" bash scripts/test_native_bridge.sh
~/ai-hub/scripts/build_seguro.sh 25 "$PWD" mvn -B -nsu clean package \
  -Dpaper.version=26.2.build.129-stable
```

Both builds retain the Java 21 bytecode target and run the full unit suite.
The Linux probe requires gcc and JDK 21/25. It creates disposable native fixtures,
starts six fresh JVMs without `--enable-preview`, and checks the 64-bit return
value, missing symbol, absent library, and fallback after failed reinitialization.
Its `--enable-native-access=ALL-UNNAMED` flag is confined to these test processes.

The API signatures follow [Oracle's Linker documentation](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/foreign/Linker.html).
These are compiler and JVM checks, not a running Paper server certification.
Deployment still requires a verified rollback copy and a server smoke test.
Rollback: restore the previous jar through the approved deployer; the code-only
change requires no data migration.
