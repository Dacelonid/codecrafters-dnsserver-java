package ie.dacelonid.dns.bitutils;

import java.util.Arrays;

public class DataCursor {
    private final byte[] data;
    private int position;

    public DataCursor(byte[] data, int startPosition) {
        this.data = data;
        this.position = startPosition;
    }

    public int getPosition() {
        return position;
    }

    public String readName() {
        DNSParserUtils.NameParseResult result = DNSParserUtils.parseName(data, position);
        position = result.position();
        return result.name();
    }

    public void skipName() {
        DNSParserUtils.NameParseResult result = DNSParserUtils.parseName(data, position);
        position = result.position(); // don't store name
    }

    public byte[] readBytes(int length) {
        byte[] result = Arrays.copyOfRange(data, position, position + length);
        position += length;
        return result;
    }

    public int readUInt16() {
        return ((data[position++] & 0xFF) << 8) | (data[position++] & 0xFF);
    }

    public int readUInt32() {
        return ((data[position++] & 0xFF) << 24) |
                ((data[position++] & 0xFF) << 16) |
                ((data[position++] & 0xFF) << 8) |
                (data[position++] & 0xFF);
    }

    public void skipBytes(int len) {
        position += len;
    }

    public void skipQuestion() {
        skipName();
        skipBytes(4); // QTYPE (2) + QCLASS (2)
    }

    public void skipAnswer() {
        skipName();
        skipBytes(2 + 2 + 4 + 2 + 4); // TYPE + CLASS + TTL + length + data
    }
}
