package com.evops.museum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.evops.common.BaseEntity;
import com.evops.museum.enums.LoanStatus;

import java.time.LocalDateTime;

/**
 * 借出/归还记录：借出、归还、撤销均落库，形成完整链路。
 */
@TableName("t_loan_record")
public class LoanRecord extends BaseEntity {
    private String loanNo;
    private Long artifactId;
    private String borrower;
    private String purpose;
    private LocalDateTime lentAt;
    private LocalDateTime expectedReturnAt;
    private LocalDateTime returnedAt;
    private LoanStatus status;
    private String remark;

    public String getLoanNo() { return loanNo; }
    public void setLoanNo(String loanNo) { this.loanNo = loanNo; }
    public Long getArtifactId() { return artifactId; }
    public void setArtifactId(Long artifactId) { this.artifactId = artifactId; }
    public String getBorrower() { return borrower; }
    public void setBorrower(String borrower) { this.borrower = borrower; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public LocalDateTime getLentAt() { return lentAt; }
    public void setLentAt(LocalDateTime lentAt) { this.lentAt = lentAt; }
    public LocalDateTime getExpectedReturnAt() { return expectedReturnAt; }
    public void setExpectedReturnAt(LocalDateTime expectedReturnAt) { this.expectedReturnAt = expectedReturnAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
