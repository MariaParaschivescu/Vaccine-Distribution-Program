import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TransportWorkers use TCP/IP to send the vaccines to the headquarter on 127.0.0.1:5587.
 * It polls a square that contains a vaccine and extracts the vaccine from that square, and then send that vaccine to the headquarter
 */
public class TransportWorker implements Runnable{
    private BlockingQueue<Square> vaccines;
    private TransportCommander commander;
    private int serverPort;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private int transportWorkerId;
    private static AtomicInteger transportWorkersNo = new AtomicInteger(1);


    public TransportWorker(BlockingQueue<Square> vaccines, TransportCommander commander) {
        this.vaccines = vaccines;
        this.commander = commander;
        transportWorkerId = transportWorkersNo.getAndIncrement();
    }

    @Override
    public void run() {
        this.serverPort = 5587;
        try {
            startConnection("127.0.0.1", 5587);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true){
            try {
                while(!vaccines.isEmpty()){


                    Square square = vaccines.poll();
                    if(square==null)continue;
                    int vaccine = square.pollVaccine();
                    String buf  = "Transport worker id:"+transportWorkerId+" sent vaccine with id: "+vaccine;
                    sendMessage(buf);
                    Random random = new Random();
                    Thread.sleep(random.nextInt(200)+100);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }
    public void sendMessage(String msg) throws IOException {
        out.println(msg);

    }

    public void sendVaccine(Square square){
        //send vaccine trough TCP to headquarter


    }
}
