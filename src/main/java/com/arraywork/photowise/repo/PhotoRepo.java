package com.arraywork.photowise.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.arraywork.photowise.entity.Photo;

/**
 * Photo Repository
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
public interface PhotoRepo extends JpaRepository<Photo, String>, JpaSpecificationExecutor<Photo> {

    Photo findByPath(String path);

}