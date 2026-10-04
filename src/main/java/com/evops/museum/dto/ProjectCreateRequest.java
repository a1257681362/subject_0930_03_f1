package com.evops.museum.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 修复项目立项请求。projectCode 是幂等键：相同编号重复提交返回原项目，不产生第二条记录。
 */
public class ProjectCreateRequest {
    @NotBlank(message = "项目编号不能为空")
    @Size(max = 64)
    private String projectCode;
    @NotNull(message = "文物ID不能为空")
    private Long artifactId;
    @NotBlank(message = "委托人不能为空")
    @Size(max = 128)
    private String clientName;
    @Size(max = 2000)
    private String plan;
    @Size(max = 500)
    private String remark;

    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
    public Long getArtifactId() { return artifactId; }
    public void setArtifactId(Long artifactId) { this.artifactId = artifactId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
