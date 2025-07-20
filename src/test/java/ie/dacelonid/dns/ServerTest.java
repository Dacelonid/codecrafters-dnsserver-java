package ie.dacelonid.dns;

import ie.dacelonid.dns.structure.Answer;
import ie.dacelonid.dns.structure.Header;
import ie.dacelonid.dns.structure.Question;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
    public void serverTestWithCompression() throws Exception {
        DatagramPacket datagramPacket = sendDNSRequest(true);
        byte[] response = datagramPacket.getData();

        Header header = new Header.HeaderBuilder().from(response);
        Header expectedHeader = new Header.HeaderBuilder().packetID(5878).queryResponseID(1).ansRecordCount(3).questionCount(3).recurDesired(1).build();
        assertEquals(expectedHeader, header);

        Question expectedQuestion = new Question.QuestionBuilder().name("www.example.com").type(1).classValue(1).build();
        Question question = new Question.QuestionBuilder().from(response, 0);
        assertEquals(expectedQuestion, question);

        expectedQuestion = new Question.QuestionBuilder().name("www.example.org").type(1).classValue(1).build();
        question = new Question.QuestionBuilder().from(response, 1);
        assertEquals(expectedQuestion, question);

        expectedQuestion = new Question.QuestionBuilder().name("www.somewhere.example.com").type(1).classValue(1).build();
        question = new Question.QuestionBuilder().from(response, 2);
        assertEquals(expectedQuestion, question);

        Answer expectedAnswer = new Answer.AnswerBuilder().name("www.example.com").type(1).classValue(1).timeToLive(60).length(4).data("8.8.8.8").build();
        Answer answer = new Answer.AnswerBuilder().from(response, 0, 2);
        assertEquals(expectedAnswer, answer);

        expectedAnswer = new Answer.AnswerBuilder().name("www.example.org").type(1).classValue(1).timeToLive(60).length(4).data("8.8.8.8").build();
        answer = new Answer.AnswerBuilder().from(response, 1, 2);
        assertEquals(expectedAnswer, answer);

        expectedAnswer = new Answer.AnswerBuilder().name("www.somewhere.example.com").type(1).classValue(1).timeToLive(60).length(4).data("8.8.8.8").build();
        answer = new Answer.AnswerBuilder().from(response, 2, 2);
        assertEquals(expectedAnswer, answer);
    }
    @Test


    public void serverTestWithoutCompression() throws Exception {
        DatagramPacket datagramPacket = sendDNSRequest(false);
        byte[] response = datagramPacket.getData();

        Header header = new Header.HeaderBuilder().from(response);
        Header expectedHeader = new Header.HeaderBuilder().packetID(5878).queryResponseID(1).ansRecordCount(3).questionCount(3).recurDesired(1).build();
        assertEquals(expectedHeader, header);

        Question expectedQuestion = new Question.QuestionBuilder().name("www.example.com").type(1).classValue(1).build();
        Question question = new Question.QuestionBuilder().from(response, 0);
        assertEquals(expectedQuestion, question);

        expectedQuestion = new Question.QuestionBuilder().name("www.example.org").type(1).classValue(1).build();
        question = new Question.QuestionBuilder().from(response, 1);
        assertEquals(expectedQuestion, question);

        expectedQuestion = new Question.QuestionBuilder().name("www.somewhere.example.com").type(1).classValue(1).build();
        question = new Question.QuestionBuilder().from(response, 2);
        assertEquals(expectedQuestion, question);

        Answer expectedAnswer = new Answer.AnswerBuilder().name("www.example.com").type(1).classValue(1).timeToLive(60).length(4).data("8.8.8.8").build();
        Answer answer = new Answer.AnswerBuilder().from(response, 0, 2);
        assertEquals(expectedAnswer, answer);

        expectedAnswer = new Answer.AnswerBuilder().name("www.example.org").type(1).classValue(1).timeToLive(60).length(4).data("8.8.8.8").build();
        answer = new Answer.AnswerBuilder().from(response, 1, 2);
        assertEquals(expectedAnswer, answer);

        expectedAnswer = new Answer.AnswerBuilder().name("www.somewhere.example.com").type(1).classValue(1).timeToLive(60).length(4).data("8.8.8.8").build();
        answer = new Answer.AnswerBuilder().from(response, 2, 2);
        assertEquals(expectedAnswer, answer);
    }


    public DatagramPacket sendDNSRequest(boolean compression) throws IOException {
        String dnsServer = "localhost";

        ByteBuffer buffer = ByteBuffer.allocate(512);
        // Build DNS request
        Header requestHeader = new Header.HeaderBuilder().packetID(5878).questionCount(3).recurDesired(1).build();
        buffer.put(requestHeader.tobytes());
        if (compression) {
            buffer.put(getCompressedQuestion());
        } else {
            buffer.put(getuncompressedMultiplQuestion());
        }
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

    private byte[] getCompressedQuestion() {
        return new byte[]{
                // Question 1: www.example.com (offset 12)
                0x03, 'w', 'w', 'w',                 // 12–15
                0x07, 'e', 'x', 'a', 'm', 'p', 'l', 'e', // 16–23 ← `.example` starts at 16
                0x03, 'c', 'o', 'm',                 // 24–26
                0x00,                               // end of name
                0x00, 0x01,                         // QTYPE = A
                0x00, 0x01,                         // QCLASS = IN

                // Question 2: www.example.org (not compressible — org is unique)
                0x03, 'w', 'w', 'w',
                0x07, 'e', 'x', 'a', 'm', 'p', 'l', 'e',
                0x03, 'o', 'r', 'g',
                0x00,
                0x00, 0x01,
                0x00, 0x01,

                // Question 3: www.somewhere.example.com
                0x03, 'w', 'w', 'w',
                0x09, 's', 'o', 'm', 'e', 'w', 'h', 'e', 'r', 'e',
                (byte) 0xC0, 0x10, // pointer to offset 16: start of `.example.com`
                0x00, 0x01,
                0x00, 0x01
        };
    }

    private byte[] getuncompressedMultiplQuestion() {
        return new byte[]{
                // Question 1: www.example.com
                0x03, 'w', 'w', 'w',
                0x07, 'e', 'x', 'a', 'm', 'p', 'l', 'e',
                0x03, 'c', 'o', 'm',
                0x00,
                0x00, 0x01,       // QTYPE = A
                0x00, 0x01,       // QCLASS = IN

                // Question 2: www.example.org
                0x03, 'w', 'w', 'w',
                0x07, 'e', 'x', 'a', 'm', 'p', 'l', 'e',
                0x03, 'o', 'r', 'g',
                0x00,
                0x00, 0x01,       // QTYPE = A
                0x00, 0x01,       // QCLASS = IN

                // Question 3: www.somewhere.example.com
                0x03, 'w', 'w', 'w',
                0x09, 's', 'o', 'm', 'e', 'w', 'h', 'e', 'r', 'e',
                0x07, 'e', 'x', 'a', 'm', 'p', 'l', 'e',
                0x03, 'c', 'o', 'm',
                0x00,
                0x00, 0x01,       // QTYPE = A
                0x00, 0x01        // QCLASS = IN
        };

    }
}
