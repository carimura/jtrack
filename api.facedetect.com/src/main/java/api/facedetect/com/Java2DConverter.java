package api.facedetect.com;

import java.awt.*;
import java.awt.image.*;
import java.nio.*;

public class Java2DConverter {

    static final byte[]
            gamma22    = new byte[256],
            gamma22inv = new byte[256];

    Frame convert(BufferedImage image) {
        if (image == null) {
            return null;
        }
        SampleModel sm = image.getSampleModel();
        int depth = 0, numChannels = sm.getNumBands();
        switch (image.getType()) {
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
            case BufferedImage.TYPE_INT_BGR:
                depth = Frame.DEPTH_UBYTE;
                numChannels = 4;
                break;
        }
        if (depth == 0 || numChannels == 0) {
            switch (sm.getDataType()) {
                case DataBuffer.TYPE_BYTE:   depth = Frame.DEPTH_UBYTE;  break;
                case DataBuffer.TYPE_USHORT: depth = Frame.DEPTH_USHORT; break;
                case DataBuffer.TYPE_SHORT:  depth = Frame.DEPTH_SHORT;  break;
                case DataBuffer.TYPE_INT:    depth = Frame.DEPTH_INT;    break;
                case DataBuffer.TYPE_FLOAT:  depth = Frame.DEPTH_FLOAT;  break;
                case DataBuffer.TYPE_DOUBLE: depth = Frame.DEPTH_DOUBLE; break;
                default: assert false;
            }
        }
        var frame = new Frame(image.getWidth(), image.getHeight(), depth, numChannels);
        copy(image, frame, 1.0, false, null);
        return frame;
    }

