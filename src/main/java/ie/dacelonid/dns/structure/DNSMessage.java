package ie.dacelonid.dns.structure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ie.dacelonid.dns.structure.DNSParserUtils.parseRecords;

public class DNSMessage {
    private Header header;
    private final List<Question> questions;
    private final List<Answer> answers;

    public List<Question> getQuestions() {
        return questions;
    }

    public void addAnswer(Answer answer) {
        answers.add(answer);
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public DNSMessage() {
        questions = new ArrayList<>();
        answers = new ArrayList<>();
    }

    private DNSMessage(byte[] data) {
        header = new Header.HeaderBuilder().from(data);

        questions = parseRecords(data, header.getQuestionCount(),
                (d, numQuestions) -> new Question.QuestionBuilder().from(d, numQuestions).build());

        answers = parseRecords(data, header.getAnsRecordCount(),
                (d, numAnswers) -> new Answer.AnswerBuilder().from(d, numAnswers).build());
    }

    public DNSMessage(DNSMessage request) {
        header = new Header.HeaderBuilder().fromRequest(request);
        questions = new ArrayList<>(request.questions); // one-liner copy
        answers = new ArrayList<>();
    }

    public static DNSMessage from(DNSMessage request) {
        return new DNSMessage(request);
    }

    public static DNSMessage from(byte[] data) {
        return new DNSMessage(data);
    }

    public Header getHeader() {
        return header;
    }

    public byte[] tobytes() {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        buffer.put(header.tobytes());
        buffer.put(getBytes(questions));
        buffer.put(getBytes(answers));
        return buffer.array();
    }


    private byte[] getBytes(List<? extends DNSPart> elements) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (DNSPart dnsPart : elements) {
            try {
                stream.write(dnsPart.toBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return stream.toByteArray();
    }

    public List<Answer> getAnswers() {
        return answers;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DNSMessage that)) return false;
        return Objects.equals(header, that.header) && Objects.equals(questions, that.questions) && Objects.equals(answers, that.answers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, questions, answers);
    }

    @Override
    public String toString() {
        return "DNSMessage{" +
                "header=" + header +
                ", questions=" + questions +
                ", answers=" + answers +
                '}';
    }
}
