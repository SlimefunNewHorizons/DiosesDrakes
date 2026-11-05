package cl.drakescraft.dioses.bridge;

import java.nio.file.Path;

/** Standalone probe: use a fresh JVM for each native-library fixture. */
public final class NativeBridgeProbe {
    public static void main(String[] args) {
        if (RustNativeBridge.isNativeLoaded() || RustNativeBridge.solveEnergyTick() != 0L) {
            throw new AssertionError("Native bridge must be optional at class load");
        }
        Path library = Path.of(args[0]);
        boolean expectedLoaded = Boolean.parseBoolean(args[1]);
        RustNativeBridge.initialize(library);
        long expected = expectedLoaded ? 0x123456789abcdef0L : 0L;
        if (RustNativeBridge.isNativeLoaded() != expectedLoaded
                || RustNativeBridge.solveEnergyTick() != expected) {
            throw new AssertionError("Native binding or 64-bit ABI mismatch");
        }
        RustNativeBridge.initialize(library.resolveSibling("nonexistent-library.so"));
        if (RustNativeBridge.isNativeLoaded() || RustNativeBridge.solveEnergyTick() != 0L) {
            throw new AssertionError("Failed reinitialization retained a stale native binding");
        }
        System.out.println("PASS Java " + Runtime.version().feature() + " native=" + expectedLoaded);
    }
}