    static void flipCopyWithGamma(ByteBuffer srcBuf, int srcBufferIndex, int srcStep,
                                         ByteBuffer dstBuf, int dstBufferIndex, int dstStep,
                                         boolean signed, double gamma, boolean flip, int channels) {
        assert srcBuf != dstBuf;
        int w = Math.min(srcStep, dstStep);
        int srcLine = srcBufferIndex, dstLine = dstBufferIndex;
        byte[] buffer = new byte[channels];
        while (srcLine < srcBuf.capacity() && dstLine < dstBuf.capacity()) {
            if (flip) {
                srcBufferIndex = srcBuf.capacity() - srcLine - srcStep;
            } else {
                srcBufferIndex = srcLine;
            }
            dstBufferIndex = dstLine;
            w = Math.min(Math.min(w, srcBuf.capacity() - srcBufferIndex), dstBuf.capacity() - dstBufferIndex);
            if (signed) {
                if (channels > 1) {
                    for (int x = 0; x < w; x+=channels) {
                        for (int z = 0; z < channels; z++) {
                            int in = srcBuf.get(srcBufferIndex++);
                            byte out;
                            if (gamma == 1.0) {
                                out = (byte)in;
                            } else {
                                out = (byte)Math.round(Math.pow((double)in/Byte.MAX_VALUE, gamma)*Byte.MAX_VALUE);
                            }
                            buffer[z] = out;
                        }
                        for (int z = channels-1; z >= 0; z--) {
                            dstBuf.put(dstBufferIndex++, buffer[z]);
                        }
                    }
                } else {
                    for (int x = 0; x < w; x++) {
                        int in = srcBuf.get(srcBufferIndex++);
                        byte out;
                        if (gamma == 1.0) {
                            out = (byte)in;
                        } else {
                            out = (byte)Math.round(Math.pow((double)in/Byte.MAX_VALUE, gamma)*Byte.MAX_VALUE);
                        }
                        dstBuf.put(dstBufferIndex++, out);
                    }
                }
            } else {
                if (channels > 1) {
                    for (int x = 0; x < w; x+=channels) {
                        for (int z = 0; z < channels; z++) {
                            byte out;
                            int in = srcBuf.get(srcBufferIndex++) & 0xFF;
                            if (gamma == 1.0) {
                                out = (byte)in;
                            } else if (gamma == 2.2) {
                                out = gamma22[in];
                            } else if (gamma == 1/2.2) {
                                out = gamma22inv[in];
                            } else {
                                out = (byte)Math.round(Math.pow((double)in/0xFF, gamma)*0xFF);
                            }
                            buffer[z] = out;
                        }
                        for (int z = channels-1; z >= 0; z--) {
                            dstBuf.put(dstBufferIndex++, buffer[z]);
                        }
                    }
                } else {
                    for (int x = 0; x < w; x++) {
                        byte out;
                        int in = srcBuf.get(srcBufferIndex++) & 0xFF;
                        if (gamma == 1.0) {
                            out = (byte)in;
                        } else if (gamma == 2.2) {
                            out = gamma22[in];
                        } else if (gamma == 1/2.2) {
                            out = gamma22inv[in];
                        } else {
                            out = (byte)Math.round(Math.pow((double)in/0xFF, gamma)*0xFF);
                        }
                        dstBuf.put(dstBufferIndex++, out);
                    }
                }
            }
            srcLine += srcStep;
            dstLine += dstStep;
        }
    }
    static void flipCopyWithGamma(ShortBuffer srcBuf, int srcBufferIndex, int srcStep,
                                         ShortBuffer dstBuf, int dstBufferIndex, int dstStep,
                                         boolean signed, double gamma, boolean flip, int channels) {
        assert srcBuf != dstBuf;
        int w = Math.min(srcStep, dstStep);
        int srcLine = srcBufferIndex, dstLine = dstBufferIndex;
        short[] buffer = new short[channels];
        while (srcLine < srcBuf.capacity() && dstLine < dstBuf.capacity()) {
            if (flip) {
                srcBufferIndex = srcBuf.capacity() - srcLine - srcStep;
            } else {
                srcBufferIndex = srcLine;
            }
            dstBufferIndex = dstLine;
            w = Math.min(Math.min(w, srcBuf.capacity() - srcBufferIndex), dstBuf.capacity() - dstBufferIndex);
            if (signed) {
                if (channels > 1) {
                    for (int x = 0; x < w; x+=channels) {
                        for (int z = 0; z < channels; z++) {
                            int in = srcBuf.get(srcBufferIndex++);
                            short out;
                            if (gamma == 1.0) {
                                out = (short)in;
                            } else {
                                out = (short)Math.round(Math.pow((double)in/Short.MAX_VALUE, gamma)*Short.MAX_VALUE);
                            }
                            buffer[z] = out;
                        }
                        for (int z = channels-1; z >= 0; z--) {
                            dstBuf.put(dstBufferIndex++, buffer[z]);
                        }
                    }
                } else {
                    for (int x = 0; x < w; x++) {
                        int in = srcBuf.get(srcBufferIndex++);
                        short out;
                        if (gamma == 1.0) {
                            out = (short)in;
                        } else {
                            out = (short)Math.round(Math.pow((double)in/Short.MAX_VALUE, gamma)*Short.MAX_VALUE);
                        }
                        dstBuf.put(dstBufferIndex++, out);
                    }
                }
            } else {
                if (channels > 1) {
                    for (int x = 0; x < w; x+=channels) {
                        for (int z = 0; z < channels; z++) {
                            int in = srcBuf.get(srcBufferIndex++);
                            short out;
                            if (gamma == 1.0) {
                                out = (short)in;
                            } else {
                                out = (short)Math.round(Math.pow((double)in/0xFFFF, gamma)*0xFFFF);
                            }
                            buffer[z] = out;
                        }
                        for (int z = channels-1; z >= 0; z--) {
                            dstBuf.put(dstBufferIndex++, buffer[z]);
                        }
                    }
                } else {
                    for (int x = 0; x < w; x++) {
                        int in = srcBuf.get(srcBufferIndex++) & 0xFFFF;
                        short out;
                        if (gamma == 1.0) {
                            out = (short)in;
                        } else {
                            out = (short)Math.round(Math.pow((double)in/0xFFFF, gamma)*0xFFFF);
                        }
                        dstBuf.put(dstBufferIndex++, out);
                    }
                }
            }
            srcLine += srcStep;
            dstLine += dstStep;
        }
    }
    static void flipCopyWithGamma(IntBuffer srcBuf, int srcBufferIndex, int srcStep,
                                         IntBuffer dstBuf, int dstBufferIndex, int dstStep,
                                         double gamma, boolean flip, int channels) {
        assert srcBuf != dstBuf;
        int w = Math.min(srcStep, dstStep);
        int srcLine = srcBufferIndex, dstLine = dstBufferIndex;
        int[] buffer = new int[channels];
        while (srcLine < srcBuf.capacity() && dstLine < dstBuf.capacity()) {
            if (flip) {
                srcBufferIndex = srcBuf.capacity() - srcLine - srcStep;
            } else {
                srcBufferIndex = srcLine;
            }
            dstBufferIndex = dstLine;
            w = Math.min(Math.min(w, srcBuf.capacity() - srcBufferIndex), dstBuf.capacity() - dstBufferIndex);
            if (channels > 1) {
                for (int x = 0; x < w; x+=channels) {
                    for (int z = 0; z < channels; z++) {
                        int in = srcBuf.get(srcBufferIndex++);
                        int out;
                        if (gamma == 1.0) {
                            out = (int)in;
                        } else {
                            out = (int)Math.round(Math.pow((double)in/Integer.MAX_VALUE, gamma)*Integer.MAX_VALUE);
                        }
                        buffer[z] = out;
                    }
                    for (int z = channels-1; z >= 0; z--) {
                        dstBuf.put(dstBufferIndex++, buffer[z]);
                    }
                }
            } else {
                for (int x = 0; x < w; x++) {
                    int in = srcBuf.get(srcBufferIndex++);
                    int out;
                    if (gamma == 1.0) {
                        out = in;
                    } else {
                        out = (int)Math.round(Math.pow((double)in/Integer.MAX_VALUE, gamma)*Integer.MAX_VALUE);
                    }
                    dstBuf.put(dstBufferIndex++, out);
                }
            }
            srcLine += srcStep;
            dstLine += dstStep;
        }
    }
    static void flipCopyWithGamma(FloatBuffer srcBuf, int srcBufferIndex, int srcStep,
                                         FloatBuffer dstBuf, int dstBufferIndex, int dstStep,
                                         double gamma, boolean flip, int channels) {
        assert srcBuf != dstBuf;
        int w = Math.min(srcStep, dstStep);
        int srcLine = srcBufferIndex, dstLine = dstBufferIndex;
        float[] buffer = new float[channels];
        while (srcLine < srcBuf.capacity() && dstLine < dstBuf.capacity()) {
            if (flip) {
                srcBufferIndex = srcBuf.capacity() - srcLine - srcStep;
            } else {
                srcBufferIndex = srcLine;
            }
            dstBufferIndex = dstLine;
            w = Math.min(Math.min(w, srcBuf.capacity() - srcBufferIndex), dstBuf.capacity() - dstBufferIndex);
            if (channels > 1) {
                for (int x = 0; x < w; x+=channels) {
                    for (int z = 0; z < channels; z++) {
                        float in = srcBuf.get(srcBufferIndex++);
                        float out;
                        if (gamma == 1.0) {
                            out = in;
                        } else {
                            out = (float)Math.pow(in, gamma);
                        }
                        buffer[z] = out;
                    }
                    for (int z = channels-1; z >= 0; z--) {
                        dstBuf.put(dstBufferIndex++, buffer[z]);
                    }
                }
            } else {
                for (int x = 0; x < w; x++) {
                    float in = srcBuf.get(srcBufferIndex++);
                    float out;
                    if (gamma == 1.0) {
                        out = in;
                    } else {
                        out = (float)Math.pow(in, gamma);
                    }
                    dstBuf.put(dstBufferIndex++, out);
                }
            }
            srcLine += srcStep;
            dstLine += dstStep;
        }
    }
    static void flipCopyWithGamma(DoubleBuffer srcBuf, int srcBufferIndex, int srcStep,
                                         DoubleBuffer dstBuf, int dstBufferIndex, int dstStep,
                                         double gamma, boolean flip, int channels) {
        assert srcBuf != dstBuf;
        int w = Math.min(srcStep, dstStep);
        int srcLine = srcBufferIndex, dstLine = dstBufferIndex;
        double[] buffer = new double[channels];
        while (srcLine < srcBuf.capacity() && dstLine < dstBuf.capacity()) {
            if (flip) {
                srcBufferIndex = srcBuf.capacity() - srcLine - srcStep;
            } else {
                srcBufferIndex = srcLine;
            }
            dstBufferIndex = dstLine;
            w = Math.min(Math.min(w, srcBuf.capacity() - srcBufferIndex), dstBuf.capacity() - dstBufferIndex);
            if (channels > 1) {
                for (int x = 0; x < w; x+=channels) {
                    for (int z = 0; z < channels; z++) {
                        double in = srcBuf.get(srcBufferIndex++);
                        double out;
                        if (gamma == 1.0) {
                            out = in;
                        } else {
                            out = Math.pow(in, gamma);
                        }
                        buffer[z] = out;
                    }
                    for (int z = channels-1; z >= 0; z--) {
                        dstBuf.put(dstBufferIndex++, buffer[z]);
                    }
                }
            } else {
                for (int x = 0; x < w; x++) {
                    double in = srcBuf.get(srcBufferIndex++);
                    double out;
                    if (gamma == 1.0) {
                        out = in;
                    } else {
                        out = Math.pow(in, gamma);
                    }
                    dstBuf.put(dstBufferIndex++, out);
                }
            }
            srcLine += srcStep;
            dstLine += dstStep;
        }
    }

