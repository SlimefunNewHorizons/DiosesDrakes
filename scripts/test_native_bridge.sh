#!/usr/bin/env bash
# Run after mvn clean package, on Linux with gcc and JDK 21/25 installed.
set -euo pipefail
repo_dir="$(cd "$(dirname "$0")/.." && pwd)"
probe_dir="$(mktemp -d)"
trap 'rm -rf "$probe_dir"' EXIT
gcc -shared -fPIC -x c -o "$probe_dir/energy.so" - <<'C'
#include <stdint.h>
int64_t slimefun_solve_energy_tick(void) { return INT64_C(0x123456789abcdef0); }
C
gcc -shared -fPIC -x c -o "$probe_dir/no-symbol.so" - <<'C'
int unrelated_function(void) { return 1; }
C
/usr/lib/jvm/java-21-openjdk-amd64/bin/javac --release 21 \
    -cp "$repo_dir/target/classes" -d "$probe_dir" \
    "$repo_dir/src/test/java/cl/drakescraft/dioses/bridge/NativeBridgeProbe.java"
for runtime in 21 25; do
    for fixture in energy no-symbol absent; do
        expected=false
        if [[ "$fixture" == energy ]]; then expected=true; fi
        "/usr/lib/jvm/java-$runtime-openjdk-amd64/bin/java" \
            --enable-native-access=ALL-UNNAMED -cp "$repo_dir/target/classes:$probe_dir" \
            cl.drakescraft.dioses.bridge.NativeBridgeProbe "$probe_dir/$fixture.so" "$expected"
    done
done
