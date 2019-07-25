package com.pinealpha.demos.jimage;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static org.bytedeco.opencv.global.opencv_imgproc.*;


public class FaceDetect {

    private CascadeClassifier classifier = null;
    private Fiber downloadFiber;

    FaceDetect() {
        this.downloadFiber = FiberScope.background().schedule(() -> {
            try {
                this.setupClassifier();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    public Boolean isInitialized() {
        return this.classifier != null;
    }

    private RectVector detectFaces(Mat frame) {
        try {
            if (this.classifier == null) {
                this.downloadFiber.toFuture().get();
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        var gray = new Mat();
        cvtColor(frame, gray, COLOR_BGR2GRAY);
        equalizeHist(gray, gray);

        RectVector faces = new RectVector();
        this.classifier.detectMultiScale(gray, faces);
        return faces;
    }

    private void setupClassifier() throws IOException {
        URL url = new URL(
                "https://raw.github.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_alt.xml"
        );
        this.classifier = new CascadeClassifier(
                Loader.cacheResource(url)
                        .getAbsolutePath()
        );
    }

    private BufferedImage drawFaces(BufferedImage orig, RectVector faces) {
        long nFaces = faces.size();

        if (nFaces == 0) {
            return orig;
        }

        for (int iface = 0; iface < nFaces; ++iface) {
            Rect rect = faces.get(iface);
            Graphics2D orig2D = orig.createGraphics();
            orig2D.setStroke(new BasicStroke(4.0f));
            orig2D.setColor(new Color(255,255, 0));
            orig2D.drawRect(rect.x(), rect.y(), rect.width(), rect.height());
            orig2D.dispose();
        }
        return orig;
    }

    private BufferedImage doDetection(GifDecoder gifDecoder, Integer frameIndex) {
        var frame = gifDecoder.getFrame(frameIndex);
        var frameMat = Java2DFrameUtils.toMat(frame);
        var faces = detectFaces(frameMat);

        return drawFaces(frame, faces);
    }

    public ArrayList<BufferedImage> processFrameWithDetections(GifDecoder gifDecoder) {

        var finalFrames = new ArrayList<BufferedImage>();

        for (int i = 0; i < gifDecoder.getFrameCount(); ++i) {
            finalFrames.add(doDetection(gifDecoder, i));
        }

        return finalFrames;
    }
}
