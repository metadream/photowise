package com.arraywork.photowise.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arraywork.photowise.entity.AppSetting;

/**
 * Setting Repository
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/12
 */
public interface SettingRepo extends JpaRepository<AppSetting, String> {
}