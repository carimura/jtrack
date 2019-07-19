package com.pinealpha.demos.jimage;

import org.bytedeco.opencv.opencv_core.Mat;

import java.awt.image.BufferedImage;

public class Pair {

    protected BufferedImage img;
    protected Mat ofImg;

    public Pair(BufferedImage frame, Mat ofFrame) {
        img = frame;
        ofImg = ofFrame;
    }

    public BufferedImage getLeft() {
        return img;
    }

    public Mat getRight() {
        return ofImg;
    }
}
