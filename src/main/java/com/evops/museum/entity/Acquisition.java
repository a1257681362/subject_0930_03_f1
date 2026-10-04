package com.evops.museum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import com.evops.museum.enums.AcquisitionStatus;

import java.time.LocalDateTime;

/**
 * 入藏登记：关联文物档案，记录照片（逗号分隔）、尺寸、库位。
 */
@TableName("t_acquisition")
public class Acquisition extends BaseEntity {
    private String acquisitionNo;
    private Long artifactId;
    private String photos;
    private String dimensions;
    private String storageLocation;
    private LocalDateTime acquiredAt;
    private AcquisitionStatus status;
    private String remark;

    public String getAcquisitionNo() { return acquisitionNo; }
    public void setAcquisitionNo(String acquisitionNo) { this.acquisitionNo = acquisitionNo; }
    public Long getArtifactId() { return artifactId; }
    public void setArtifactId(Long artifactId) { this.artifactId = artifactId; }
    public String getPhotos() { return photos; }
    public void setPhotos(String photos) { this.photos = photos; }
    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    public LocalDateTime getAcquiredAt() { return acquiredAt; }
    public void setAcquiredAt(LocalDateTime acquiredAt) { this.acquiredAt = acquiredAt; }
    public AcquisitionStatus getStatus() { return status; }
    public void setStatus(AcquisitionStatus status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
