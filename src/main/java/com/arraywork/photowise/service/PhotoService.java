package com.arraywork.photowise.service;

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

    @Value("${photowise.page-size}")
    private int pageSize;

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

}