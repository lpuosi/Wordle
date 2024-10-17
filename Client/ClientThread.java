package Client;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import java.util.concurrent.ConcurrentLinkedQueue;


public class ClientThread extends Thread{
    private String address;
    private volatile Boolean done= false;
    private int size = 1024;
    private ConcurrentLinkedQueue<String> playerStats;
    private MulticastSocket socket;

    public ClientThread(String address, MulticastSocket socket, ConcurrentLinkedQueue<String> playerStats){
        this.playerStats = playerStats;
        this.socket = socket;
        this.address = address;
    }
    @Override
    public void run(){
        try{
            InetAddress group = InetAddress.getByName(address);
            if (!group.isMulticastAddress()) {
                throw new IllegalArgumentException(
                "Indirizzo multicast non valido: " + group.getHostAddress());
            }
            socket.joinGroup(group);
            while(!done){
                DatagramPacket packet = new DatagramPacket(new byte[size], size);
                socket.receive(packet);
                System.out.println("Ho ricevuto");
                String mess = new String(packet.getData(), packet.getOffset(),
                packet.getLength());
                playerStats.add(mess);
            }

        }
        catch(SocketException e){
            return;
        }
        catch(Exception e){
            e.printStackTrace();

        }
        


    }
   
}
