package cl.drakescraft.dioses.bridge;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Optional Project Panama bridge, loadable on Java 21 and newer without preview bytecode.
 * High-Performance Native Altar Power & Favor Calculations.
 */
public final class RustNativeBridge {
    private static final Logger LOGGER = Logger.getLogger("DiosesDrakes-RustBridge");
    private static boolean isNativeLoaded = false;
    private static MethodHandle solveEnergyTickMH;

    public static void initialize(Path nativeLibPath) {
        isNativeLoaded = false;
        solveEnergyTickMH = null;
        try {
            System.load(nativeLibPath.toAbsolutePath().toString());
            // Resolve the runtime's FFM API only here: Java 21's preview types must
            // not mark the plugin class as preview and prevent loading on Java 25.
            Class<?> lookupType = Class.forName("java.lang.foreign.SymbolLookup");
            Object lookup = lookupType.getMethod("loaderLookup").invoke(null);
            Optional<?> result = (Optional<?>) lookupType.getMethod("find", String.class)
                    .invoke(lookup, "slimefun_solve_energy_tick");
            Object symbol = result.orElse(null);
            if (symbol != null) {
                Class<?> linkerType = Class.forName("java.lang.foreign.Linker");
                Class<?> layoutType = Class.forName("java.lang.foreign.MemoryLayout");
                Class<?> descriptorType = Class.forName("java.lang.foreign.FunctionDescriptor");
                Object layouts = Array.newInstance(layoutType, 0);
                Object longLayout = Class.forName("java.lang.foreign.ValueLayout").getField("JAVA_LONG").get(null);
                Object descriptor = descriptorType.getMethod("of", layoutType, layouts.getClass())
                        .invoke(null, longLayout, layouts);
                Object options = Array.newInstance(Class.forName("java.lang.foreign.Linker$Option"), 0);
                Object linker = linkerType.getMethod("nativeLinker").invoke(null);
                solveEnergyTickMH = (MethodHandle) linkerType.getMethod("downcallHandle",
                        Class.forName("java.lang.foreign.MemorySegment"), descriptorType, options.getClass())
                        .invoke(linker, symbol, descriptor, options);
                isNativeLoaded = true;
                LOGGER.info("⚡ [DiosesDrakes] Successfully bound to Slimefun-Rust Native FFM Altar Engine!");
            }
        } catch (Throwable t) {
            LOGGER.warning("⚠️ [DiosesDrakes] Slimefun-Rust native library not loaded: " + t.getMessage());
        }
    }

    public static long solveEnergyTick() {
        if (isNativeLoaded && solveEnergyTickMH != null) {
            try {
                return (long) solveEnergyTickMH.invokeExact();
            } catch (Throwable ignored) {}
        }
        return 0;
    }

    public static boolean isNativeLoaded() {
        return isNativeLoaded;
    }
}
