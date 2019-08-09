package api.facedetect.com;

import org.junit.Test;
import static junit.framework.TestCase.*;

public class FaceDetectInitTest {


    @Test
    public void testInit() {
        var faceDetect = new FaceDetect();
        assertFalse(faceDetect.isInitialized());

        try {
            Thread.sleep(9000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
        assertTrue(faceDetect.isInitialized());
    }

}
