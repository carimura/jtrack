package com.pinealpha.demos.jimage;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.bytedeco.opencv.global.opencv_imgproc.*;


public class FaceDetect {

    private CascadeClassifier classifier = null;

    public FaceDetect() {
        try {
            this.classifier = this.setupClasifier();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.toString());
            System.exit(1);
        }
    }

    public RectVector detectFaces(Mat frame) {
        Mat gray = new Mat ();
        cvtColor(frame, gray, COLOR_BGR2GRAY);
        equalizeHist(gray, gray);

        RectVector faces = new RectVector();
        this.classifier.detectMultiScale(gray, faces);
        return faces;
    }

    private CascadeClassifier setupClasifier() throws IOException {
        URL url = new URL("https://raw.github.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_alt.xml");
        File file = Loader.cacheResource(url);
        String classifierName = file.getAbsolutePath();
        return new CascadeClassifier(classifierName);
        //return new CascadeClassifier("/usr/share/jimage/haarcascade_frontalface_alt.xml");
    }

    public BufferedImage drawFaces(BufferedImage orig, RectVector faces) {
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

    public BufferedImage processImageFromMat(Pair matFrame) {

        return drawFaces(matFrame.getLeft(), detectFaces(matFrame.getRight()));
    }
}
