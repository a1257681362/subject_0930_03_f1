package com.evops.museum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import com.evops.museum.enums.ArtifactStatus;

/**
 * 文物档案：材质、年代、来源、保存等级。artifact_code 全局唯一。
 */
@TableName("t_artifact")
public class Artifact extends BaseEntity {
    private String artifactCode;
    private String name;
    private String material;
    private String era;
    private String source;
    private String preservationLevel;
    private ArtifactStatus status;
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
    public ArtifactStatus getStatus() { return status; }
    public void setStatus(ArtifactStatus status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
