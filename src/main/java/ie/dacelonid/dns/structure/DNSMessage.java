package ie.dacelonid.dns.structure;

import java.util.ArrayList;
import java.util.List;

public class DNSMessage {
    private final Header header;
    private final List<Question> questions;
    private List<Answer> answers;

    private DNSMessage(byte[] data){
        header = new Header.HeaderBuilder().from(data);
        questions = new ArrayList<>();
        answers = new ArrayList<>();
        for (int x = 0; x < header.getQuestionCount(); x++) {
            questions.add(new Question.QuestionBuilder().from(data, x));
        }

    }

    public List<Question> getQuestions() {
        return questions;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public static DNSMessage from(byte[] data) {

        return new DNSMessage(data);
    }

    public Header getHeader() {
        return header;
    }
}
