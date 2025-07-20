package ie.dacelonid.dns.structure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Question {

    String name;
    int type;
    int classValue;

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getClassValue() {
        return classValue;
    }

    private Question(QuestionBuilder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.classValue = builder.classValue;
    }

    public byte[] toBytes() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            String[] split = name.split("\\.");
            byte[] sld = split[0].getBytes(StandardCharsets.UTF_8);
            byte[] tld = split[1].getBytes(StandardCharsets.UTF_8);
            output.write(sld.length);
            output.write(sld);
            output.write(tld.length);
            output.write(tld);
            output.write(0x00);
            output.write(intToBytes(type, 2));
            output.write(intToBytes(classValue, 2));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output.toByteArray();
    }

    private byte[] intToBytes(int x, int size) {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putShort((short) x);
        return buffer.flip().array();
    }

    public static class QuestionBuilder {
        public String name;
        public int type;
        public int classValue;

        public QuestionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public QuestionBuilder type(int type) {
            this.type = type;
            return this;
        }

        public QuestionBuilder classValue(int classValue) {
            this.classValue = classValue;
            return this;
        }

        public Question build() {
            return new Question(this);
        }

        public Question from(byte[] data) {
            int pos = 12; //start from header
            pos = getName(data, pos);
            pos = getType(data, ++pos);
            pos = getClassValue(data, ++pos);
            return new Question(this);
        }

        private int getClassValue(byte[] data, int pos) {
            ByteBuffer f = ByteBuffer.allocate(4);
            f.put((byte) 0x00);
            f.put((byte) 0x00);
            f.put(data[pos++]);
            f.put(data[pos]);
            f.flip();
            this.classValue = f.getInt();
            return pos;
        }

        private int getType(byte[] data, int pos) {
            ByteBuffer f = ByteBuffer.allocate(4);
            f.put((byte) 0x00);
            f.put((byte) 0x00);
            f.put(data[pos++]);
            f.put(data[pos]);
            f.flip();
            this.type = f.getInt();
            return pos;
        }

        private int getName(byte[] data, int pos) {
            StringBuilder name = new StringBuilder();
            while (pos < data.length) {
                int length = data[pos++]; //first record is the length of the record
                name.append(new String(data, pos, length, StandardCharsets.UTF_8));
                pos += length;
                length = data[pos++];
                name.append(".");
                name.append(new String(data, pos, length, StandardCharsets.UTF_8));
                this.name = name.toString();
                pos += length;
                if (data[pos] == 0)
                    break;
            }
            return pos;
        }
    }
}
