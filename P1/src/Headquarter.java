import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;


/**
 * Holds the list of production points, it initializes itself as a server and accepts transport workers as clients
 */
public class Headquarter {


    public static void main(String[] args) throws FileNotFoundException {

        PrintStream fout = new PrintStream(new FileOutputStream("output.txt"));
        System.setOut(fout);

        ArrayList<ProductionPoint> productionPoints = new ArrayList<>();

        new Thread(() -> {
            ServerSocket serverSocket;

            int port = 5587;
            try {
                serverSocket = new ServerSocket(port);
                Semaphore transportLimit = new Semaphore(10);
                for(int k=0; k<15; k++) {
                    new Thread(()->{
                        try {
                            Thread.sleep(1000);
                            Socket clientSocket;
                            PrintWriter out;
                            BufferedReader in;
                            clientSocket = serverSocket.accept();
                            out = new PrintWriter(clientSocket.getOutputStream(), true);
                            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            while (true) {
                                //add semaphore.aquire here with limit
                                transportLimit.acquire();
                                String buf = in.readLine();
                                if (buf != null && !buf.equals("")) {
                                    System.out.println("!!!!!!!!!!!!!!!!!!!" + buf+" transportLimit passes remaining: "+transportLimit.availablePermits());
                                }
                                transportLimit.release();
                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }

                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();

        Random random = new Random();
        for (int i = 0; i < random.nextInt(4) + 2; i++) {
            ProductionPoint pp = new ProductionPoint((int) random.nextInt(401) + 100);
            productionPoints.add(pp);
            Thread tprpoint = new Thread(pp);
            tprpoint.start();

        }
        new Thread(new TransportCommander(productionPoints)).start();

    }

}
