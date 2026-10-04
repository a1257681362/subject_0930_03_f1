package com.evops.museum.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 文物建档请求。artifactCode 是幂等键：相同编号重复提交返回原档案，不产生第二条记录。
 */
public class ArtifactCreateRequest {
    @NotBlank(message = "文物编号不能为空")
    @Size(max = 64)
    private String artifactCode;
    @NotBlank(message = "文物名称不能为空")
    @Size(max = 128)
    private String name;
    @Size(max = 64)
    private String material;
    @Size(max = 64)
    private String era;
    @Size(max = 255)
    private String source;
    @Size(max = 32)
    private String preservationLevel;
    @Size(max = 500)
    private String remark;

    public String getArtifactCode() { return artifactCode; }
    public void setArtifactCode(String artifactCode) { this.artifactCode = artifactCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public String getEra() { return era; }
    public void setEra(String era) { this.era = era; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getPreservationLevel() { return preservationLevel; }
    public void setPreservationLevel(String preservationLevel) { this.preservationLevel = preservationLevel; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
