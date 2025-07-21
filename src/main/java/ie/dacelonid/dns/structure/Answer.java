package ie.dacelonid.dns.structure;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.StringJoiner;

public class Answer {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Answer answer)) return false;
        return length == answer.length && classValue == answer.classValue &&  data == answer.data && type == answer.type && Objects.equals(name, answer.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, length, classValue, timeToLive, data, type);
    }

    @Override
    public String toString() {
        return "Answer{" +
                "name='" + name + '\'' +
                ", length=" + length +
                ", classValue=" + classValue +
                ", timeToLive=" + timeToLive +
                ", data=" + data +
                ", type=" + type +
                '}';
    }

    private final String name;
    private final int length;
    private final int classValue;
    private final int timeToLive;
    private final int data;
    private final int type;

    public Answer(AnswerBuilder answerBuilder) {
        this.name = answerBuilder.name;
        this.type = answerBuilder.type;
        this.length = answerBuilder.length;
        this.classValue = answerBuilder.classValue;
        this.timeToLive = answerBuilder.timeToLive;
        this.data = answerBuilder.data;

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
            output.write(intToBytes(timeToLive, 4));
            output.write(intToBytes(length, 2));
            output.write(intToBytes(data, 4));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output.toByteArray();
    }

    private byte[] intToBytes(int x, int size) {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        if (size == 2) {
            buffer.putShort((short) x);
        } else {
            buffer.putInt(x);
        }
        return buffer.flip().array();
    }

    public static class AnswerBuilder {
        private int length;
        private String name;
        private int type;
        private int classValue;
        private int timeToLive;
        private int data;

        public AnswerBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AnswerBuilder type(int type) {
            this.type = type;
            return this;
        }

        public AnswerBuilder classValue(int classValue) {
            this.classValue = classValue;
            return this;
        }

        public AnswerBuilder timeToLive(int timeToLive) {
            this.timeToLive = timeToLive;
            return this;
        }

        public AnswerBuilder length(int length) {
            this.length = length;
            return this;
        }

        public AnswerBuilder data(String data) {
            String[] parts = data.split("\\.");
            if (parts.length != 4) {
                throw new IllegalArgumentException("Invalid IPv4 address: " + data);
            }

            int b1 = Integer.parseInt(parts[0]) & 0xFF;
            int b2 = Integer.parseInt(parts[1]) & 0xFF;
            int b3 = Integer.parseInt(parts[2]) & 0xFF;
            int b4 = Integer.parseInt(parts[3]) & 0xFF;

            this.data = (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
            return this;
        }

        public Answer build() {
            return new Answer(this);
        }

        public Answer from(byte[] data, int whichAnswer, int totalQuestions) {
            Question question = new Question.QuestionBuilder().from(data, totalQuestions-1);//skip the Question part
            int position = question.getPosition();
            for (int x = 0; x <= whichAnswer; x++) {
                position = parseNameFromData(data, position);
                position = parseTypeFromData(data, position);
                position = parseClassFromData(data, position);
                position = parseTimeToLiveFromData(data, position);
                position = parseLengthFromData(data, position);
                position = parseDataFromData(data, position);
            }
            return new Answer(this);
        }

        public Answer froma(byte[] data, int whichAnswer, int position) {
            for (int x = 0; x <= whichAnswer; x++) {
                position = parseNameFromData(data, position);
                position = parseTypeFromData(data, position);
                position = parseClassFromData(data, position);
                position = parseTimeToLiveFromData(data, position);
                position = parseLengthFromData(data, position);
                position = parseDataFromData(data, position);
            }
            return new Answer(this);
        }

        private int parseDataFromData(byte[] data, int position) {
            ByteBuffer f = ByteBuffer.allocate(4);
            f.put(data[position++]);
            f.put(data[position++]);
            f.put(data[position++]);
            f.put(data[position++]);
            f.flip();
            this.data = f.getInt();
            return position;
        }

        private int parseLengthFromData(byte[] data, int position) {
            ByteBuffer f = ByteBuffer.allocate(4);
            f.put((byte) 0x00);
            f.put((byte) 0x00);
            f.put(data[position++]);
            f.put(data[position++]);
            f.flip();
            this.length = f.getInt();
            return position;
        }

        private int parseTimeToLiveFromData(byte[] data, int position) {
            ByteBuffer f = ByteBuffer.allocate(4);
            f.put(data[position++]);
            f.put(data[position++]);
            f.put(data[position++]);
            f.put(data[position++]);
            f.flip();
            this.timeToLive = f.getInt();
            return position;
        }

        private int parseClassFromData(byte[] data, int position) {
            ByteBuffer f = ByteBuffer.allocate(4);
            f.put((byte) 0x00);
            f.put((byte) 0x00);
            f.put(data[position++]);
            f.put(data[position++]);
            f.flip();
            this.classValue = f.getInt();
            return position;
        }

        private int parseTypeFromData(byte[] data, int position) {
            ByteBuffer f = ByteBuffer.allocate(4);
            f.put((byte) 0x00);
            f.put((byte) 0x00);
            f.put(data[position++]);
            f.put(data[position++]);
            f.flip();
            this.type = f.getInt();
            return position;
        }

        private int parseNameFromData(byte[] data, int position) {
            StringJoiner name = new StringJoiner(".");
            while (position < data.length) {

                int length = data[position++] & 0xFF; // read length and advance
                if((length & 192) == 192) { //compression
                    int nextByte = data[position++] & 0xFF;
                    int pointer = ((length & 0x3F) << 8) | nextByte;
                    name.add(parseNameFromDataa(data, pointer));
                    break;
                }

                if (length == 0) {
                    break; // null terminator indicates end of name
                }

                name.add(new String(data, position, length, StandardCharsets.UTF_8));
                position += length;
            }
            this.name = name.toString();
            return position;
        }

        private String parseNameFromDataa(byte[] data, int offset) {
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
