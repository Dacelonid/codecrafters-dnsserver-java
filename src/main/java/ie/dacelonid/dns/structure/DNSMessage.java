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

    private DNSMessage(byte[] data) {
        header = new Header.HeaderBuilder().from(data);
        questions = new ArrayList<>();
        answers = new ArrayList<>();
        for (int x = 0; x < header.getQuestionCount(); x++) {
            questions.add(new Question.QuestionBuilder().from(data, x));
        }

    }

    public DNSMessage(DNSMessage request) {
        header = new Header.HeaderBuilder().fromRequest(request);
        questions = new ArrayList<>();
        answers = new ArrayList<>();
        for (Question requestQuestion : request.questions) {
            questions.add(new Question.QuestionBuilder().name(requestQuestion.getName()).type(requestQuestion.getType()).classValue(requestQuestion.getClassValue()).build());
            Answer build = new Answer.AnswerBuilder().name(requestQuestion.getName()).type(requestQuestion.getType()).classValue(requestQuestion.getClassValue()).timeToLive(60).length(4).data("8.8.8.8").build();
            answers.add(build);
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
}
