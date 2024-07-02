package com.arraywork.photowise.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import com.arraywork.photowise.entity.Scanning;
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

    public static Scanning scanning = Scanning.getSingleton();
    private static final String SUPPORTED_MEDIA = "JPEG|PNG|HEIF|WebP|MP4|MOV";

    @Resource
    private PhotoService photoService;

    @Value("${photowise.library}")
    private String library;

    @Value("${photowise.storage}")
    private String storage;

    @Async // Scan photo library
    // @PostConstruct
    public void scan() {
        File lib = new File(library);
        Assert.isTrue(lib.exists() && lib.isDirectory(), "Library does not exist or is not a directory");

        if (scanning.inProgess) return;
        scanning.inProgess = true;
        long stms = System.currentTimeMillis();

        List<File> files = new ArrayList<>();
        Files.walk(lib, files);
        scanning.total = files.size();
        scanning.count = 0;

        for (File file : files) {
            scanning.message = file.getPath();

            // Find photo data based on path relative to photo library
            String filePath = file.getPath().substring(library.length());
            Photo _photo = photoService.getPhoto(filePath);

            // Compare the file size and time
            if (_photo != null && _photo.getLength() == file.length()
                && _photo.getModifiedTime() == file.lastModified()) {
                continue;
            }

            // Extract and save metadata
            try {
                Photo photo = extractMetadata(file);
                if (photo != null) {
                    if (_photo != null) {
                        photo.setId(_photo.getId());
                    }

                    photoService.save(photo);
                    scanning.count++;
                }
            } catch (Exception e) {
                scanning.message = e.getMessage();
                e.printStackTrace();
            }
        }

        scanning.elapsedTime = System.currentTimeMillis() - stms;
        scanning.message = scanning.total + " files were found and " + scanning.count + " indexes were created.";
        scanning.inProgess = false;
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
            if (!typeName.matches("(" + SUPPORTED_MEDIA + ")")) {
                return null;
            }

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
                    String duration = dir.getDescription(Mp4Directory.TAG_DURATION_SECONDS);
                    if (duration != null) mediaInfo.setDuration(duration);
                    if (dir instanceof Mp4VideoDirectory) {
                        mediaInfo.setWidth(dir.getInteger(Mp4VideoDirectory.TAG_WIDTH));
                        mediaInfo.setHeight(dir.getInteger(Mp4VideoDirectory.TAG_HEIGHT));
                    }
                }
                // video/mov
                else if (dir instanceof QuickTimeDirectory) {
                    String duration = dir.getDescription(QuickTimeDirectory.TAG_DURATION_SECONDS);
                    if (duration != null) mediaInfo.setDuration(duration);
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