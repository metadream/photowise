package com.arraywork.photowise;

import java.io.File;
import java.net.URL;

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
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/10
 */
public class OpenCv {

    /**
     * 根据操作系统加载不同的外部库（仅在启动时调用一次）
     */
    public static void loadLibrary() {
        String osName = System.getProperty("os.name").toLowerCase();
        String libName = osName.contains("windows") ? "opencv_java4100.dll" : "libopencv_java4100.so";
        URL url = ClassLoader.getSystemResource("opencv/" + libName);
        System.load(url.getPath());
    }

    /**
     * 捕获视频并截取相同纵横比的缩略图
     * @param input
     * @param output
     * @param longSide 长边值
     * @return
     */
    public static boolean captureVideo(String input, String output, int longSide) {
        checkPath(input, output);
        VideoCapture capture = new VideoCapture(input);
        Assert.isTrue(capture.isOpened(), "Cannot open the video: " + input);

        // 为避免前几帧的空白或空黑画面、同时避免超过总帧数，此处自动截取整个视频的中间帧
        // 如果使用CAP_PROP_POS_FRAMES或CAP_PROP_POS_MSEC设置精确的中间帧，则会非常慢
        // CAP_PROP_POS_AVI_RATIO采用相对的、粗略的位置（取值0.0-1.0），速度非常快
        capture.set(Videoio.CAP_PROP_POS_AVI_RATIO, 0.5);

        Mat mat = new Mat();
        if (capture.read(mat)) {
            Size size = calcSize(mat.width(), mat.height(), longSide);
            Imgproc.resize(mat, mat, size, 0, 0, Imgproc.INTER_AREA);
            return Imgcodecs.imwrite(output, mat);
        }
        capture.release();
        return false;
    }

    /**
     * 按相同纵横比缩放图片
     * @param input
     * @param output
     * @param longSide 长边值
     * @param quality 质量（0-100）
     * @return
     */
    public static boolean resizeImage(String input, String output, int longSide, int quality) {
        checkPath(input, output);
        Mat src = Imgcodecs.imread(input);

        Mat dist = new Mat();
        Size size = calcSize(src.width(), src.height(), longSide);
        Imgproc.resize(src, dist, size, 0, 0, Imgproc.INTER_AREA);
        MatOfInt params = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, quality);
        return Imgcodecs.imwrite(output, dist, params);
    }

    /**
     * 按相同纵横比缩放图片（质量75%）
     * @param input
     * @param output
     * @param longSide 长边值
     * @return
     */
    public static boolean resizeImage(String input, String output, int longSide) {
        return resizeImage(input, output, longSide, 75);
    }

    // 校验输入输出文件是否存在
    private static void checkPath(String input, String output) {
        File file = new File(input);
        Assert.isTrue(file.exists(), "Input file not found: " + file);
        File dir = new File(output).getParentFile();
        Assert.isTrue(dir.exists(), "Output path not found: " + dir);
    }

    // 计算缩放后的尺寸
    private static Size calcSize(int width, int height, int longSide) {
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
        return new Size(width, height);
    }

}