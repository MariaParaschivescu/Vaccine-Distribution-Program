import java.util.LinkedList;
import java.util.Queue;

public class Port<T> {
    private Queue<T> queue = new LinkedList<>();
    int ready = 0;

    /**
     * Used to send notifications
     * @param v
     */
    public synchronized void send(T v){
        queue.add(v);
        ++ready;
        notifyAll();
    }

    /**
     * Used to receive notifications
     * @return
     * @throws InterruptedException
     */
    public synchronized T receive() throws InterruptedException{
        while(ready==0)wait();
        --ready;
        return queue.remove();
    }
}
