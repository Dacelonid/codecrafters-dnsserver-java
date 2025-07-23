package ie.dacelonid.dns.structure;


import ie.dacelonid.dns.bitutils.DataCursor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import static ie.dacelonid.dns.structure.DNSParserUtils.intToBytes;

public class Answer extends DNSPart {
    private final int length;
    private final int timeToLive;
    private final int data;

    public Answer(AnswerBuilder answerBuilder) {
        this.name = answerBuilder.name;
        this.type = answerBuilder.type;
        this.length = answerBuilder.length;
        this.classValue = answerBuilder.classValue;
        this.timeToLive = answerBuilder.timeToLive;
        this.data = answerBuilder.data;
    }

    @Override
    protected byte[] additionalInfo() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
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
            try {
                byte[] bytes = InetAddress.getByName(data).getAddress();
                if (bytes.length != 4) throw new IllegalArgumentException("Not an IPv4 address");
                this.data = ((bytes[0] & 0xFF) << 24) |
                        ((bytes[1] & 0xFF) << 16) |
                        ((bytes[2] & 0xFF) << 8) |
                        (bytes[3] & 0xFF);
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException("Invalid IP address: " + data, e);
            }

            return this;
        }

        public Answer build() {
            return new Answer(this);
        }

        public AnswerBuilder from(byte[] data, int whichAnswer) {
            DataCursor cursor = new DataCursor(data);
            cursor.skipQuestions();

            for (int i = 0; i < whichAnswer; i++) {
                cursor.skipAnswer();
            }
            this.name = cursor.readName();
            this.type = cursor.readUInt16();
            this.classValue = cursor.readUInt16();
            this.timeToLive = cursor.readUInt32();
            this.length = cursor.readUInt16();
            this.data = cursor.readUInt32();

            return this;

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
        return "Answer{" + "name='" + name + '\'' + ", length=" + length + ", classValue=" + classValue + ", timeToLive=" + timeToLive + ", data=" + data + ", type=" + type + '}';
    }
}
