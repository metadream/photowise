package com.arraywork.photowise.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.stereotype.Service;

import com.arraywork.photowise.entity.CameraInfo;
import com.arraywork.photowise.entity.GeoLocation;
import com.arraywork.photowise.entity.MediaInfo;
import com.arraywork.photowise.entity.PhotoIndex;
import com.arraywork.photowise.enums.MediaType;
import com.arraywork.springforce.util.Assert;
import com.arraywork.springforce.util.FileUtils;
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
 * Exif Metadata Service
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/24
 */
@Service
public class ExifService {

    private static final String SUPPORTED_FORMATS = "JPEG|PNG|WebP|HEIC|HEIF|MP4|MOV";

    /** Extract image or video metadata from a file */
    public PhotoIndex extractMetadata(File file)
        throws IOException, ImageProcessingException, MetadataException {

        try (InputStream inputStream = new FileInputStream(file);
             BufferedInputStream bufferedStream = new BufferedInputStream(inputStream)) {

            // Detect the file type
            FileType fileType = FileTypeDetector.detectFileType(bufferedStream);
            Assert.isTrue(fileType.getName().matches(SUPPORTED_FORMATS), "不支持的媒体格式");

            // Create photo index
            PhotoIndex photo = new PhotoIndex();
            MediaInfo mediaInfo = new MediaInfo();
            photo.setMediaInfo(mediaInfo);
            photo.setMediaType(MediaType.of(fileType.getMimeType()));

            // Parse metadata into photo index
            Metadata metadata = ImageMetadataReader.readMetadata(bufferedStream, file.length(), fileType);
            for (Directory dir : metadata.getDirectories()) {
                // Universal EXIF
                if (dir instanceof ExifIFD0Directory) {
                    // Original time
                    Date date = dir.getDate(ExifDirectoryBase.TAG_DATETIME, TimeZone.getDefault());
                    if (date != null) {
                        photo.setOriginalTime(date.getTime());
                    }
                    // Camera make and model
                    String make = dir.getString(ExifDirectoryBase.TAG_MAKE);
                    if (make != null) {
                        CameraInfo cameraInfo = photo.getCameraInfo();
                        String model = dir.getString(ExifDirectoryBase.TAG_MODEL);
                        if (model != null) make += " " + model;
                        cameraInfo.setMakeModel(make);
                    }
                } else if (dir instanceof ExifSubIFDDirectory) {
                    // Original time (including time zone)
                    String tz = dir.getString(ExifSubIFDDirectory.TAG_TIME_ZONE);
                    TimeZone timeZone = tz != null ? TimeZone.getTimeZone(ZoneId.of(tz)) : TimeZone.getDefault();
                    Date date = dir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, timeZone);
                    if (date != null) {
                        photo.setOriginalTime(date.getTime());
                        photo.setTimeZone(timeZone.getDisplayName());
                    }
                    // Camera parameters
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
                    // HEIF picture dimensions (NOT in HeifDirectory)
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

            // If the original time does not exist, get the file creation time and default time zone
            long photoTime = photo.getOriginalTime() > 0 ? photo.getOriginalTime() : FileUtils.getCreationTime(file);
            String timeZone = photo.getTimeZone() != null ? photo.getTimeZone() : TimeZone.getDefault().getDisplayName();
            photo.setPhotoTime(photoTime);
            photo.setTimeZone(timeZone);
            return photo;
        }
    }

    /** Extract GPS metadata */
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