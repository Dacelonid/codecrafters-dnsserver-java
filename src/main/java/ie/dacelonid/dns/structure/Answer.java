package ie.dacelonid.dns.structure;


import ie.dacelonid.dns.bitutils.DNSParserUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static ie.dacelonid.dns.bitutils.DNSParserUtils.*;

public class Answer implements DNSPart {
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

        public Answer from(byte[] data, int whichAnswer, int position) {
            for (int x = 0; x <= whichAnswer; x++) {
                DNSParserUtils.NameParseResult result = DNSParserUtils.parseName(data, position);
                this.name = result.name();
                position = result.position();
                this.type = readUInt16(data, position);
                position += 2;
                this.classValue = readUInt16(data, position);
                position += 2;
                this.timeToLive = readUInt32(data, position);
                position += 4;
                this.length = readUInt16(data, position);
                position += 2;
                this.data = readUInt32(data, position);
                position += 4;
            }
            return new Answer(this);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Answer answer)) return false;
        return length == answer.length && classValue == answer.classValue && data == answer.data && type == answer.type && Objects.equals(name, answer.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, length, classValue, timeToLive, data, type);
    }

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
}
