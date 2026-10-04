package com.evops.museum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import com.evops.museum.enums.ProjectStatus;

import java.time.LocalDateTime;

/**
 * 修复项目：委托人、修复方案。project_code 全局唯一。
 */
@TableName("t_restoration_project")
public class RestorationProject extends BaseEntity {
    private String projectCode;
    private Long artifactId;
    private String clientName;
    private String plan;
    private ProjectStatus status;
    private LocalDateTime acceptedAt;
    private String remark;

    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
    public Long getArtifactId() { return artifactId; }
    public void setArtifactId(Long artifactId) { this.artifactId = artifactId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
