import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the production robot, it moves and produces vaccines, then notifies the production point
 */
public class RobotWorker implements Runnable{
    private Position position;
    private ProductionPoint productionPoint;
    private final Port<String> comPort;
    private int robotId;
    private int moves;

    public static AtomicInteger noOfRobotWorkers = new AtomicInteger(1);
    public RobotWorker(Position position, ProductionPoint productionPoint, Port<String> comPort) {
        this.position = position;
        this.productionPoint = productionPoint;
        this.comPort = comPort;
        moves=0;
        robotId = noOfRobotWorkers.getAndIncrement();
    }

    @Override
    public void run() {
        try {
            while(true){
                moveandproduce();

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Moves to an available unlocked position and leaves a vaccine in its previous position. Then sleeps x*30 miliseconds.
     * @return
     * @throws InterruptedException
     */
    public boolean moveandproduce() throws InterruptedException {
        int[] dirx = {0,0,1,-1};
        int[] diry = {-1,1,0,0};
        for(int i=0; i<=3; i++){
            //future positions
            int x = position.getRow()+dirx[i];
            int y = position.getCol()+diry[i];
            if(x<0||x>=productionPoint.getGridSize()||y<0||y>=productionPoint.getGridSize())
                continue;

            boolean aquired = productionPoint.getGridSemaphore()[x][y].tryAcquire();
            if(aquired){
                if(productionPoint.getGrid()[x][y].getPresentRobot()==null){
                    productionPoint.getGrid()[x][y].setPresentRobot(this);
                    position.setRow(x);
                    position.setCol(y);
                    try {
                        produceDose();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                productionPoint.getGridSemaphore()[x][y].release();

                return true;
            }

        }
        System.out.println("Robot got stuck and will sleep");
        Thread.sleep(productionPoint.getRandomNumber(10,50));

        return false;
    }

    /**
     * Gets the square it is sitting on and produces a vaccine, then places the vaccine inside the square.
     * @throws InterruptedException
     */
    public void produceDose() throws InterruptedException {
        int vaccineId = ProductionPoint.vaccineId.getAndIncrement();
        Square square = productionPoint.getGrid()[position.getRow()][position.getCol()];
        square.setVaccineDose(vaccineId);
        productionPoint.getSquareDoses().add(square);
        System.out.println("Robotworker with moves:"+moves++ +" pos"+position.getAsString()+" id "+robotId+ " produced vaccine with id:"+vaccineId
        +" in production point: "+productionPoint.getPpid());

        /**
         * Locks the transportLock while it notifies the productionPoint that it produced a vaccine
         */
        new Thread(()->{
            productionPoint.transportLock.lock();
            comPort.send("Robotworker with id " + robotId + " produced " + vaccineId);
            productionPoint.transportLock.unlock();
        }).start();

        Random random = new Random();
        Thread.sleep((random.nextInt(10)+1)*30);//Sleep a multiple of 30
    }

    public Position getPosition() {
        return position;
    }
}
