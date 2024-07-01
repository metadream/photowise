package com.arraywork.photowise.service;

import org.springframework.data.domain.Page;
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

    // 查询分页元数据
    public Pagination<Photo> getPhotos(String page, Photo condition) {
        Sort sort = Sort.by("lastModified").descending().and(Sort.by("code").descending());
        page = page != null && page.matches("\\d+") ? page : "1";
        // Pageable pageable = PageRequest.of(Integer.parseInt(page) - 1, pageSize,
        // sort);
        Page<Photo> pageInfo = null;// photoRepo.findAll(new MetadataSpec(condition), pageable);
        return new Pagination<Photo>(pageInfo);
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

}