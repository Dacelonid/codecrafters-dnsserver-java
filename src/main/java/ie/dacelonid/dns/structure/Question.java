package ie.dacelonid.dns.structure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.StringJoiner;

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
            for (String s : split) {
                output.write(s.length());
                output.write(s.getBytes(StandardCharsets.UTF_8));
            }
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

        public Question from(byte[] data, int count) {
            for (int x = 0;x<=count;x++) {
                parseNameFromData(data);
                parseTypeFromData(data);
                parseClassFromData(data);
            }
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
            StringJoiner name = new StringJoiner(".");
            while (position < data.length) {
                int length = data[position++] & 0xFF; // read length and advance
                if (length == 0) {
                    break; // null terminator indicates end of name
                }else if((length & 192) == 192) { //compression
                    int nextByte = data[position++] & 0xFF;
                    int pointer = ((length & 0x3F) << 8) | nextByte;
                    name.add(parseNameFromData(data, pointer));
                    break;
                }

                name.add(new String(data, position, length, StandardCharsets.UTF_8));
                position += length;
            }
            this.name = name.toString();
        }


        private String parseNameFromData(byte[] data, int offset) {
            StringJoiner name = new StringJoiner(".");
            while (offset < data.length) {
                int length = data[offset++] & 0xFF; // read length and advance

                if (length == 0) {
                    break; // null terminator indicates end of name
                }

                name.add(new String(data, offset, length, StandardCharsets.UTF_8));
                offset += length;
            }
            return name.toString();
        }

    }
}
