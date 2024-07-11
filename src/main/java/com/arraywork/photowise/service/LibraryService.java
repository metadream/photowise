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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.arraywork.photowise.entity.CameraInfo;
import com.arraywork.photowise.entity.GeoLocation;
import com.arraywork.photowise.entity.MediaInfo;
import com.arraywork.photowise.entity.Photo;
import com.arraywork.photowise.entity.ScanningLog;
import com.arraywork.photowise.entity.ScanningOption;
import com.arraywork.photowise.enums.LogLevel;
import com.arraywork.springforce.channel.ChannelService;
import com.arraywork.springforce.util.Assert;
import com.arraywork.springforce.util.Files;
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

import jakarta.annotation.Resource;

/**
 * Library Service
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Service
public class LibraryService {

    private static final String SUPPORTED_MEDIA = "JPEG|PNG|HEIF|WebP|MP4|MOV";
    public static int scanningProgress = -1;
    public static boolean scanningAborted = false;
    public static List<ScanningLog> scanningLogs = new ArrayList<>();

    @Resource
    private ChannelService channelService;
    @Resource
    private PhotoService photoService;

    @Value("${photowise.library}")
    private String library;

    @Value("${photowise.storage}")
    private String storage;

    // Scan the photo library
    @Async
    public void startScan(ScanningOption option) {
        File lib = new File(library);
        Assert.isTrue(lib.exists() && lib.isDirectory(), "Library does not exist or is not a directory");

        if (scanningProgress > -1) return;
        scanningProgress = 0;
        long startTime = System.currentTimeMillis();

        // Clean invalid indexes
        if (option.isCleanIndexes()) {
            cleanIndexes();
        }

        List<File> files = new ArrayList<>();
        Files.walk(lib, files);
        int total = files.size();
        int count = 0;
        int success = 0;

        for (File file : files) {
            if (scanningProgress == -1) return;

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String filePath = file.getPath().substring(library.length());
            ScanningLog log = new ScanningLog(LogLevel.INFO, total, ++count);
            log.setPath(filePath);
            scanningProgress = log.getProgress();

            // Find photo data based on path relative to photo library
            Photo _photo = photoService.getPhoto(filePath);
            // Compare the file size and time
            if (!option.isFullScan() && _photo != null && _photo.getLength() == file.length()
                && _photo.getModifiedTime() == file.lastModified()) {
                log.setLevel(LogLevel.SKIPPED);
                log.setMessage("The same modified time and size.");
                channelService.broadcast("library", log);
                scanningLogs.add(0, log);
                continue;
            }

            // Extract and save metadata
            try {
                Photo photo = extractMetadata(file);
                if (_photo != null) {
                    photo.setId(_photo.getId());
                }
                log.setSuccess(++success);
                photoService.save(photo);
            } catch (Exception e) {
                log.setLevel(LogLevel.ERROR);
                log.setMessage(e.getMessage());
            } finally {
                channelService.broadcast("library", log);
                scanningLogs.add(0, log);
            }
        }

        ScanningLog log = new ScanningLog(LogLevel.FINISHED, total, count);
        log.setMessage(total + " files were found and " + success + " indexes were created."
            + " Elapsed " + (System.currentTimeMillis() - startTime) / 1000 + " s");
        channelService.broadcast("library", log);
        scanningLogs.add(0, log);
        scanningProgress = -1;
    }

    // Abort scan async thread
    public void abortScan() {
        scanningProgress = -1;
    }

    // Clear scanning logs
    public void clearLogs() {
        scanningLogs.clear();
    }

    // Clean indexes with invalid file path
    private void cleanIndexes() {
        List<Photo> photos = photoService.getAllPhotos();
        int total = photos.size();
        int count = 0;

        for (Photo photo : photos) {
            if (scanningProgress == -1) return;

            Path path = Path.of(library, photo.getPath());
            if (!path.toFile().exists()) {
                photoService.delete(photo);

                ScanningLog log = new ScanningLog(LogLevel.CLEAN, total, ++count);
                log.setPath(photo.getPath());
                channelService.broadcast("library", log);
            }
        }
    }

    // Extract metadata
    private Photo extractMetadata(File file) throws ImageProcessingException, IOException, MetadataException {
        InputStream inputStream = null;
        BufferedInputStream bufferedStream = null;

        try {
            inputStream = new FileInputStream(file);
            bufferedStream = new BufferedInputStream(inputStream);

            // Detect file type and support "JPEG|PNG|HEIF|WebP|MP4|MOV" only
            FileType fileType = FileTypeDetector.detectFileType(bufferedStream);
            String typeName = fileType.getName();
            Assert.isTrue(typeName.matches("(" + SUPPORTED_MEDIA + ")"), "Unsupported media format.");

            MediaInfo mediaInfo = new MediaInfo();
            mediaInfo.setMimeType(fileType.getMimeType());

            Photo photo = new Photo();
            photo.setMediaInfo(mediaInfo);
            photo.setPath(file.getPath().substring(library.length()));
            photo.setLength(file.length());
            photo.setCreationTime(Files.getCreationTime(file));
            photo.setModifiedTime(file.lastModified());

            // Parse metadata to photo
            Metadata metadata = ImageMetadataReader.readMetadata(bufferedStream, photo.getLength(), fileType);
            for (Directory dir : metadata.getDirectories()) {
                // Generic exif
                if (dir instanceof ExifIFD0Directory) {
                    // Shooting time
                    Date date = dir.getDate(ExifDirectoryBase.TAG_DATETIME);
                    if (date != null) {
                        photo.setShootingTime(date.getTime());
                    }

                    // Camera brand and model
                    String make = dir.getString(ExifDirectoryBase.TAG_MAKE);
                    if (make != null) {
                        CameraInfo cameraInfo = photo.getCameraInfo();
                        String model = dir.getString(ExifDirectoryBase.TAG_MODEL);
                        if (model != null) make += " " + model;
                        cameraInfo.setMakeModel(make);
                    }
                }
                else if (dir instanceof ExifSubIFDDirectory) {
                    // Shooting parameters
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

                    // Heif image dimension is here, not in HeifDirectory
                    Integer width = dir.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
                    Integer height = dir.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
                    if (width != null && height != null) {
                        mediaInfo.setWidth(width);
                        mediaInfo.setHeight(height);
                    }
                }
                else if (dir instanceof GpsDirectory) {
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
                else if (dir instanceof PngDirectory) {
                    PngDirectory pngDir = (PngDirectory) dir;
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

            // TODO 生成缩略图
            return photo;
        } finally {
            bufferedStream.close();
            inputStream.close();
        }
    }

    // Extract GEO location
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