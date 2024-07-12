package com.arraywork.photowise.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arraywork.photowise.entity.AppUser;

/**
 * User Repository
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/12
 */
public interface UserRepo extends JpaRepository<AppUser, String> {}