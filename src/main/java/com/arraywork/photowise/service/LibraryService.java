package com.arraywork.photowise.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.arraywork.photowise.entity.GeoLocation;
import com.arraywork.photowise.entity.MediaInfo;
import com.arraywork.photowise.entity.Parameter;
import com.arraywork.photowise.entity.Photo;
import com.arraywork.photowise.entity.ScanningInfo;
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
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.gif.GifHeaderDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

/**
 * Library Service
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Service
public class LibraryService {

    public static ScanningInfo scanning = ScanningInfo.getSingleton();

    @Resource
    private PhotoService photoService;

    @Value("${photowise.library}")
    private String library;

    @Value("${photowise.storage}")
    private String storage;

    @Async // Scan photo library
    @PostConstruct
    public void scan() {
        library = "C:\\Users\\Administrator\\Pictures";
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
            Photo photo = photoService.getPhoto(filePath);

            // Compare the file size and time
            if (photo != null && photo.getLength() == file.length()
                && photo.getModifiedTime() == file.lastModified()) {
                continue;
            }
            // Extract and save metadata
            try {
                photo = extractMetadata(file);
                if (photo != null) {
                    System.out.println(photo);
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
        System.out.println(scanning);
    }

    // Extract metadata
    private Photo extractMetadata(File file) throws ImageProcessingException, IOException, MetadataException {
        InputStream inputStream = null;
        BufferedInputStream bufferedStream = null;

        try {
            inputStream = new FileInputStream(file);
            bufferedStream = new BufferedInputStream(inputStream);

            // Detect file type and exclude non-images and non-videos
            FileType fileType = FileTypeDetector.detectFileType(bufferedStream);
            String mimeType = fileType.getMimeType();
            if (mimeType == null || !mimeType.matches("(image|video)/.+")) { // TODO 各种图片格式的宽高要分别从directory提取
                return null;
            }

            MediaInfo mediaInfo = new MediaInfo();
            mediaInfo.setMimeType(mimeType);

            Photo photo = new Photo();
            photo.setMediaInfo(mediaInfo);

            BasicFileAttributes attrs = java.nio.file.Files.readAttributes(file.toPath(), BasicFileAttributes.class);

            photo.setCreationTime(attrs.creationTime().toMillis());
            photo.setModifiedTime(file.lastModified());
            photo.setPath(file.getPath().substring(library.length()));

            // Parse metadata to photo entity
            Metadata metadata = ImageMetadataReader.readMetadata(bufferedStream, photo.getLength(), fileType);
            for (Directory dir : metadata.getDirectories()) {
                if (dir instanceof ExifIFD0Directory) {
                    // photo.setShootingTime(dir.getDate(ExifDirectoryBase.TAG_DATETIME).getTime());
                    // photo.setMakeModel(
                    // dir.getString(ExifDirectoryBase.TAG_MAKE) + " " +
                    // dir.getString(ExifDirectoryBase.TAG_MODEL));
                }
                else
                    if (dir instanceof JpegDirectory) {
                        mediaInfo.setWidth(dir.getInteger(JpegDirectory.TAG_IMAGE_WIDTH));
                        mediaInfo.setHeight(dir.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT));
                    }
                    else if (dir instanceof GifHeaderDirectory) {
                        mediaInfo.setWidth(dir.getInteger(GifHeaderDirectory.TAG_IMAGE_WIDTH));
                        mediaInfo.setHeight(dir.getInteger(GifHeaderDirectory.TAG_IMAGE_HEIGHT));
                    }
                    else if (dir instanceof PngDirectory) {
                        PngDirectory pngDir = (PngDirectory) dir;
                        PngChunkType pngType = pngDir.getPngChunkType();
                        if (pngType.equals(PngChunkType.IHDR)) {
                            mediaInfo.setWidth(dir.getInteger(PngDirectory.TAG_IMAGE_WIDTH));
                            mediaInfo.setHeight(dir.getInteger(PngDirectory.TAG_IMAGE_HEIGHT));
                        }
                    }
                    else if (dir instanceof ExifSubIFDDirectory) {
                        Parameter params = new Parameter();
                        params.setApertureValue(dir.getString(ExifSubIFDDirectory.TAG_FNUMBER));
                        params.setShutterSpeed(dir.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
                        params.setIsoSpeed(dir.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
                        params.setExposureBias(dir.getString(ExifSubIFDDirectory.TAG_EXPOSURE_BIAS));
                        params.setFocalLength(
                            dir.getString(ExifSubIFDDirectory.TAG_35MM_FILM_EQUIV_FOCAL_LENGTH));
                        photo.setParameter(params);
                    }
                    else if (dir instanceof GpsDirectory) {
                        GeoLocation geoLocation = extractGeoLocation((GpsDirectory) dir);
                        photo.setGeoLocation(geoLocation);
                    }
            }
            return photo;
        } finally {
            bufferedStream.close();
            inputStream.close();
        }
    }

    private GeoLocation extractGeoLocation(GpsDirectory gps) {
        com.drew.lang.GeoLocation geo = gps.getGeoLocation();
        GeoLocation geoLocation = new GeoLocation();
        geoLocation.setLatitude(geo.getLatitude());
        geoLocation.setLongitude(geo.getLongitude());
        geoLocation.setAltitude(gps.getDoubleObject(GpsDirectory.TAG_ALTITUDE));
        return geoLocation;
    }

    // public static void main(String[] args) {
    // LibraryService libraryService = new LibraryService();
    // libraryService.scan();
    // }

}