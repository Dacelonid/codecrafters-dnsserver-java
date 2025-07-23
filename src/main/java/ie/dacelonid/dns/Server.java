package ie.dacelonid.dns;

import ie.dacelonid.dns.structure.Answer;
import ie.dacelonid.dns.structure.DNSMessage;
import ie.dacelonid.dns.structure.Header;
import ie.dacelonid.dns.structure.Question;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final int serverPort;
    private boolean keepRunning = true;
    private DatagramSocket serverSocket;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final String ip;
    private final int port;

    public Server(int serverPort, String ip, String port) {
        this.serverPort = serverPort;
        this.ip = ip;
        this.port = Integer.parseInt(port);
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(this.serverPort)) {
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
        DNSMessage request = DNSMessage.from(packet.getData());
        DNSMessage response = DNSMessage.from(request);
        for (Question question : response.getQuestions()) {
            //For each question figure out if to handle the response or to forward
            if (ip != null && port != 0) {
                response.addAnswer(getResponses(request.getHeader().getPacketID(), question));
            } else {
                response.addAnswer(new Answer.AnswerBuilder().name(question.getName()).type(question.getType()).classValue(question.getClassValue()).timeToLive(60).length(4).data("8.8.8.8").build());
            }
        }
        sendResponse(response, packet.getSocketAddress());
    }

    private Answer getResponses(int packetID, Question question) {
        Header forwardHeader = new Header.HeaderBuilder().packetID(packetID).questionCount(1).recurDesired(1).build();
        ByteBuffer forwardMessage = ByteBuffer.allocate(512);
        forwardMessage.put(forwardHeader.tobytes());
        forwardMessage.put(question.toBytes());

        byte[] forwardBytes = forwardMessage.array();
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket request = new DatagramPacket(forwardBytes, forwardBytes.length, InetAddress.getByName(ip), port);
            socket.send(request);

            byte[] responseBuffer = new byte[512];
            DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length);
            socket.setSoTimeout(2000); // Avoid blocking forever
            socket.receive(response);
            DNSMessage responseMessage = DNSMessage.from(responseBuffer);
            if (responseMessage.getHeader().getAnsRecordCount() > 0) {
                return responseMessage.getAnswers().getFirst();
            }
            return new Answer.AnswerBuilder().name(question.getName()).type(question.getType()).classValue(question.getClassValue()).timeToLive(60).length(4).data("8.8.8.8").build();
        } catch (Exception e) {
            System.out.println("Could not get response from upstream DNS \n>" + e.getMessage());
        }

        return new Answer.AnswerBuilder().name(question.getName()).type(question.getType()).classValue(question.getClassValue()).timeToLive(60).length(4).data("8.8.8.8").build();
    }

    private void sendResponse(DNSMessage response, SocketAddress replyAddress) {
        byte[] bytes = response.tobytes();
        final DatagramPacket packetResponse = new DatagramPacket(bytes, bytes.length, replyAddress);
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
