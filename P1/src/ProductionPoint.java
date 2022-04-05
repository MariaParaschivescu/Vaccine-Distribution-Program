import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Production point handles how vaccines are produced on its grid.
 */
public class ProductionPoint implements Runnable{
    private final int gridSize;
    private final Port<String> commChanel;
    private final Square[][] grid;
    private final Semaphore[][] gridSemaphore;
    private final ArrayList<RobotWorker> productionRobots;
    private final BlockingQueue<Square> squareDoses;
    private final AtomicInteger productionRobotsSize;
    private final int ppid;
    public ReentrantLock transportLock;
    public static AtomicInteger vaccineId = new AtomicInteger(1);
    public static AtomicInteger noOfPPs = new AtomicInteger(1);

    public ProductionPoint(int gridSize) {
        this.gridSize = gridSize;
        grid = new Square[gridSize][gridSize];
        productionRobots = new ArrayList<>();
        gridSemaphore = new Semaphore[gridSize][gridSize];
        productionRobotsSize = new AtomicInteger(0);
        squareDoses = new LinkedBlockingQueue<Square>();
        commChanel = new Port<>();
        for(int i=0; i<gridSize; i++){
            for(int j=0; j<gridSize; j++){
                gridSemaphore[i][j] = new Semaphore(1);
                grid[i][j] = new Square();
            }
        }
        ppid = noOfPPs.getAndIncrement();
        transportLock = new ReentrantLock();
    }

    /**
     * Adds a production robot at an empty place in the grid.
     * Uses semaphores to ensure that there will be no collision with other robots.
     * @throws InterruptedException
     */
    public void addProductionRobot() throws InterruptedException {
        int x,y;
        do{
            x = getRandomNumber(0, gridSize);
            y = getRandomNumber(0, gridSize);

            boolean aquired = gridSemaphore[x][y].tryAcquire();
            if(aquired && grid[x][y].getPresentRobot()==null){

                RobotWorker newrobot = new RobotWorker(
                        new Position(x,y),this,commChanel
                );
                Thread tnewrobot = new Thread(newrobot);
                tnewrobot.start();
                grid[x][y].setPresentRobot(newrobot);
                productionRobots.add(newrobot);
                productionRobotsSize.incrementAndGet();
                gridSemaphore[x][y].release();
                break;
            }
        }while(true);
    }


    /**
     * This adds production robots, it adds one and then waits between 500 and 1000milis and then adds another one and so on.
     */
    public void addProductionRobotsScheduler(){
        while(productionRobotsSize.get()<gridSize/2){
            try{
                addProductionRobot();
                Thread.sleep(getRandomNumber(500,1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Used to get a random number between min and max
     * @param min
     * @param max
     * @return
     */
    public int getRandomNumber(int min, int max){
        Random random = new Random();
        return random.nextInt(max-min)+min;
    }

    /**
     * Asks all production robots in the facility for their positions.
     * The transportLock is used so that no transport robot works while the facility asks for positions.
     */
    public void askProductionRobotsForPositions(){
        transportLock.lock();
        System.out.println("There are a total of "+productionRobots.size()+ " production robots");
        for(int i=0; i<productionRobots.size(); i++){
            RobotWorker it = productionRobots.get(i);
            Position position = it.getPosition();
            System.out.println("There is a robot on :"+position.getAsString());
        }
        transportLock.unlock();
    }

    @Override
    public void run() {

        /**
         * Ask the production robots for positions and waits for 3 seconds and then repeats
         */
        new Thread(()->{
            try {
                while (true) {
                    askProductionRobotsForPositions();
                    Thread.sleep(3000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();

        /**
         * Receive notifications from production robots when they produce vaccines
         */
        new Thread(()->{
            try {
                while (true){
                    String response = commChanel.receive();
                    System.out.println("Notification received in production point:"+ppid+" response:"+response);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        addProductionRobotsScheduler();
    }

    public int getGridSize() {
        return gridSize;
    }

    public Semaphore[][] getGridSemaphore() {
        return gridSemaphore;
    }

    public Square[][] getGrid() {
        return grid;
    }

    public BlockingQueue<Square> getSquareDoses() {
        return squareDoses;
    }

    public int getPpid() {
        return ppid;
    }


}
