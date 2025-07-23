package ie.dacelonid.dns.structure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class DNSMessage {
    private final Header header;
    private final List<Question> questions;
    private final List<Answer> answers;

    @Override
    public String toString() {
        return "DNSMessage{" +
                "header=" + header +
                ", questions=" + questions +
                ", answers=" + answers +
                '}';
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void addAnswer(Answer answer) {
        answers.add(answer);
    }

    private DNSMessage(byte[] data) {
        header = new Header.HeaderBuilder().from(data);
        questions = new ArrayList<>();
        answers = new ArrayList<>();
        for (int x = 1; x <= header.getQuestionCount(); x++) {
            questions.add(new Question.QuestionBuilder().from(data, x));
        }
        if (header.getAnsRecordCount() > 0) {
            for (int x = 0; x < header.getQuestionCount(); x++) {
                int position = 0;
                for (Question question : questions)
                    position = question.getPosition();
                answers.add(new Answer.AnswerBuilder().froma(data, x, position));
            }
        }
    }

    public DNSMessage(DNSMessage request) {
        header = new Header.HeaderBuilder().fromRequest(request);
        questions = new ArrayList<>();
        answers = new ArrayList<>();
        for (Question requestQuestion : request.questions) {
            questions.add(new Question.QuestionBuilder()
                    .name(requestQuestion.getName())
                    .type(requestQuestion.getType())
                    .classValue(requestQuestion.getClassValue())
                    .build());
        }
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
        buffer.put(getQuestionBytes());
        buffer.put(getAnswerBytes());
        return buffer.array();
    }


    private byte[] getQuestionBytes() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        for (Question question : questions) {
            try {
                stream.write(question.toBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return stream.toByteArray();
    }

    private byte[] getAnswerBytes() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (Answer answer : answers) {
            try {
                stream.write(answer.toBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return stream.toByteArray();
    }

    public List<Answer> getAnswers() {
        return answers;
    }
}
