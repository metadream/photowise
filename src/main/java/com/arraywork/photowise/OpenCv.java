package com.arraywork.photowise;

import java.io.File;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import com.arraywork.springforce.util.Assert;

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
    public static boolean captureVideo(String input, String output, int size) {
        checkPath(input, output);
        VideoCapture capture = new VideoCapture(input);
        Assert.isTrue(capture.isOpened(), "Cannot open the video: " + input);

        int frames = (int) capture.get(Videoio.CAP_PROP_FRAME_COUNT);
        int fps = (int) capture.get(Videoio.CAP_PROP_FPS);
        int pos = (int) capture.get(Videoio.CAP_PROP_POS_FRAMES);
        int duration = frames / fps;
        System.out.println("frames=" + frames);
        System.out.println("fps=" + fps);
        System.out.println("duration=" + duration);
        System.out.println("pos=" + pos);

        capture.set(Videoio.CAP_PROP_POS_FRAMES, frames / 2);

        Mat mat = new Mat();
        if (capture.read(mat)) {
            System.out.println(mat.width());
            System.out.println(mat.height());
            int[] d = getDimension(mat.width(), mat.height(), size);
            Imgproc.resize(mat, mat, new Size(d[0], d[1]), 0, 0, Imgproc.INTER_AREA);
            return Imgcodecs.imwrite(output, mat);
        }
        capture.release();
        return false;
    }

    // Resize image (default quality 75%)
    public static boolean resizeImage(String input, String output, int size) {
        return resizeImage(input, output, size, 75);
    }

    // Resize image
    public static boolean resizeImage(String input, String output, int size, int quality) {
        checkPath(input, output);
        Mat src = Imgcodecs.imread(input);
        int[] d = getDimension(src.width(), src.height(), size);

        Mat dist = new Mat();
        Imgproc.resize(src, dist, new Size(d[0], d[1]), 0, 0, Imgproc.INTER_AREA);
        MatOfInt params = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality);
        return Imgcodecs.imwrite(output, dist, params);
    }

    // Check the input and output path valid
    private static void checkPath(String input, String output) {
        File file = new File(input);
        Assert.isTrue(file.exists(), "Input file not found: " + file);
        File dir = new File(output).getParentFile();
        Assert.isTrue(dir.exists(), "Output path not found: " + dir);
    }

    // Calculate width and height based on long side and ratio
    private static int[] getDimension(int width, int height, int longSide) {
        if (longSide > 0) {
            double ratio = (double) width / height;

            if (width > height) {
                width = longSide;
                height = (int) (longSide / ratio);
            }
            else {
                height = longSide;
                width = (int) (longSide * ratio);
            }
        }
        int[] d = { width, height };
        return d;
    }

}