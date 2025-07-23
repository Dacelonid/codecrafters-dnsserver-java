package ie.dacelonid.dns.structure;

import ie.dacelonid.dns.bitutils.DNSParserUtils;
import ie.dacelonid.dns.bitutils.DataCursor;

import java.util.Objects;

import static ie.dacelonid.dns.bitutils.DNSParserUtils.readUInt16;

public class Question extends DNSPart {
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

    @Override
    protected byte[] additionalInfo() {
        return new byte[0];
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

        public QuestionBuilder from(byte[] data, int questionToRetrieve) {
            DataCursor cursor = new DataCursor(data);
            cursor.skipHeader();

            for (int i = 0; i < questionToRetrieve - 1; i++) {
                cursor.skipQuestion(); // skip questions until target
            }

            this.name = cursor.readName();
            this.type = cursor.readUInt16();
            this.classValue = cursor.readUInt16();
            return this;
        }

    }

}
