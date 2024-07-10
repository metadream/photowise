package com.arraywork.photowise;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

/**
 * Open Computer Vision Utils
 * System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/10
 */
public class OpenCv {

    // Capture video
    public static boolean capture(String input, String output, int size) {
        VideoCapture capture = new VideoCapture(input);
        // capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 320);
        // capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 240);

        Mat mat = new Mat();
        if (capture.read(mat)) {
            return Imgcodecs.imwrite(output, mat);
        }
        return false;
    }

    // Resize image (default quality 75%)
    public static boolean resize(String input, String output, int size) {
        return resize(input, output, size, 75);
    }

    // Resize image
    public static boolean resize(String input, String output, int size, int quality) {
        Mat src = Imgcodecs.imread(input);

        int width = src.width();
        int height = src.height();
        double ratio = (double) width / height;

        if (width > height) {
            width = size;
            height = (int) (size / ratio);
        }
        else {
            height = size;
            width = (int) (size * ratio);
        }

        Mat dist = new Mat();
        Imgproc.resize(src, dist, new Size(width, height), 0, 0, Imgproc.INTER_AREA);
        MatOfInt params = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality);
        return Imgcodecs.imwrite(output, dist, params);
    }

}