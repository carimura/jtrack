package api.facedetect.com;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class FiberSyncTest {

    @Test
    public void testSyncFibers()
    {
        var wg = new FiberWaitGroup();

        for (int i = 0; i < 100; i++) {
            var f = FiberScope.background().schedule(() -> {
                return "hello world";
            });
            wg.add(i, f);
        }

        var res = wg.awaitForResult();
        for (Object r: res) {
            assertEquals("hello world", (String) r);
        }
    }

}
