package com.arraywork.photowise;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.springframework.util.StopWatch;

/**
 *
 * @author AiChen
 * @created 2024/07/10
 */
public class JavaCvTest {

    public static void main(String[] args) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // OpenCv.captureVideo("C:\\Users\\Administrator\\Videos\\rc.mp4",
        // "C:\\Users\\Administrator\\Videos\\rc.jpg", 0);
        // if (1 == 1) return;

        int count = 1;
        StopWatch sw = new StopWatch();
        sw.start();

        // 约15秒
        for (int i = 0; i < count; i++) {
            BufferedImage srcImage = ImageIO.read(new File("C:\\Users\\Administrator\\Pictures\\IMG_7505.JPEG"));
            BufferedImage thumbImage = ImageUtil.resizeByNative(srcImage, 403);
            ImageIO.write(thumbImage, "jpg",
                new File("C:\\Users\\Administrator\\Pictures\\3\\IMG_7505_" + i + ".JPEG"));
        }

        sw.stop();
        System.out.println("OpenCv Processed: " + sw.getLastTaskTimeMillis() + "ms");
        sw.start();

        // 约22秒
        for (int i = 0; i < count; i++) {
            BufferedImage srcImage = ImageIO.read(new File("C:\\Users\\Administrator\\Pictures\\IMG_7505.JPEG"));
            BufferedImage thumbImage = ImageUtil.resizeByThumbnailator(srcImage, 403);
            ImageIO.write(thumbImage, "jpg",
                new File("C:\\Users\\Administrator\\Pictures\\2\\IMG_7505_" + i + ".JPEG"));
        }

        sw.stop();
        System.out.println("Thumbnailator Processed: " + sw.getLastTaskTimeMillis() + "ms");
        sw.start();

        // 约7秒
        for (int i = 0; i < count; i++) {
            OpenCv.resizeImage("C:\\Users\\Administrator\\Pictures\\IMG_7505.JPEG",
                "C:\\Users\\Administrator\\Pictures\\1\\IMG_7505_" + i + ".JPEG", 403);
        }

        sw.stop();
        System.out.println("Native Processed: " + sw.getLastTaskTimeMillis() + "ms");
    }

}