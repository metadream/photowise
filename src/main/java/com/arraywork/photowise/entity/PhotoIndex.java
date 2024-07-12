package com.arraywork.photowise.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

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

    // file path relative to library
    @Column(unique = true)
    @NotBlank(message = "Photo path cannot be blank")
    private String path;

    // file size in bytes
    private long length;

    ////////////////////////////// Original metadata

    @Type(JsonStringType.class)
    private MediaInfo mediaInfo;

    @Type(JsonStringType.class)
    private CameraInfo cameraInfo;

    @Type(JsonStringType.class)
    private GeoLocation geoLocation;

    ////////////////////////////// Additional metadata

    @Size(max = 80, message = "Title cannot exceed {max} characters")
    private String title;

    @Size(max = 80, message = "Place cannot exceed {max} characters")
    private String place;

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

    // Date and time

    private long shootingTime;
    private long creationTime;
    private long modifiedTime;

    @UpdateTimestamp
    private LocalDateTime lastModified;

    private boolean video;
    private boolean favored;

    public CameraInfo getCameraInfo() {
        if (cameraInfo == null) {
            cameraInfo = new CameraInfo();
        }
        return cameraInfo;
    }

}