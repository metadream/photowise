package com.arraywork.photowise.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arraywork.photowise.entity.GeoLocation;
import com.arraywork.photowise.entity.OsmAddress;
import com.arraywork.photowise.entity.PhotoIndex;
import com.arraywork.photowise.repo.PhotoFilter;
import com.arraywork.photowise.repo.PhotoRepo;
import com.arraywork.springforce.util.Assert;
import com.arraywork.springforce.util.OpenCv;
import com.arraywork.springforce.util.Times;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;

/**
 * Photo Service
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Service
public class PhotoService {

    @Resource
    private SettingService settingService;
    @Resource
    private ExifService exifService;
    @Resource
    private OsmService osmService;
    @Resource
    private PhotoRepo photoRepo;

    @Value("${photowise.thumbnails}")
    private String thumbnails;

    @Value("${photowise.thumb-size}")
    private int thumbSize;

    @Value("${photowise.trash}")
    private String trash;

    /** Get all photo indexes */
    public List<PhotoIndex> getPhotos() {
        return photoRepo.findAll();
    }

    /** Get photo indexes with condition and group by year and month */
    public Map<String, List<PhotoIndex>> getPhotos(PhotoIndex condition) {
        Map<String, List<PhotoIndex>> photoGroup = new TreeMap<>(Comparator.reverseOrder());
        List<PhotoIndex> indexes = photoRepo.findAll(new PhotoFilter(condition));

        for (PhotoIndex photo : indexes) {
            LocalDateTime photoTime = Times.toLocal(photo.getPhotoTime());
            String yearMonth = photoTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            List<PhotoIndex> photos = photoGroup.computeIfAbsent(yearMonth, k -> new ArrayList<>());
            photos.add(photo);
        }
        return photoGroup;
    }

    /** Get photo index by primary ID */
    public PhotoIndex getPhoto(String id) {
        PhotoIndex photo = photoRepo.findById(id).orElse(null);
        Assert.notNull(photo, "照片索引不存在");

        // If there is latitude and longitude but no address, obtain from the OSM API
        GeoLocation location = photo.getGeoLocation();
        if (location != null && location.getAddress() == null) {
            OsmAddress address = osmService.reverse(location.getLatitude(), location.getLongitude());
            location.setAddress(address);
        }
        return photoRepo.save(photo);
    }

    /** Get photo index by file path */
    public PhotoIndex getPhotoByPath(String path) {
        return photoRepo.findByPath(path);
    }

    /** Cumulative used space */
    public long getUsedSpace() {
        return photoRepo.sumFileLength();
    }

    /**
     * Build photo index from a file
     * If overwrite is true, enforce update the index and thumbnails
     */
    @Transactional(rollbackFor = Exception.class)
    public PhotoIndex build(File file, boolean overwrite)
        throws ImageProcessingException, IOException, MetadataException {
        String library = settingService.getLibrary();
        String absolutePath = file.getPath();
        String relativePath = absolutePath.substring(library.length());

        // 0. Determines whether to skip build photo index
        //    based on the scan parameters, file length, and modified time
        PhotoIndex _photo = getPhotoByPath(relativePath);
        if (!overwrite && _photo != null
            && _photo.getFileLength() == file.length()
            && _photo.getModifiedTime() == file.lastModified()) {
            return null;
        }

        // 1. Extract metadata
        PhotoIndex photo = exifService.extractMetadata(file);
        photo.setId(_photo != null ? _photo.getId() : null);
        photo.setPath(relativePath);
        photo.setFileLength(file.length());
        photo.setModifiedTime(file.lastModified());

        // 2. Generate thumbnails
        String output = Path.of(thumbnails, photo.getPath()) + ".jpg";
        if (photo.isVideo()) {
            OpenCv.captureVideo(absolutePath, output, thumbSize);
        } else {
            OpenCv.resizeImage(absolutePath, output, thumbSize);
        }

        // 3. Save the photo index
        return save(photo);
    }

    /** Save photo index metadata */
    @Transactional(rollbackFor = Exception.class)
    public PhotoIndex save(PhotoIndex photo) {
        // TODO validate
        return photoRepo.save(photo);
    }

    /** Delete photo index */
    @Transactional(rollbackFor = Exception.class)
    public void delete(PhotoIndex photo) {
        // TODO delete thumbnails...
        photoRepo.delete(photo);
    }

    /** Switch favorites in batches */
    @Transactional(rollbackFor = Exception.class)
    public int favorite(String[] photoIds, boolean favored) {
        int result = 0;
        for (String id : photoIds) {
            PhotoIndex photo = photoRepo.getReferenceById(id);
            photo.setFavored(favored);
            result++;
        }
        return result;
    }

    /** Move to the trash in batches */
    @Transactional(rollbackFor = Exception.class)
    public int trash(String[] photoIds) throws IOException {
        int result = 0;
        for (String id : photoIds) {
            PhotoIndex photo = photoRepo.getReferenceById(id);
            photo.setTrashed(true);

            Path original = Path.of(settingService.getLibrary(), photo.getPath());
            Path dest = Path.of(trash, photo.getPath());
            File parent = dest.getParent().toFile();
            if (!parent.exists()) parent.mkdirs();
            Files.move(original, dest, StandardCopyOption.REPLACE_EXISTING);
            result++;
        }
        return result;
    }

}