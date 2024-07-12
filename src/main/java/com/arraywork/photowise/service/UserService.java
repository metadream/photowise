package com.arraywork.photowise.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arraywork.photowise.entity.AppUser;
import com.arraywork.photowise.repo.UserRepo;

import jakarta.annotation.Resource;

/**
 * User Service
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/12
 */
@Service
public class UserService {

    @Resource
    private UserRepo userRepo;

    // 保存用户
    @Transactional(rollbackFor = Exception.class)
    public AppUser save(AppUser user) {
        return userRepo.save(user);
    }

}