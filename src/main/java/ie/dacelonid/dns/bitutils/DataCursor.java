package ie.dacelonid.dns.bitutils;

public class DataCursor {
    private final byte[] data;
    private int position;

    public DataCursor(byte[] data) {
        this.data = data;
        this.position = 0;
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

    public void skipQuestions(int numberOfQuestions) {
        if(position == 0){
            skipHeader();
        }
        for (int x = 0; x < numberOfQuestions; x++) {
            skipName();
            skipBytes(4); // QTYPE (2) + QCLASS (2)
        }
    }

    public void skipHeader() {
        skipBytes(12);
    }

    public void skipAnswer() {
        skipName();
        skipBytes(2 + 2 + 4 + 2 + 4); // TYPE + CLASS + TTL + length + data
    }
}
