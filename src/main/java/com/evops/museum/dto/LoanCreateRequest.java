package com.evops.museum.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 借出登记请求。
 */
public class LoanCreateRequest {
    @NotNull(message = "文物ID不能为空")
    private Long artifactId;
    @NotBlank(message = "借用人/单位不能为空")
    @Size(max = 128)
    private String borrower;
    @Size(max = 500)
    private String purpose;
    private LocalDateTime expectedReturnAt;
    @Size(max = 500)
    private String remark;

    public Long getArtifactId() { return artifactId; }
    public void setArtifactId(Long artifactId) { this.artifactId = artifactId; }
    public String getBorrower() { return borrower; }
    public void setBorrower(String borrower) { this.borrower = borrower; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public LocalDateTime getExpectedReturnAt() { return expectedReturnAt; }
    public void setExpectedReturnAt(LocalDateTime expectedReturnAt) { this.expectedReturnAt = expectedReturnAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
