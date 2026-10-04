package com.evops.museum.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入藏登记请求：关联文物，记录照片、尺寸、库位。
 */
public class AcquisitionCreateRequest {
    @NotNull(message = "文物ID不能为空")
    private Long artifactId;
    @Size(max = 32, message = "照片数量不能超过32张")
    private List<String> photoUrls;
    @Size(max = 255)
    private String dimensions;
    @Size(max = 128)
    private String storageLocation;
    private LocalDateTime acquiredAt;
    @Size(max = 500)
    private String remark;

    public Long getArtifactId() { return artifactId; }
    public void setArtifactId(Long artifactId) { this.artifactId = artifactId; }
    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }
    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    public LocalDateTime getAcquiredAt() { return acquiredAt; }
    public void setAcquiredAt(LocalDateTime acquiredAt) { this.acquiredAt = acquiredAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
