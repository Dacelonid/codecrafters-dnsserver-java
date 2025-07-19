import java.util.Arrays;

/**
 * A utility class for writing bits into a byte array (MSB-first).
 */
public class BitWriter {
    private final byte[] buffer;
    private int bitPos = 0;

    public BitWriter(int byteLength) {
        this.buffer = new byte[byteLength];
    }

    /**
     * Writes the lowest 'bitCount' bits of the given value into the buffer.
     */
    public void writeBits(int value, int bitCount) {
        for (int i = bitCount - 1; i >= 0; i--) {
            int byteIndex = bitPos / 8;
            int bitOffset = 7 - (bitPos % 8); // MSB-first
            int bit = (value >> i) & 1;
            buffer[byteIndex] |= (bit << bitOffset);
            bitPos++;
        }
    }

    public byte[] getBytes() {
        return buffer;
    }

    public int getBitPos() {
        return bitPos;
    }
}