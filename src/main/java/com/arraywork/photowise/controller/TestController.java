package com.arraywork.photowise.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arraywork.photowise.ImageUtil;
import com.arraywork.photowise.OpenCv;
import com.arraywork.springforce.StaticResourceHandler;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import ws.schild.jave.EncoderException;
import ws.schild.jave.InputFormatException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.ScreenExtractor;
import ws.schild.jave.info.MultimediaInfo;

@RestController
public class TestController {

    private int count = 1;

    @Resource
    private StaticResourceHandler resourceHandler;

    // opencv seek keyframe
    // opencv CAP_PROP_POS_FRAMES very slow
    // CV_CAP_PROP_POS_AVI_RATIO
    // 循环50次约5秒
    @GetMapping("/test/video/opencv")
    public String videoopencv() {
        StopWatch sw = new StopWatch();
        sw.start();

        for (int i = 0; i < count; i++) {
            OpenCv.captureVideo("./video.mp4", "./video_opencv.jpg", 400);
        }

        sw.stop();
        return "Opencv video processed: " + sw.getTotalTimeMillis() + "ms";
    }

    // 循环50次约12秒
    @GetMapping("/test/video/ffmpeg")
    public String videoffmpeg() throws InputFormatException, EncoderException {
        StopWatch sw = new StopWatch();
        sw.start();

        for (int i = 0; i < count; i++) {
            screenshot("./video.mp4", "./video_ffmpeg.jpg");
        }

        sw.stop();
        return "Ffmpeg video processed: " + sw.getTotalTimeMillis() + "ms";
    }

    // 循环50次约15秒
    @GetMapping("/test/image/native")
    public String imagenative() throws IOException {
        StopWatch sw = new StopWatch();
        sw.start();

        for (int i = 0; i < count; i++) {
            BufferedImage srcImage = ImageIO.read(new File("./image.jpg"));
            BufferedImage thumbImage = ImageUtil.resizeByNative(srcImage, 400);
            ImageIO.write(thumbImage, "jpg", new File("image_native.jpg"));
        }

        sw.stop();
        return "Native image processed: " + sw.getTotalTimeMillis() + "ms";
    }

    // 循环50次约22秒
    @GetMapping("/test/image/thumb")
    public String imagethumb() throws IOException {
        StopWatch sw = new StopWatch();
        sw.start();

        for (int i = 0; i < count; i++) {
            BufferedImage srcImage = ImageIO.read(new File("./image.jpg"));
            BufferedImage thumbImage = ImageUtil.resizeByThumbnailator(srcImage, 400);
            ImageIO.write(thumbImage, "jpg", new File("image_thumb.jpg"));
        }

        sw.stop();
        return "Thumbnailator image processed: " + sw.getTotalTimeMillis() + "ms";
    }

    // 循环50次约7秒
    @GetMapping("/test/image/opencv")
    public String imageopencv() throws IOException {
        StopWatch sw = new StopWatch();
        sw.start();

        for (int i = 0; i < count; i++) {
            OpenCv.resizeImage("./image.jpg", "./image_opencv.jpg", 400);
        }

        sw.stop();
        return "Opencv image processed: " + sw.getTotalTimeMillis() + "ms";
    }

    @GetMapping("/test/video/stream")
    public void streaming(HttpServletResponse response) throws IOException {
        response.setHeader("Content-Type", "image/jpeg");
        VideoCapture capture = new VideoCapture("./video.mp4");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        Mat mat = new Mat();
        if (capture.read(mat)) {
            MatOfByte buff = new MatOfByte();
            Imgcodecs.imencode(".jpg", mat, buff);
            byte[] bytes = buff.toArray();

            resourceHandler.copy(new ByteArrayInputStream(bytes), response.getOutputStream());

        }
    }

    private void screenshot(String input, String output) throws InputFormatException, EncoderException {
        MultimediaObject mObject = new MultimediaObject(new File(input));
        MultimediaInfo mInfo = mObject.getInfo();
        long millis = mInfo.getDuration() / 2;
        ScreenExtractor screenExtractor = new ScreenExtractor();
        try {
            screenExtractor.renderOneImage(mObject, -1, -1, millis, new File(output), 1);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
    }

}