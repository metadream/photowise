package com.arraywork.photowise.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arraywork.photowise.entity.Photo;
import com.arraywork.photowise.repo.PhotoRepo;
import com.arraywork.springforce.util.Pagination;

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

    @Value("${photowise.pagesize}")
    private int pagesize;

    // 查询分页元数据
    public Pagination<Photo> getPhotos(String page, Photo condition) {
        Sort sort = Sort.by("modifiedTime").descending();
        page = page != null && page.matches("\\d+") ? page : "1";
        Pageable pageable = PageRequest.of(Integer.parseInt(page) - 1, pagesize, sort);
        Page<Photo> pageInfo = photoRepo.findAll(pageable);
        return new Pagination<Photo>(pageInfo);
    }

    // 查询所有照片索引
    public List<Photo> getAllPhotos() {
        return photoRepo.findAll();
    }

    // Get photo by file path
    public Photo getPhoto(String path) {
        return photoRepo.findByPath(path);
    }

    // Save photo
    @Transactional(rollbackFor = Exception.class)
    public Photo save(Photo photo) {
        return photoRepo.save(photo);
    }

    // Delete photo
    @Transactional(rollbackFor = Exception.class)
    public void delete(Photo photo) {
        photoRepo.delete(photo);
    }

}