package api.facedetect.com;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;


public class Frame {

    /** Constants to be used for {@link #imageDepth}. */
    public static final int
            DEPTH_BYTE   =  -8,
            DEPTH_UBYTE  =   8,
            DEPTH_SHORT  = -16,
            DEPTH_USHORT =  16,
            DEPTH_INT    = -32,
            DEPTH_LONG   = -64,
            DEPTH_FLOAT  =  32,
            DEPTH_DOUBLE =  64;

    /** Information associated with the {@link #image} field. */
    public int imageWidth, imageHeight, imageDepth, imageChannels, imageStride;

    /**
     * Buffers to hold image pixels from multiple channels for a video frame.
     * Most of the software supports packed data only, but an array is provided
     * to allow users to store images in a planar format as well.
     */
    public Buffer[] image;

    /** The underlying data object, for example, Pointer, AVFrame, IplImage, or Mat. */
    public Object opaque;

    /** Returns {@code Math.abs(depth) / 8}. */
    public static int pixelSize(int depth) {
        return Math.abs(depth) / 8;
    }

    /** Empty constructor. */
    public Frame() { }

    /** Allocates a new packed image frame in native memory where rows are 8-byte aligned. */
    public Frame(int width, int height, int depth, int channels) {
        this(width, height, depth, channels, ((width * channels * pixelSize(depth) + 7) & ~7) / pixelSize(depth));
    }
    public Frame(int width, int height, int depth, int channels, int imageStride) {
        this.imageWidth = width;
        this.imageHeight = height;
        this.imageDepth = depth;
        this.imageChannels = channels;
        this.imageStride = imageStride;
        this.image = new Buffer[1];

        Pointer pointer = new BytePointer(imageHeight * imageStride * pixelSize(depth));
        ByteBuffer buffer = pointer.asByteBuffer();
        switch (imageDepth) {
            case DEPTH_BYTE:
            case DEPTH_UBYTE:  image[0] = buffer;                  break;
            case DEPTH_SHORT:
            case DEPTH_USHORT: image[0] = buffer.asShortBuffer();  break;
            case DEPTH_INT:    image[0] = buffer.asIntBuffer();    break;
            case DEPTH_LONG:   image[0] = buffer.asLongBuffer();   break;
            case DEPTH_FLOAT:  image[0] = buffer.asFloatBuffer();  break;
            case DEPTH_DOUBLE: image[0] = buffer.asDoubleBuffer(); break;
            default: throw new UnsupportedOperationException("Unsupported depth value: " + imageDepth);
        }
        opaque = pointer;
    }
}
