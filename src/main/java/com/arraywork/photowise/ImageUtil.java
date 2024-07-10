package com.arraywork.photowise;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;

import net.coobird.thumbnailator.Thumbnails;

/**
 * Image Utility
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/06/06
 */
public class ImageUtil {

    // Resize image by native (Fast but low quality)
    public static BufferedImage resizeByNative(BufferedImage srcImage, int size) {
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        double scale = (double) width / height;

        if (width > height) {
            width = size;
            height = (int) (size / scale);
        }
        else {
            height = size;
            width = (int) (size * scale);
        }
        return resizeByNative(srcImage, width, height);
    }

    // Resize image by native (Fast but low quality)
    public static BufferedImage resizeByNative(BufferedImage srcImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height,
            (srcImage.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_INT_ARGB));
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(srcImage, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    // Resize image by thumbnailator (Slower speed but higher quality)
    public static BufferedImage resizeByThumbnailator(BufferedImage srcImage, int size) throws IOException {
        return Thumbnails.of(srcImage).size(size, size).outputQuality(0.9).asBufferedImage();
    }

}