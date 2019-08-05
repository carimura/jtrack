package api.facedetect.com;

import java.awt.image.BufferedImage;

import org.bytedeco.opencv.opencv_core.Mat;


public class Java2DFrameUtils {

    private static OpenCVConverter matConv = new OpenCVConverter();
    private static Java2DConverter java2DConv  = new Java2DConverter();

    public static Mat toMat(BufferedImage src){
        return matConv.convertToMat(java2DConv.convert(src)).clone();
    }
}
