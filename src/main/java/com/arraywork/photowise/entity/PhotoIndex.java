package com.arraywork.photowise.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import com.arraywork.photowise.enums.MediaType;
import com.arraywork.springforce.id.NanoIdGeneration;

import io.hypersistence.utils.hibernate.type.json.JsonStringType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Photo Index
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/01
 */
@Entity
@DynamicInsert
@Data
public class PhotoIndex {

    @Id
    @NanoIdGeneration
    @Column(length = 24, insertable = false, updatable = false)
    private String id;

    // 照片文件相对（照片库）路径
    @Column(unique = true)
    @NotBlank(message = "照片路径不能为空")
    private String path;

    ////////////////////////////// EXIF 元数据

    @Type(JsonStringType.class)
    private MediaInfo mediaInfo;

    @Type(JsonStringType.class)
    private CameraInfo cameraInfo;

    @Type(JsonStringType.class)
    private GeoLocation geoLocation;

    ////////////////////////////// 附加属性

    @Size(max = 80, message = "照片标题不能超过 {max} 个字")
    private String title;

    @Size(max = 80, message = "照片地点不能超过 {max} 个字")
    private String place;

    @Size(max = 20, message = "照片城市不能超过 {max} 个字")
    private String city;

    @Type(JsonStringType.class)
    @Column(columnDefinition = "JSON DEFAULT (JSON_ARRAY())")
    private String[] people;

    @Type(JsonStringType.class)
    @Column(columnDefinition = "JSON DEFAULT (JSON_ARRAY())")
    private String[] animals;

    @Type(JsonStringType.class)
    @Column(columnDefinition = "JSON DEFAULT (JSON_ARRAY())")
    private String[] things;

    @Type(JsonStringType.class)
    @Column(columnDefinition = "JSON DEFAULT (JSON_ARRAY())")
    private String[] albums;

    private MediaType mediaType;
    private boolean isFavored;

    ////////////////////////////// 日期时间

    // 照片创建时间（如果有拍摄时间则与之相等且不可修改，没有则取文件创建时间且可修改）
    private long photoTime;

    // 照片拍摄时间（取自EXIF元数据）
    private long originalTime;

    // 文件更新时间（用于跳过扫描）
    private long modifiedTime;

    // 数据创建时间
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime creationTime;

    // 数据更新时间
    @UpdateTimestamp
    private LocalDateTime lastModified;

    ////////////////////////////// 公共方法

    public CameraInfo getCameraInfo() {
        if (cameraInfo == null) {
            cameraInfo = new CameraInfo();
        }
        return cameraInfo;
    }

    public boolean isVideo() {
        return mediaType == MediaType.video;
    }

}