package com.arraywork.photowise.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arraywork.photowise.entity.PhotoIndex;
import com.arraywork.photowise.enums.MediaType;
import com.arraywork.photowise.repo.PhotoFilter;
import com.arraywork.photowise.repo.PhotoRepo;

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

    // 获取照片列表
    public List<PhotoIndex> getPhotos() {
        PhotoIndex condition = new PhotoIndex();
        condition.setMediaType(MediaType.IMAGE);
        return photoRepo.findAll(new PhotoFilter(condition));
    }

    // 获取视频列表
    public List<PhotoIndex> getVideos() {
        PhotoIndex condition = new PhotoIndex();
        condition.setMediaType(MediaType.VIDEO);
        return photoRepo.findAll(new PhotoFilter(condition));
    }

    // 获取收藏夹列表
    public List<PhotoIndex> getFavored() {
        PhotoIndex condition = new PhotoIndex();
        condition.setFavored(true);
        return photoRepo.findAll(new PhotoFilter(condition));
    }

    // 获取回收站列表
    public List<PhotoIndex> getTrashed() {
        PhotoIndex condition = new PhotoIndex();
        condition.setTrashed(true);
        return photoRepo.findAll(new PhotoFilter(condition));
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