import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BitWriterTest {

    @Test
    public void testSingleBytePacking(){
        BitWriter writer = new BitWriter(1);
        writer.writeBits(0b101, 3);
        byte[] bytes = writer.getBytes();
        assertArrayEquals(new byte[]{(byte) 0b10100000}, bytes);

        BitReader reader = new BitReader(bytes);
        int val = reader.readBits(3);
        assertEquals(0b101, val);
    }
    @Test
    public void testMultiBytePacking(){
        BitWriter writer = new BitWriter(2);
        writer.writeBits(0xA, 4);   // 1010
        writer.writeBits(0xB, 4);   // 1011
        byte[] bytes = writer.getBytes();
        assertArrayEquals(new byte[]{(byte) 0xAB, 0x00}, bytes);

        BitReader reader = new BitReader(bytes);
        int v1 = reader.readBits(4);
        int v2 = reader.readBits(4);
        assertEquals(0xA, v1);
        assertEquals(0xB, v2);
    }

    @Test
    public void testCrossByteBoundary(){
        BitWriter writer = new BitWriter(2);
        writer.writeBits(0b11111, 5);
        writer.writeBits(0b001, 3);
        byte[] bytes = writer.getBytes();
        assertArrayEquals(new byte[]{(byte) 0b11111001, 0x00}, bytes);

        BitReader reader = new BitReader(bytes);
        int val1 = reader.readBits(5);
        int val2 = reader.readBits(3);

        assertEquals(0x1F, val1);
        assertEquals(0x1, val2);

    }
}
