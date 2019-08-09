package api.facedetect.com;

import java.util.concurrent.ExecutionException;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;


public class FiberTest
{

    @Test
    public void testFiberAsFuture()
    {
        var newF = FiberScope.background().schedule(() -> {
            return "Hello world";
        });

        try {
            newF.join();
            var result = newF.toFuture().thenApply(x -> {
                var parts = x.split(" ");
                var first = parts[0];
                return String.format("%s Denis!", first);
            });
            assertEquals(result.get(), "Hello Denis!");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
