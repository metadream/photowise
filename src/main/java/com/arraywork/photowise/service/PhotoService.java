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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arraywork.photowise.entity.PhotoIndex;
import com.arraywork.photowise.repo.PhotoFilter;
import com.arraywork.photowise.repo.PhotoRepo;
import com.arraywork.springforce.util.Times;

import jakarta.annotation.Resource;

/**
 * Photo Service
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Service
public class PhotoService {

    @Resource
    private PhotoRepo photoRepo;

    @Value("${photowise.library}")
    private String library;

    @Value("${photowise.trash}")
    private String trash;

    // 获取所有索引
    public List<PhotoIndex> getIndexes() {
        return photoRepo.findAll();
    }

    // 根据条件获取索引并按年月分组
    public Map<String, List<PhotoIndex>> getIndexes(PhotoIndex condition) {
        Map<String, List<PhotoIndex>> photoGroup = new TreeMap<>(Comparator.reverseOrder());
        List<PhotoIndex> indexes = photoRepo.findAll(new PhotoFilter(condition));

        for (PhotoIndex photo : indexes) {
            LocalDateTime photoTime = Times.toLocal(photo.getPhotoTime());
            String yearMonth = photoTime.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            List<PhotoIndex> photos = photoGroup.get(yearMonth);
            if (photos == null) {
                photos = new ArrayList<>();
                photoGroup.put(yearMonth, photos);
            }
            photos.add(photo);
        }
        return photoGroup;
    }

    // Get photo by file path
    public PhotoIndex getPhoto(String path) {
        return photoRepo.findByPath(path);
    }

    // Save photo
    @Transactional(rollbackFor = Exception.class)
    public PhotoIndex save(PhotoIndex photo) {
        return photoRepo.save(photo);
    }

    // Delete photo
    @Transactional(rollbackFor = Exception.class)
    public void delete(PhotoIndex photo) {
        photoRepo.delete(photo);
    }

    // 批量切换收藏
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

    // 批量移入回收站
    @Transactional(rollbackFor = Exception.class)
    public int trash(String[] photoIds) throws IOException {
        int result = 0;
        for (String id : photoIds) {
            PhotoIndex photo = photoRepo.getReferenceById(id);
            photo.setTrashed(true);

            Path original = Path.of(library, photo.getPath());
            Path dest = Path.of(trash, photo.getPath());
            File parent = dest.getParent().toFile();
            if (!parent.exists()) parent.mkdirs();
            Files.move(original, dest, StandardCopyOption.REPLACE_EXISTING);
            result++;
        }
        return result;
    }

}