package com.arraywork.photowise.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.imageio.ImageIO;

import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.arraywork.photowise.ImageUtil;
import com.arraywork.springforce.StaticResourceHandler;
import com.arraywork.springforce.util.OpenCv;

import jakarta.annotation.Resource;
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
    @GetMapping("/test/video/opencv/{filename}")
    public String videoopencv(@PathVariable String filename) throws UnsupportedEncodingException {
        StopWatch sw = new StopWatch();
        sw.start();

        filename = URLDecoder.decode(filename, "UTF-8");
        for (int i = 0; i < count; i++) {
            OpenCv.captureVideo(filename, filename + "_中文opencv.jpg", 400);
        }

        sw.stop();
        return "Opencv video processed: " + sw.getTotalTimeMillis() + "ms";
    }

    // 循环50次约12秒
    @GetMapping("/test/video/ffmpeg/{filename}")
    public String videoffmpeg(@PathVariable String filename)
        throws InputFormatException, EncoderException, UnsupportedEncodingException {
        StopWatch sw = new StopWatch();
        sw.start();

        filename = URLDecoder.decode(filename, "UTF-8");
        for (int i = 0; i < count; i++) {
            screenshot(filename, filename + "_中文ffmpeg.jpg");
        }

        sw.stop();
        return "Ffmpeg video processed: " + sw.getTotalTimeMillis() + "ms";
    }

    // 循环50次约15秒
    @GetMapping("/test/image/native/{filename}")
    public String imagenative(@PathVariable String filename) throws IOException {
        StopWatch sw = new StopWatch();
        sw.start();

        filename = URLDecoder.decode(filename, "UTF-8");
        for (int i = 0; i < count; i++) {
            BufferedImage srcImage = ImageIO.read(new File(filename));
            BufferedImage thumbImage = ImageUtil.resizeByNative(srcImage, 400);
            ImageIO.write(thumbImage, "jpg", new File(filename + "_中文native.jpg"));
        }

        sw.stop();
        return "Native image processed: " + sw.getTotalTimeMillis() + "ms";
    }

    // 循环50次约22秒
    @GetMapping("/test/image/thumb/{filename}")
    public String imagethumb(@PathVariable String filename) throws IOException {
        StopWatch sw = new StopWatch();
        sw.start();

        filename = URLDecoder.decode(filename, "UTF-8");
        for (int i = 0; i < count; i++) {
            BufferedImage srcImage = ImageIO.read(new File(filename));
            BufferedImage thumbImage = ImageUtil.resizeByThumbnailator(srcImage, 400);
            ImageIO.write(thumbImage, "jpg", new File(filename + "_中文thumb.jpg"));
        }

        sw.stop();
        return "Thumbnailator image processed: " + sw.getTotalTimeMillis() + "ms";
    }

    // 循环50次约7秒
    @GetMapping("/test/image/opencv/{filename}")
    public String imageopencv(@PathVariable String filename) throws IOException {
        StopWatch sw = new StopWatch();
        sw.start();

        filename = URLDecoder.decode(filename, "UTF-8");
        for (int i = 0; i < count; i++) {
            OpenCv.resizeImage(filename, filename + "_中文opencv.jpg", 400);
        }

        sw.stop();
        return "Opencv image processed: " + sw.getTotalTimeMillis() + "ms";
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