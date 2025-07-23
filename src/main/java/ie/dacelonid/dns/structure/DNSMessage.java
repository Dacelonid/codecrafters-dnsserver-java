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
        for (int questionNumber = 1; questionNumber <= header.getQuestionCount(); questionNumber++) {
            questions.add(new Question.QuestionBuilder().from(data, questionNumber).build());
        }
        if (header.getAnsRecordCount() > 0) {
            for (int answerNumber = 0; answerNumber < header.getAnsRecordCount(); answerNumber++) {
                answers.add(new Answer.AnswerBuilder().from(data, answerNumber, header.getQuestionCount()).build());
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
}
