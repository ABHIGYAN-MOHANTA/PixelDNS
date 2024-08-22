import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class Main {
    public static void main(String[] args) {

        System.out.println("DNS Server listening: Try this -> dig @localhost -p 2053 example.com");
        if (args.length < 1) {
            System.exit(1);
        }

        String[] resolverParts = args[0].split(":");
        if (resolverParts.length != 2) {
            System.exit(1);
        }

        String resolverIP = resolverParts[0];
        int resolverPort;
        try {
            resolverPort = Integer.parseInt(resolverParts[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number.");
            System.exit(1);
            return;
        }

        SocketAddress resolver = new InetSocketAddress(resolverIP, resolverPort);

        try (DatagramSocket serverSocket = new DatagramSocket(new InetSocketAddress("0.0.0.0", 2053))) {
            while (true) {
                byte[] buf = new byte[512];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);
                System.out.println("Packet Recieved");

                DNSMessage question = new DNSMessage(buf);
                for (String qd : question.qd) {
                    DNSMessage forward = question.clone();
                    forward.qd = new ArrayList<>();
                    forward.qd.add(qd);
                    byte[] buffer = forward.array();
                    DatagramPacket forwardPacket = new DatagramPacket(buffer, buffer.length, resolver);
                    serverSocket.send(forwardPacket);

                    buffer = new byte[512];
                    forwardPacket = new DatagramPacket(buffer, buffer.length);
                    serverSocket.receive(forwardPacket);
                    forward = new DNSMessage(buffer);
                    for (String an : forward.an.keySet()) {
                        question.an.put(an, forward.an.get(an));
                    }
                }

                char[] requestFlags = String.format("%16s", Integer.toBinaryString(question.flags)).replace(' ', '0').toCharArray();
                requestFlags[0] = '1';  // QR
                requestFlags[13] = '1'; // RCODE
                question.flags = (short) Integer.parseInt(new String(requestFlags), 2);

                byte[] buffer = question.array();
                DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, packet.getSocketAddress());
                serverSocket.send(sendPacket);
                System.out.println("Packet Sent");
            }
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }
}

class DNSMessage {
    public short id;
    public short flags;
    public List<String> qd = new ArrayList<>();
    public Map<String, byte[]> an = new HashMap<>();

    public DNSMessage() {
        // Empty constructor
    }

    public DNSMessage(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        id = buffer.getShort();
        flags = buffer.getShort();
        int qdcount = buffer.getShort();
        int ancount = buffer.getShort();
        buffer.getShort(); // nscount
        buffer.getShort(); // arcount

        for (int i = 0; i < qdcount; i++) {
            qd.add(decodeDomainName(buffer));
            buffer.getShort(); // Type
            buffer.getShort(); // Class
        }

        for (int i = 0; i < ancount; i++) {
            String domain = decodeDomainName(buffer);
            buffer.getShort(); // Type = A
            buffer.getShort(); // Class = IN
            buffer.getInt();   // TTL
            buffer.getShort(); // Length
            byte[] ip = new byte[4];
            buffer.get(ip); // Data
            an.put(domain, ip);
        }
    }

    public byte[] array() {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        buffer.putShort(id);
        buffer.putShort(flags);
        buffer.putShort((short) qd.size());
        buffer.putShort((short) an.size());
        buffer.putShort((short) 0); // nscount
        buffer.putShort((short) 0); // arcount

        for (String domain : qd) {
            buffer.put(encodeDomainName(domain));
            buffer.putShort((short) 1); // Type = A
            buffer.putShort((short) 1); // Class = IN
        }

        for (String domain : an.keySet()) {
            buffer.put(encodeDomainName(domain));
            buffer.putShort((short) 1);  // Type = A
            buffer.putShort((short) 1);  // Class = IN
            buffer.putInt(60);          // TTL
            buffer.putShort((short) 4);  // Length
            buffer.put(an.get(domain)); // Data
        }
        return buffer.array();
    }

    private byte[] encodeDomainName(String domain) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (String label : domain.split("\\.")) {
            out.write(label.length());
            out.writeBytes(label.getBytes());
        }
        out.write(0); // Terminating null byte
        return out.toByteArray();
    }

    private String decodeDomainName(ByteBuffer buffer) {
        byte labelLength;
        StringJoiner labels = new StringJoiner(".");
        boolean compressed = false;
        int position = 0;
        while ((labelLength = buffer.get()) != 0) {
            if ((labelLength & 0xC0) == 0xC0) {
                compressed = true;
                int offset = ((labelLength & 0x3F) << 8) | (buffer.get() & 0xFF);
                position = buffer.position();
                buffer.position(offset);
            } else {
                byte[] label = new byte[labelLength];
                buffer.get(label);
                labels.add(new String(label));
            }
        }
        if (compressed) {
            buffer.position(position);
        }
        return labels.toString();
    }

    public DNSMessage clone() {
        DNSMessage clone = new DNSMessage();
        clone.id = id;
        clone.flags = flags;
        clone.qd.addAll(qd);
        for (Map.Entry<String, byte[]> entry : an.entrySet()) {
            clone.an.put(entry.getKey(), entry.getValue().clone());
        }
        return clone;
    }
}
