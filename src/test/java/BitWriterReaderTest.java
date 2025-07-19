import java.util.Arrays;

// --- Unit Tests ---
class BitWriterReaderTest {
    public static void main(String[] args) {
        testSingleBytePacking();
        testMultiBytePacking();
        testCrossByteBoundary();
        System.out.println("All tests passed.");
    }

    static void testSingleBytePacking() {
        BitWriter writer = new BitWriter(1);
        writer.writeBits(0b101, 3);
        byte[] bytes = writer.getBytes();
        assert Arrays.equals(bytes, new byte[]{(byte) 0b10100000});

        BitReader reader = new BitReader(bytes);
        int val = reader.readBits(3);
        assert val == 0b101 : "Expected 0b101, got " + Integer.toBinaryString(val);
    }

    static void testMultiBytePacking() {
        BitWriter writer = new BitWriter(2);
        writer.writeBits(0xA, 4);   // 1010
        writer.writeBits(0xB, 4);   // 1011
        byte[] bytes = writer.getBytes();
        assert Arrays.equals(bytes, new byte[]{(byte) 0xAB, 0x00});

        BitReader reader = new BitReader(bytes);
        int v1 = reader.readBits(4);
        int v2 = reader.readBits(4);
        assert v1 == 0xA;
        assert v2 == 0xB;
    }

    static void testCrossByteBoundary() {
        BitWriter writer = new BitWriter(2);
        writer.writeBits(0b11111, 5);
        writer.writeBits(0b001, 3);
        byte[] bytes = writer.getBytes();
        assert Arrays.equals(bytes, new byte[]{(byte) 0b11111001, 0x00});

        BitReader reader = new BitReader(bytes);
        int val1 = reader.readBits(5);
        int val2 = reader.readBits(3);
        assert val1 == 0b11111;
        assert val2 == 0b001;
    }
}