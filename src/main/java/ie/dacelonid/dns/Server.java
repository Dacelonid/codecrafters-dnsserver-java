package ie.dacelonid.dns;

import ie.dacelonid.dns.bitutils.BitReader;
import ie.dacelonid.dns.structure.Answer;
import ie.dacelonid.dns.structure.Header;
import ie.dacelonid.dns.structure.Question;

import javax.imageio.stream.IIOByteBuffer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            keepRunning = false;
        }
    }

    private void handleClient(DatagramPacket packet) {
        byte[] data = packet.getData();
        Header requestheader = new Header.HeaderBuilder().from(data);
        System.out.println("Received data:\n" + requestheader);

        final byte[] bufResponse = new byte[512];
        Header responseHeader = new Header.HeaderBuilder().packetID(1234).queryResponseID(1).questionCount(1).ansRecordCount(1).build();

        Question requestQuestion = new Question.QuestionBuilder().from(data);
        Question responseQuestion = new Question.QuestionBuilder().name(requestQuestion.getName()).type(requestQuestion.getType()).classValue(requestQuestion.getClassValue()).build();

        Answer answerResponse = new Answer.AnswerBuilder().name(requestQuestion.getName()).type(requestQuestion.getType()).classValue(requestQuestion.getClassValue()).timeToLive(60).length(4).data("8.8.8.8").build();

        byte[] headerBytes = responseHeader.tobytes();
        byte[] questionBytes = responseQuestion.toBytes();
        byte[] answerBytes = answerResponse.toBytes();
        System.arraycopy(headerBytes, 0, bufResponse, 0, headerBytes.length);
        System.arraycopy(questionBytes, 0, bufResponse, headerBytes.length, questionBytes.length);
        System.arraycopy(answerBytes, 0, bufResponse, headerBytes.length+questionBytes.length, answerBytes.length);
        final DatagramPacket packetResponse = new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());
        try {
            serverSocket.send(packetResponse);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void stop() {
        this.keepRunning = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close(); // This will unblock serverSocket.receive(packet);
        }
        threadPool.shutdown();
    }
}
