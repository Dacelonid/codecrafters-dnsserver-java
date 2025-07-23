package ie.dacelonid.dns.bitutils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public class DNSParserUtils {

    public static int readUInt16(byte[] data, int position) {
        return ((data[position++] & 0xFF) << 8) |
                (data[position++] & 0xFF);
    }

    public static int readUInt32(byte[] data, int position) {
        return ((data[position++] & 0xFF) << 24) |
                ((data[position++] & 0xFF) << 16) |
                ((data[position++] & 0xFF) << 8)  |
                (data[position++] & 0xFF);
    }

    public static NameParseResult parseName(byte[] data, int position) {
        StringJoiner name = new StringJoiner(".");
        int originalPos = position;
        boolean jumped = false;

        while (true) {
            int length = data[position++] & 0xFF;

            if ((length & 0xC0) == 0xC0) {
                // Pointer: next byte gives offset
                int nextByte = data[position++] & 0xFF;
                int pointer = ((length & 0x3F) << 8) | nextByte;

                if (!jumped) {
                    originalPos = position; // save return position for caller
                    jumped = true;
                }

                // Follow the pointer recursively
                NameParseResult result = parseName(data, pointer);
                name.add(result.name);
                break;
            }

            if (length == 0) {
                break;
            }

            name.add(new String(data, position, length, StandardCharsets.UTF_8));
            position += length;
        }

        return new NameParseResult(name.toString(), jumped ? originalPos : position);
    }

    public static byte[] intToBytes(int x, int size) {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        if (size == 2) {
            buffer.putShort((short) x);
        } else {
            buffer.putInt(x);
        }
        return buffer.flip().array();
    }

    public record NameParseResult(String name, int position){}

}
