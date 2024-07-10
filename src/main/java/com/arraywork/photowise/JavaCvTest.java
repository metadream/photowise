package com.arraywork.photowise;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.util.StopWatch;

/**
 *
 * @author AiChen
 * @created 2024/07/10
 */
public class JavaCvTest {

    public static void main(String[] args) throws IOException {
        boolean res = OpenCv.captureVideo("/home/xehu/Documents/test2/video.mp4",
            "/home/xehu/Documents/test21/video.jpg", 480);
        System.out.println("----------" + res);
        if (1 == 1) return;

        int count = 50;
        StopWatch sw = new StopWatch();
        sw.start();

        // 约15秒
        for (int i = 0; i < count; i++) {
            BufferedImage srcImage = ImageIO.read(new File("/home/xehu/Documents/test2/IMG_5986.JPG"));
            BufferedImage thumbImage = ImageUtil.resizeByNative(srcImage, 403);
            ImageIO.write(thumbImage, "jpg",
                new File("/home/xehu/Documents/test2/1/IMG_5986_" + i + ".JPEG"));
        }

        sw.stop();
        System.out.println("Native Processed: " + sw.getLastTaskTimeMillis() + "ms");
        sw.start();

        // 约22秒
        for (int i = 0; i < count; i++) {
            BufferedImage srcImage = ImageIO.read(new File("/home/xehu/Documents/test2/IMG_5986.JPG"));
            BufferedImage thumbImage = ImageUtil.resizeByThumbnailator(srcImage, 403);
            ImageIO.write(thumbImage, "jpg",
                new File("/home/xehu/Documents/test2/2/IMG_5986_" + i + ".JPEG"));
        }

        sw.stop();
        System.out.println("Thumbnailator Processed: " + sw.getLastTaskTimeMillis() + "ms");
        sw.start();

        // 约7秒
        for (int i = 0; i < count; i++) {
            OpenCv.resizeImage("/home/xehu/Documents/test2/IMG_5986.JPG",
                "/home/xehu/Documents/test2/3/IMG_5986_" + i + ".JPEG", 403);
        }

        sw.stop();
        System.out.println("Opencv Processed: " + sw.getLastTaskTimeMillis() + "ms");
    }

}