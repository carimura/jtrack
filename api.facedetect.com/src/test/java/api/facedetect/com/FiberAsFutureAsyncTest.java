package api.facedetect.com;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

public class FiberAsFutureAsyncTest {


    @Test
    public void testFiberWithAsync() {
        var f = FiberScope.background().schedule(() -> {
            return 5;
        });

        var postProc = f.toFuture().thenApplyAsync(x -> {
            return x * 10;
        }).thenApply(x -> {
            return x * 2;
        });

        try {
            var res = postProc.get();
            assertEquals(Integer.valueOf(100), res);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            fail();
        }
    }
}
