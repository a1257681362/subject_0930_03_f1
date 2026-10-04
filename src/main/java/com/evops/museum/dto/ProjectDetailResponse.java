package com.evops.museum.dto;

import com.evops.museum.entity.RestorationProject;
import com.evops.museum.entity.RestorationStage;

import java.util.List;

/**
 * 修复项目详情：项目本体 + 按序排列的阶段结果。
 */
public class ProjectDetailResponse {
    private RestorationProject project;
    private List<RestorationStage> stages;

    public ProjectDetailResponse(RestorationProject project, List<RestorationStage> stages) {
        this.project = project;
        this.stages = stages;
    }

    public RestorationProject getProject() { return project; }
    public List<RestorationStage> getStages() { return stages; }
}
