import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("Logs from the dns server!");

        try (DatagramSocket serverSocket = new DatagramSocket(2053)) {
            while (true) {
                final byte[] buf = new byte[512];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);
                System.out.println("Packet received");

                ByteBuffer receivedData = ByteBuffer.wrap(packet.getData());
                receivedData.order(ByteOrder.BIG_ENDIAN);

                // Parse the received DNS header
                short ID = receivedData.getShort(0);
                byte flags1 = receivedData.get(2);
                byte flags2 = receivedData.get(3);

                // Extract OPCODE and RD from the received flags
                int OPCODE = (flags1 >> 3) & 0x0F;
                boolean RD = (flags1 & 0x01) == 1;

                System.out.println("Received packet ID: " + ID);
                System.out.println("OPCODE: " + OPCODE);
                System.out.println("RD: " + RD);

                // Parse the question section
                int offset = 12; // Start of the question section
                StringBuilder domainName = new StringBuilder();
                while (true) {
                    int labelLength = receivedData.get(offset++) & 0xFF;
                    if (labelLength == 0) break;
                    byte[] labelBytes = new byte[labelLength];
                    receivedData.position(offset);
                    receivedData.get(labelBytes);
                    domainName.append(new String(labelBytes)).append(".");
                    offset += labelLength;
                }
                if (domainName.length() > 0) {
                    domainName.setLength(domainName.length() - 1); // Remove trailing dot
                }
                short qtype = receivedData.getShort(offset);
                short qclass = receivedData.getShort(offset + 2);

                System.out.println("Domain Name: " + domainName);
                System.out.println("Query Type: " + qtype);
                System.out.println("Query Class: " + qclass);

                // Prepare response flags
                byte responseFlags1 = (byte) (0x80 | (OPCODE << 3) | (RD ? 1 : 0));
                byte responseFlags2 = 0;

                // Set RCODE based on OPCODE
                if (OPCODE != 0) {
                    responseFlags2 |= 4; // RCODE 4 (not implemented) for non-standard queries
                }

                short QDCOUNT = 1;
                short ANCOUNT = 1;
                short NSCOUNT = 0;
                short ARCOUNT = 0;

                // Prepare the answer section
                int TTL = 60;
                short RDLENGTH = 4;
                byte[] RDATA = new byte[]{8, 8, 8, 8}; // 8.8.8.8

                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(responseStream);

                // Write header
                dos.writeShort(ID);
                dos.writeByte(responseFlags1);
                dos.writeByte(responseFlags2);
                dos.writeShort(QDCOUNT);
                dos.writeShort(ANCOUNT);
                dos.writeShort(NSCOUNT);
                dos.writeShort(ARCOUNT);

                // Write question section
                dos.write(encodeDomainName(domainName.toString()));
                dos.writeShort(qtype);
                dos.writeShort(qclass);

                // Write answer section
                dos.write(encodeDomainName(domainName.toString()));
                dos.writeShort(qtype);
                dos.writeShort(qclass);
                dos.writeInt(TTL);
                dos.writeShort(RDLENGTH);
                dos.write(RDATA);

                byte[] response = responseStream.toByteArray();

                System.out.println(Arrays.toString(response));
                System.out.println(response.length);
                final DatagramPacket packetResponse = new DatagramPacket(response, response.length, packet.getSocketAddress());
                serverSocket.send(packetResponse);
                System.out.println("Packet sent");
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }

    private static byte[] encodeDomainName(String domain) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (String label : domain.split("\\.")) {
            baos.write(label.length());
            try {
                baos.write(label.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        baos.write(0); // terminating null byte
        return baos.toByteArray();
    }
}