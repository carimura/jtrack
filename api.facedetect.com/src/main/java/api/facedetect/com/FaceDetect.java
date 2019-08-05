package api.facedetect.com;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import api.gif.com.GifDecoder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class FaceDetect {

    private CascadeClassifier classifier = null;
    private Fiber downloadFiber;

    public FaceDetect() {
        this.downloadFiber = FiberScope.background().schedule(() -> {
            try {
                this.setupClassifier();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    Boolean isInitialized() {
        return classifier != null;
    }

    private RectVector detectFaces(Mat frame) {
        try {
            if (!isInitialized()) {
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
        File file = Loader.cacheResource(url);
        String classifierName = file.getAbsolutePath();
        this.classifier = new CascadeClassifier(classifierName);
    }

    private BufferedImage drawFaces(BufferedImage orig, RectVector faces) {
        long nFaces = faces.size();

        if (nFaces == 0) {
            return orig;
        }

        BasicStroke basicStroke = new BasicStroke(10);
        for (int iface = 0; iface < nFaces; ++iface) {
            Rect rect = faces.get(iface);

            Graphics2D orig2D = orig.createGraphics();
            orig2D.drawRect(
                    rect.x(), rect.y(),
                    rect.width(), rect.height()
            );
            orig2D.setStroke(basicStroke);
            orig2D.dispose();
        }
        return orig;
    }

    private BufferedImage doDetection(GifDecoder gifDecoder, Integer fiberIndex) {
        System.out.println(String.format("entering fiber '%d'", fiberIndex));
        var frame = gifDecoder.getFrame(fiberIndex);
        System.out.println(String.format("fiber '%d': frame obtained", fiberIndex));
        var frameMat = Java2DFrameUtils.toMat(frame);
        System.out.println(String.format("fiber '%d': frame converted to Mat", fiberIndex));
        var faces = detectFaces(frameMat);
        System.out.println(String.format("fiber '%d': end of detection", fiberIndex));
        var finalFrame = drawFaces(frame, faces);
        System.out.println(String.format("fiber '%d': building final frame", fiberIndex));

        return finalFrame;
    }

    public void processFrameWithDetections(
            FiberWaitGroup wg, GifDecoder gifDecoder) {

        int n = gifDecoder.getFrameCount();

        for (int i = 0; i < n; i++) {
            final var newI = i;
            var f = FiberScope.background().schedule(() -> {
                var img = doDetection(gifDecoder, newI);
                System.out.println(String.format("fiber '%d' is out", newI));
                return img;
            });

            wg.add(i, f);
        }
    }
}
