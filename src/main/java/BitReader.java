/**
 * A utility class for reading bits from a byte array (MSB-first).
 */
class BitReader {
    private final byte[] buffer;
    private int bitPos = 0;

    public BitReader(byte[] buffer) {
        this.buffer = buffer;
    }

    /**
     * Reads the next 'bitCount' bits as an integer.
     */
    public int readBits(int bitCount) {
        int result = 0;
        for (int i = 0; i < bitCount; i++) {
            int byteIndex = bitPos / 8;
            int bitOffset = 7 - (bitPos % 8);
            int bit = (buffer[byteIndex] >> bitOffset) & 1;
            result = (result << 1) | bit;
            bitPos++;
        }
        return result;
    }

    public int getBitPos() {
        return bitPos;
    }
}