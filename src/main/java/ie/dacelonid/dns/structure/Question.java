package ie.dacelonid.dns.structure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Question {

    private int position;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Question question)) return false;
        return type == question.type && classValue == question.classValue && Objects.equals(name, question.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, classValue);
    }

    @Override
    public String toString() {
        return "Question{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", classValue=" + classValue +
                '}';
    }

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
        this.position = builder.position;
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

    public int getPosition() {
        return position;
    }

    public static class QuestionBuilder {
        public String name;
        public int type;
        public int classValue;
        private int position = 12;

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
            parseNameFromData(data);
            parseTypeFromData(data);
            parseClassFromData(data);
            return new Question(this);
        }

        private void parseClassFromData(byte[] data) {
            ByteBuffer f = ByteBuffer.allocate(4);
            f.put((byte) 0x00);
            f.put((byte) 0x00);
            f.put(data[position++]);
            f.put(data[position++]);
            f.flip();
            this.classValue = f.getInt();
        }

        private void parseTypeFromData(byte[] data) {
            ByteBuffer f = ByteBuffer.allocate(4);
            f.put((byte) 0x00);
            f.put((byte) 0x00);
            f.put(data[position++]);
            f.put(data[position++]);
            f.flip();
            this.type = f.getInt();
        }

        private void parseNameFromData(byte[] data) {
            StringBuilder name = new StringBuilder();
            while (position < data.length) {
                int length = data[position++]; //first record is the length of the record
                name.append(new String(data, position, length, StandardCharsets.UTF_8));
                position += length;
                length = data[position++];
                name.append(".");
                name.append(new String(data, position, length, StandardCharsets.UTF_8));
                this.name = name.toString();
                position += length;
                if (data[position++] == 0) {
                    break;
                }
            }
        }
    }
}
