import java.util.ArrayList;

/**
 * TransportCommander initializes the transportWorkers and starts them
 */
public class TransportCommander implements Runnable{
    private ArrayList<TransportWorker> transportWorkers;
    private ArrayList<ProductionPoint> productionPoints;

    public TransportCommander(ArrayList<ProductionPoint> productionPoints){
        transportWorkers = new ArrayList<>();
        this.productionPoints = productionPoints;
    }

    public void initCommander(){
        for(int i=1; i<=15; i++){
            //Divide the transport workers equally between the productionPoints
            transportWorkers.add(new TransportWorker(productionPoints.get(i%productionPoints.size()).getSquareDoses(), this));
        }
    }

    @Override
    public void run() {
        initCommander();
        for(var it:transportWorkers){
            new Thread(it).start();
        }
    }



}
