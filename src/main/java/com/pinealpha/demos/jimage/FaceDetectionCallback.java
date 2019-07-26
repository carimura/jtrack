package com.pinealpha.demos.jimage;

import java.awt.image.BufferedImage;
import java.io.IOException;

@FunctionalInterface
public interface FaceDetectionCallback {
    BufferedImage call(BufferedImage image) throws IOException;
}
