package com.arraywork.photowise.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.arraywork.photowise.entity.CameraInfo;
import com.arraywork.photowise.entity.GeoLocation;
import com.arraywork.photowise.entity.MediaInfo;
import com.arraywork.photowise.entity.PhotoIndex;
import com.arraywork.photowise.entity.ScanningLog;
import com.arraywork.photowise.entity.ScanningOption;
import com.arraywork.photowise.enums.LogLevel;
import com.arraywork.photowise.enums.MediaType;
import com.arraywork.springforce.channel.ChannelService;
import com.arraywork.springforce.util.Assert;
import com.arraywork.springforce.util.FileUtils;
import com.arraywork.springforce.util.OpenCv;
import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.png.PngChunkType;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mov.media.QuickTimeVideoDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import com.drew.metadata.mp4.media.Mp4VideoDirectory;
import com.drew.metadata.png.PngDirectory;
import com.drew.metadata.webp.WebpDirectory;

/**
 * Library Service
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Service
public class LibraryService {

    private static final String SUPPORTED_MEDIA = "JPEG|PNG|WebP|HEIC|HEIF|MP4|MOV";
    public static int scanningProgress = -1;
    public static List<ScanningLog> scanningLogs = new ArrayList<>();

    @Resource
    private ChannelService channelService;
    @Resource
    private PhotoService photoService;

    @Value("${photowise.library}")
    private String library;

    @Value("${photowise.thumbnails}")
    private String thumbnails;

    @Value("${photowise.thumb-size}")
    private int thumbSize;

    /** 异步扫描照片库 */
    @Async
    public void startScan(ScanningOption option) {
        File lib = new File(library);
        Assert.isTrue(lib.exists() && lib.isDirectory(), "照片库不存在或不是目录");

        if (scanningProgress > -1) return;
        scanningProgress = 0;
        long startTime = System.currentTimeMillis();

        // 清理无效索引
        if (option.isCleanIndexes()) {
            cleanIndexes();
        }

        // 获取照片库下所有文件
        List<File> files = new ArrayList<>();
        FileUtils.walk(lib, files);
        int total = files.size();
        int count = 0, success = 0;

        // 遍历文件
        for (File file : files) {
            if (scanningProgress == -1) return;

            String filePath = file.getPath();
            String relativePath = filePath.substring(library.length());
            ScanningLog log = new ScanningLog(LogLevel.INFO, total, ++count);
            log.setPath(relativePath);
            scanningProgress = log.getProgress();

            // 根据扫描参数、文件大小和更新时间判断是否跳过扫描
            PhotoIndex _photo = photoService.getPhotoByPath(relativePath);
            if (!option.isFullScan() && _photo != null
                && _photo.getMediaInfo().getLength() == file.length()
                && _photo.getModifiedTime() == file.lastModified()) {
                continue;
            }

            // 实质性处理
            try {
                // 1. 提取元数据
                PhotoIndex photo = extractMetadata(file);
                if (_photo != null) {
                    photo.setId(_photo.getId());
                }
                // 2. 生成缩略图
                String output = Path.of(thumbnails, photo.getPath()) + ".jpg";
                if (photo.isVideo()) OpenCv.captureVideo(filePath, output, thumbSize);
                else OpenCv.resizeImage(filePath, output, thumbSize);

                // 3. 保存索引
                photoService.save(photo);
                success++;
            } catch (Exception e) {
                log.setLevel(LogLevel.ERROR);
                log.setMessage(e.getMessage());
            } finally {
                channelService.broadcast("library", log);
                scanningLogs.add(0, log);
            }
        }

        // 完成扫描
        ScanningLog log = new ScanningLog(LogLevel.FINISHED, total, success);
        log.setMessage("发现 " + total + " 个文件，成功创建 " + success + " 个索引，"
            + "共耗时 " + (System.currentTimeMillis() - startTime) / 1000 + " 秒");
        channelService.broadcast("library", log);
        scanningLogs.add(0, log);
        scanningProgress = -1;
    }

    /** 获取扫描进度 */
    public int getProgress() {
        return scanningProgress;
    }

    /** 获取扫描日志 */
    public List<ScanningLog> getLogs() {
        return scanningLogs;
    }

    /** 中止扫描 */
    public void abortScan() {
        scanningProgress = -1;
    }

    /** 清空日志 */
    public void clearLogs() {
        scanningLogs.clear();
    }

    /** 清理无效索引 */
    private void cleanIndexes() {
        long startTime = System.currentTimeMillis();
        List<PhotoIndex> photos = photoService.getIndexes();
        int total = photos.size();
        int count = 0, success = 0;

        for (PhotoIndex photo : photos) {
            if (scanningProgress == -1) return;
            count++;

            // 删除文件路径不存在的索引和缩略图
            Path path = Path.of(library, photo.getPath());
            if (!path.toFile().exists()) {
                File thumbnail = Path.of(thumbnails, photo.getPath() + ".jpg").toFile();
                thumbnail.delete();
                photoService.delete(photo);

                ScanningLog log = new ScanningLog(LogLevel.CLEAN, total, count);
                log.setPath(photo.getPath());
                channelService.broadcast("library", log);
                scanningLogs.add(0, log);
                success++;
            }
        }

        // 完成清理
        ScanningLog log = new ScanningLog(LogLevel.FINISHED, total, success);
        log.setMessage("发现 " + total + " 个索引，清理 " + success + " 个，"
            + "共耗时 " + (System.currentTimeMillis() - startTime) / 1000 + " 秒");
        channelService.broadcast("library", log);
        scanningLogs.add(0, log);
    }

    /** 提取元数据 */
    private PhotoIndex extractMetadata(File file)
        throws IOException, ImageProcessingException, MetadataException {

        try (InputStream inputStream = new FileInputStream(file);
             BufferedInputStream bufferedStream = new BufferedInputStream(inputStream)) {

            // 检测文件类型（仅支持"JPEG|PNG|HEIF|WebP|MP4|MOV"）
            FileType fileType = FileTypeDetector.detectFileType(bufferedStream);
            String typeName = fileType.getName();
            Assert.isTrue(typeName.matches("(" + SUPPORTED_MEDIA + ")"), "不支持的媒体格式");

            MediaInfo mediaInfo = new MediaInfo();
            mediaInfo.setLength(file.length());

            PhotoIndex photo = new PhotoIndex();
            String mimeType = fileType.getMimeType();
            String type = mimeType.substring(0, mimeType.indexOf("/"));
            photo.setMediaType(MediaType.nameOf(type));
            photo.setMediaInfo(mediaInfo);
            photo.setPath(file.getPath().substring(library.length()));
            photo.setModifiedTime(file.lastModified());

            // 将元数据解析为照片索引
            Metadata metadata = ImageMetadataReader.readMetadata(bufferedStream, file.length(), fileType);
            for (Directory dir : metadata.getDirectories()) {
                // 通用EXIF
                if (dir instanceof ExifIFD0Directory) {
                    // 拍摄时间
                    Date date = dir.getDate(ExifDirectoryBase.TAG_DATETIME);
                    if (date != null) {
                        photo.setOriginalTime(date.getTime());
                    }

                    // 相机制造商和型号
                    String make = dir.getString(ExifDirectoryBase.TAG_MAKE);
                    if (make != null) {
                        CameraInfo cameraInfo = photo.getCameraInfo();
                        String model = dir.getString(ExifDirectoryBase.TAG_MODEL);
                        if (model != null) make += " " + model;
                        cameraInfo.setMakeModel(make);
                    }
                } else if (dir instanceof ExifSubIFDDirectory) {
                    // 拍摄参数
                    String apertureValue = dir.getString(ExifSubIFDDirectory.TAG_FNUMBER);
                    if (apertureValue != null) {
                        photo.getCameraInfo().setApertureValue(apertureValue);
                    }
                    String shutterSpeed = dir.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
                    if (shutterSpeed != null) {
                        photo.getCameraInfo().setShutterSpeed(shutterSpeed);
                    }
                    String isoEquivalent = dir.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
                    if (isoEquivalent != null) {
                        photo.getCameraInfo().setIsoEquivalent(isoEquivalent);
                    }
                    String exposureBias = dir.getString(ExifSubIFDDirectory.TAG_EXPOSURE_BIAS);
                    if (exposureBias != null) {
                        photo.getCameraInfo().setExposureBias(exposureBias);
                    }
                    String focalLength = dir.getString(ExifSubIFDDirectory.TAG_35MM_FILM_EQUIV_FOCAL_LENGTH);
                    if (focalLength != null) {
                        photo.getCameraInfo().setFocalLength(focalLength);
                    }

                    // HEIF图片尺寸（不在HeifDirectory里）
                    Integer width = dir.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
                    Integer height = dir.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
                    if (width != null && height != null) {
                        mediaInfo.setWidth(width);
                        mediaInfo.setHeight(height);
                    }
                } else if (dir instanceof GpsDirectory) {
                    GeoLocation geoLocation = extractGeoLocation((GpsDirectory) dir);
                    photo.setGeoLocation(geoLocation);
                }

                // image/jpeg
                else if (dir instanceof JpegDirectory) {
                    mediaInfo.setWidth(dir.getInteger(JpegDirectory.TAG_IMAGE_WIDTH));
                    mediaInfo.setHeight(dir.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT));
                }
                // image/webp
                else if (dir instanceof WebpDirectory) {
                    mediaInfo.setWidth(dir.getInteger(WebpDirectory.TAG_IMAGE_WIDTH));
                    mediaInfo.setHeight(dir.getInteger(WebpDirectory.TAG_IMAGE_HEIGHT));
                }
                // image/png
                else if (dir instanceof PngDirectory pngDir) {
                    PngChunkType pngType = pngDir.getPngChunkType();
                    if (pngType.equals(PngChunkType.IHDR)) {
                        mediaInfo.setWidth(dir.getInteger(PngDirectory.TAG_IMAGE_WIDTH));
                        mediaInfo.setHeight(dir.getInteger(PngDirectory.TAG_IMAGE_HEIGHT));
                    }
                }

                // video/mp4
                else if (dir instanceof Mp4Directory) {
                    if (dir.containsTag(Mp4Directory.TAG_DURATION)) {
                        mediaInfo.setDuration(dir.getLong(Mp4Directory.TAG_DURATION));
                    }
                    if (dir instanceof Mp4VideoDirectory) {
                        mediaInfo.setWidth(dir.getInteger(Mp4VideoDirectory.TAG_WIDTH));
                        mediaInfo.setHeight(dir.getInteger(Mp4VideoDirectory.TAG_HEIGHT));
                    }
                }
                // video/mov
                else if (dir instanceof QuickTimeDirectory) {
                    if (dir.containsTag(QuickTimeDirectory.TAG_DURATION)) {
                        mediaInfo.setDuration(dir.getLong(QuickTimeDirectory.TAG_DURATION));
                    }
                    if (dir instanceof QuickTimeVideoDirectory) {
                        mediaInfo.setWidth(dir.getInteger(QuickTimeVideoDirectory.TAG_WIDTH));
                        mediaInfo.setHeight(dir.getInteger(QuickTimeVideoDirectory.TAG_HEIGHT));
                    }
                }
            }

            long photoTime = photo.getOriginalTime() > 0
                ? photo.getOriginalTime() : FileUtils.getCreationTime(file);
            photo.setPhotoTime(photoTime);
            return photo;
        }
    }

    /** 提取GPS元数据 */
    private GeoLocation extractGeoLocation(GpsDirectory gps) {
        GeoLocation geoLocation = null;
        com.drew.lang.GeoLocation geo = gps.getGeoLocation();
        if (geo != null) {
            geoLocation = new GeoLocation();
            geoLocation.setLatitude(geo.getLatitude());
            geoLocation.setLongitude(geo.getLongitude());
            geoLocation.setAltitude(gps.getDoubleObject(GpsDirectory.TAG_ALTITUDE));
        }
        return geoLocation;
    }

}