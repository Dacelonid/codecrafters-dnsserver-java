package ie.dacelonid.dns.structure;

import ie.dacelonid.dns.bitutils.DNSParserUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static ie.dacelonid.dns.bitutils.DNSParserUtils.intToBytes;
import static ie.dacelonid.dns.bitutils.DNSParserUtils.readUInt16;

public class Question implements DNSPart {

    private final int position;

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

        public Question from(byte[] data, int questionToRetrieve) {
            for (int x = 1; x <= questionToRetrieve; x++) {

                DNSParserUtils.NameParseResult result = DNSParserUtils.parseName(data, position);
                this.name = result.name();
                position = result.position();
                this.type = readUInt16(data, position);
                position += 2;
                this.classValue = readUInt16(data, position);
                position += 2;
            }
            return new Question(this);
        }
    }

}
