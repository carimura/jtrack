package com.pinealpha.demos.jimage;

import org.junit.Test;
import static junit.framework.TestCase.*;

public class FaceDetectInitTest {


    @Test
    public void testInit() {
        var faceDetect = new FaceDetect();
        assertFalse(faceDetect.isInitialized());

        try {
            // classifier obtaining happens in a background
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
        assertTrue(faceDetect.isInitialized());
    }

}
