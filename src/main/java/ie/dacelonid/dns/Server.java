package ie.dacelonid.dns;

import ie.dacelonid.dns.structure.Answer;
import ie.dacelonid.dns.structure.Header;
import ie.dacelonid.dns.structure.Question;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final int port;
    private boolean keepRunning = true;
    private DatagramSocket serverSocket;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(this.port)) {
            this.serverSocket = socket;
            while (this.keepRunning) {
                final byte[] buf = new byte[512];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);
                threadPool.execute(() -> handleClient(packet));
            }
        } catch (Exception e) {
            System.out.println("IOException: " + e.getMessage());
            keepRunning = false;
        }
    }

    private void handleClient(DatagramPacket packet) {
        byte[] data = packet.getData();
        final byte[] bufResponse = new byte[512];
        Header requestHeader = new Header.HeaderBuilder().from(data);
        int numQuestions = requestHeader.getQuestionCount();
        Header responseHeader = new Header.HeaderBuilder().packetID(requestHeader.getPacketID()).queryResponseID(1).questionCount(numQuestions).ansRecordCount(numQuestions).opCode(requestHeader.getOpCode()).recurDesired(requestHeader.getRecurDesired()).respCode(requestHeader.getOpCode() == 0 ? 0 : 4).questionCount(requestHeader.getQuestionCount()).build();

        List<Question> questionsResponse = new ArrayList<>();
        List<Answer> answerResponses = new ArrayList<>();

        for (int x = 0; x < numQuestions; x++) {
            Question requestQuestion = new Question.QuestionBuilder().from(data, x);
            questionsResponse.add(new Question.QuestionBuilder().name(requestQuestion.getName()).type(requestQuestion.getType()).classValue(requestQuestion.getClassValue()).build());
            Answer build = new Answer.AnswerBuilder().name(requestQuestion.getName()).type(requestQuestion.getType()).classValue(requestQuestion.getClassValue()).timeToLive(60).length(4).data("8.8.8.8").build();
            answerResponses.add(build);
        }


        byte[] headerBytes = responseHeader.tobytes();
        byte[] questionBytes = getQuestionBytes(questionsResponse);
        byte[] answerBytes = getBytes(answerResponses);

        System.arraycopy(headerBytes, 0, bufResponse, 0, headerBytes.length);
        System.arraycopy(questionBytes, 0, bufResponse, headerBytes.length, questionBytes.length);
        System.arraycopy(answerBytes, 0, bufResponse, headerBytes.length + questionBytes.length, answerBytes.length);
        final DatagramPacket packetResponse = new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());
        try {
            serverSocket.send(packetResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static byte[] getQuestionBytes(List<Question> questionsResponse) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        for (Question question : questionsResponse) {
            try {
                stream.write(question.toBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return stream.toByteArray();
    }

    private static byte[] getBytes(List<Answer> answerResponses) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (Answer answer : answerResponses) {
            try {
                stream.write(answer.toBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return stream.toByteArray();
    }

    public void stop() {
        this.keepRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close(); // This will unblock serverSocket.receive(packet);
        }
        threadPool.shutdown();
    }
}