    public static void copy(BufferedImage image, Frame frame, double gamma, boolean flipChannels, Rectangle roi) {
        Buffer out = frame.image[0];
        int bufferIndex = roi == null ? 0 : roi.y*frame.imageStride + roi.x*frame.imageChannels;
        SampleModel sm = image.getSampleModel();
        Raster r       = image.getRaster();
        DataBuffer in  = r.getDataBuffer();
        int x = -r.getSampleModelTranslateX();
        int y = -r.getSampleModelTranslateY();
        int step = sm.getWidth()*sm.getNumBands();
        int channels = sm.getNumBands();
        if (sm instanceof ComponentSampleModel) {
            step = ((ComponentSampleModel)sm).getScanlineStride();
            channels = ((ComponentSampleModel)sm).getPixelStride();
        } else if (sm instanceof SinglePixelPackedSampleModel) {
            step = ((SinglePixelPackedSampleModel)sm).getScanlineStride();
            channels = 1;
        } else if (sm instanceof MultiPixelPackedSampleModel) {
            step = ((MultiPixelPackedSampleModel)sm).getScanlineStride();
            channels = ((MultiPixelPackedSampleModel)sm).getPixelBitStride()/8; // ??
        }
        int start = y*step + x*channels;

        if (in instanceof DataBufferByte) {
            byte[] a = ((DataBufferByte)in).getData();
            flipCopyWithGamma(ByteBuffer.wrap(a), start, step, (ByteBuffer)out, bufferIndex, frame.imageStride, false, gamma, false, flipChannels ? channels : 0);
        } else if (in instanceof DataBufferDouble) {
            double[] a = ((DataBufferDouble)in).getData();
            flipCopyWithGamma(DoubleBuffer.wrap(a), start, step, (DoubleBuffer)out, bufferIndex, frame.imageStride, gamma, false, flipChannels ? channels : 0);
        } else if (in instanceof DataBufferFloat) {
            float[] a = ((DataBufferFloat)in).getData();
            flipCopyWithGamma(FloatBuffer.wrap(a), start, step, (FloatBuffer)out, bufferIndex, frame.imageStride, gamma, false, flipChannels ? channels : 0);
        } else if (in instanceof DataBufferInt) {
            int[] a = ((DataBufferInt)in).getData();
            int stride = frame.imageStride;
            if (out instanceof ByteBuffer) {
                out = ((ByteBuffer)out).order(flipChannels ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN).asIntBuffer();
                stride /= 4;
            }
            flipCopyWithGamma(IntBuffer.wrap(a), start, step, (IntBuffer)out, bufferIndex, stride, gamma, false, flipChannels ? channels : 0);
        } else if (in instanceof DataBufferShort) {
            short[] a = ((DataBufferShort)in).getData();
            flipCopyWithGamma(ShortBuffer.wrap(a), start, step, (ShortBuffer)out, bufferIndex, frame.imageStride, true, gamma, false, flipChannels ? channels : 0);
        } else if (in instanceof DataBufferUShort) {
            short[] a = ((DataBufferUShort)in).getData();
            flipCopyWithGamma(ShortBuffer.wrap(a), start, step, (ShortBuffer)out, bufferIndex, frame.imageStride, false, gamma, false, flipChannels ? channels : 0);
        } else {
            assert false;
        }
    }

}
