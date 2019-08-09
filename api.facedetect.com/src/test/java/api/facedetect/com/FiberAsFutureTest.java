package api.facedetect.com;

import org.junit.Test;

import java.util.concurrent.ExecutionException;
import static junit.framework.TestCase.*;

public class FiberAsFutureTest {

    @Test
    public void testFiberAsFuture() {
        var f = FiberScope.background().schedule(() -> {
            return 5;
        });
        var updateFiber = f.toFuture().thenApply(r -> {
            return r * 10;
        }).thenApply(r -> {
            return r * 2;
        });

        try {
            var res = updateFiber.get();
            assertEquals(Integer.valueOf(100), res);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            fail();
        }
    }

}
