package cl.drakescraft.dioses.bridge;

import java.io.DataInputStream;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class RustNativeBridgeTest {
    @Test
    void bridgeBytecodeDoesNotRequirePreview() throws Exception {
        try (var stream = new DataInputStream(RustNativeBridge.class
                .getResourceAsStream("RustNativeBridge.class"))) {
            assertEquals(0xCAFEBABE, stream.readInt());
            assertNotEquals(65535, stream.readUnsignedShort());
            assertEquals(65, stream.readUnsignedShort());
        }
    }

    @Test
    void missingNativeLibraryKeepsJavaFallback(@TempDir Path directory) {
        assertDoesNotThrow(() -> RustNativeBridge.initialize(directory.resolve("absent.so")));
        assertFalse(RustNativeBridge.isNativeLoaded());
        assertEquals(0L, RustNativeBridge.solveEnergyTick());
    }
}
