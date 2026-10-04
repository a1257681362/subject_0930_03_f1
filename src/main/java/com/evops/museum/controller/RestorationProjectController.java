package com.evops.museum.controller;

import com.evops.common.ApiResponse;
import com.evops.museum.dto.ProjectCreateRequest;
import com.evops.museum.dto.ProjectDetailResponse;
import com.evops.museum.dto.StageRecordRequest;
import com.evops.museum.entity.RestorationProject;
import com.evops.museum.entity.RestorationStage;
import com.evops.museum.service.IdempotentCreateResult;
import com.evops.museum.service.RestorationProjectService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 修复项目接口：立项(幂等) -> 阶段结果 -> 提交验收 -> 验收，撤销仅改状态。
 */
@RestController
@RequestMapping("/api/museum/restoration-projects")
public class RestorationProjectController {

    private final RestorationProjectService projectService;

    public RestorationProjectController(RestorationProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ApiResponse<IdempotentCreateResult<RestorationProject>> create(@Validated @RequestBody ProjectCreateRequest req) {
        return ApiResponse.ok(projectService.createProject(req));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(projectService.detail(id));
    }

    @GetMapping
    public ApiResponse<List<RestorationProject>> listByArtifact(@RequestParam Long artifactId) {
        return ApiResponse.ok(projectService.listByArtifact(artifactId));
    }

    @PostMapping("/{id}/stages")
    public ApiResponse<RestorationStage> recordStage(@PathVariable Long id,
                                                     @Validated @RequestBody StageRecordRequest req) {
        return ApiResponse.ok(projectService.recordStage(id, req));
    }

    @PostMapping("/{id}/submit-acceptance")
    public ApiResponse<RestorationProject> submitAcceptance(@PathVariable Long id) {
        return ApiResponse.ok(projectService.submitAcceptance(id));
    }

    @PostMapping("/{id}/accept")
    public ApiResponse<RestorationProject> accept(@PathVariable Long id) {
        return ApiResponse.ok(projectService.accept(id));
    }

    @PostMapping("/{id}/revoke")
    public ApiResponse<RestorationProject> revoke(@PathVariable Long id) {
        return ApiResponse.ok(projectService.revoke(id));
    }
}
