package ie.dacelonid.dns;

import ie.dacelonid.dns.structure.DNSMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
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
        DNSMessage request = DNSMessage.from(packet.getData());
        DNSMessage response = DNSMessage.from(request);
        sendResponse(response, packet.getSocketAddress());
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
