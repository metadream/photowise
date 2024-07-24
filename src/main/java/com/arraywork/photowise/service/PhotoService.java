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
import com.arraywork.springforce.util.Times;

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
    private PhotoRepo photoRepo;
    @Resource
    private SettingService settingService;
    @Resource
    private OsmService osmService;

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

        // 如果有经纬度但没有地址信息，调用OSM API获取
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

    /** Save photo index */
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