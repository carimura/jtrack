package api.facedetect.com;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.opencv.opencv_core.Mat;

import static org.bytedeco.opencv.global.opencv_core.*;

public class OpenCVConverter {

    static { Loader.load(org.bytedeco.opencv.global.opencv_core.class); }

    public static int getMatDepth(int depth) {
        switch (depth) {
            case Frame.DEPTH_UBYTE:  return CV_8U;
            case Frame.DEPTH_BYTE:   return CV_8S;
            case Frame.DEPTH_USHORT: return CV_16U;
            case Frame.DEPTH_SHORT:  return CV_16S;
            case Frame.DEPTH_FLOAT:  return CV_32F;
            case Frame.DEPTH_INT:    return CV_32S;
            case Frame.DEPTH_DOUBLE: return CV_64F;
            default:  return -1;
        }
    }

    static boolean isEqual(Frame frame, Mat mat) {
        return mat != null && frame != null && frame.image != null && frame.image.length > 0
                && frame.imageWidth == mat.cols() && frame.imageHeight == mat.rows()
                && frame.imageChannels == mat.channels() && getMatDepth(frame.imageDepth) == mat.depth()
                && new Pointer(frame.image[0].position(0)).address() == mat.data().address()
                && frame.imageStride * Math.abs(frame.imageDepth) / 8 == (int)mat.step();
    }

    public Mat convertToMat(Frame frame) {
        var mat = new Mat();
        if (frame == null || frame.image == null) {
            return null;
        } else if (frame.opaque instanceof Mat) {
            return (Mat)frame.opaque;
        } else if (!isEqual(frame, mat)) {
            int depth = getMatDepth(frame.imageDepth);
            mat = depth < 0 ? null : new Mat(frame.imageHeight, frame.imageWidth, CV_MAKETYPE(depth, frame.imageChannels),
                    new Pointer(frame.image[0].position(0)), frame.imageStride * Math.abs(frame.imageDepth) / 8);
        }
        return mat;
    }
}
