package ie.dacelonid.dns;

import ie.dacelonid.dns.bitutils.BitReader;
import ie.dacelonid.dns.bitutils.BitWriter;
import ie.dacelonid.dns.structure.Answer;
import ie.dacelonid.dns.structure.Header;
import ie.dacelonid.dns.structure.Question;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerTest {

    private Server server;

    @BeforeEach
    public void setup() {
        server = new Server(2053);
        Thread t = new Thread(server);
        t.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void serverTest() throws Exception {
        DatagramPacket datagramPacket = sendDNSRequest();
        byte[] response = datagramPacket.getData();

        Header header = new Header.HeaderBuilder().from(response);
        Header expectedHeader = new Header.HeaderBuilder().packetID(5878).queryResponseID(1).ansRecordCount(1).questionCount(1).recurDesired(1).build();
        assertEquals(expectedHeader, header);

        Question expecteQuestion = new Question.QuestionBuilder().name("example.com").type(1).classValue(1).build();
        Question question = new Question.QuestionBuilder().from(response);
        assertEquals(expecteQuestion, question);

        Answer expectedAnswer = new Answer.AnswerBuilder().name("example.com").type(1).classValue(1).timeToLive(60).length(4).data("8.8.8.8").build();
        Answer answer = new Answer.AnswerBuilder().from(response);
        assertEquals(expectedAnswer, answer);
    }


    public DatagramPacket sendDNSRequest() throws IOException {
        String dnsServer = "localhost";
        String domain = "example.com";

        Header requestHeader = new Header.HeaderBuilder().packetID(5878).questionCount(1).recurDesired(1).build();
        // Build DNS request
        ByteBuffer buffer = ByteBuffer.allocate(512);
        buffer.put(requestHeader.tobytes());

        // Write domain name
        for (String label : domain.split("\\.")) {
            buffer.put((byte) label.length());
            buffer.put(label.getBytes());
        }
        buffer.put((byte) 0); // Terminate name

        buffer.putShort((short) 1); // QTYPE = A
        buffer.putShort((short) 1); // QCLASS = IN

        byte[] dnsQuery = new byte[buffer.position()];
        buffer.flip();
        buffer.get(dnsQuery);

        // Send via UDP
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(2000);
        InetAddress address = InetAddress.getByName(dnsServer);
        DatagramPacket request = new DatagramPacket(dnsQuery, dnsQuery.length, address, 2053);
        socket.send(request);
        byte[] response = new byte[512];
        DatagramPacket reply = new DatagramPacket(response, response.length);
        socket.receive(reply);

        return reply;
    }
}
