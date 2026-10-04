package com.evops.museum.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 修复阶段结果登记请求。
 */
public class StageRecordRequest {
    @NotBlank(message = "阶段名称不能为空")
    @Size(max = 128)
    private String stageName;
    @Size(max = 2000)
    private String result;

    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}
