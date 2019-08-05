package api.facedetect.com;

import java.util.HashMap;

public class FiberWaitGroup {

    protected HashMap<Integer, Fiber> result;

    public FiberWaitGroup() {
         result = new HashMap<>();
    }

    public synchronized void add(Integer index, Fiber newFiber) {
        result.put(index, newFiber);
    }


    public synchronized Object[] awaitForResult() {
        var mapped =  result.values().stream().map(x -> {
            try {
                return x.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
            return null;
        });

        return mapped.toArray();
    }
}
