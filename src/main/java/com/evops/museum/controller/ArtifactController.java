package com.evops.museum.controller;

import com.evops.common.ApiResponse;
import com.evops.museum.dto.ArtifactCreateRequest;
import com.evops.museum.entity.Artifact;
import com.evops.museum.enums.ArtifactStatus;
import com.evops.museum.service.ArtifactService;
import com.evops.museum.service.IdempotentCreateResult;
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
 * 文物档案接口。建档按 artifactCode 幂等：重复提交返回原档案，不产生第二条记录。
 */
@RestController
@RequestMapping("/api/museum/artifacts")
public class ArtifactController {

    private final ArtifactService artifactService;

    public ArtifactController(ArtifactService artifactService) {
        this.artifactService = artifactService;
    }

    @PostMapping
    public ApiResponse<IdempotentCreateResult<Artifact>> create(@Validated @RequestBody ArtifactCreateRequest req) {
        return ApiResponse.ok(artifactService.createArchive(req));
    }

    @GetMapping("/{id}")
    public ApiResponse<Artifact> detail(@PathVariable Long id) {
        Artifact artifact = artifactService.getById(id);
        if (artifact == null) {
            throw new IllegalArgumentException("文物不存在: " + id);
        }
        return ApiResponse.ok(artifact);
    }

    @GetMapping
    public ApiResponse<List<Artifact>> list(@RequestParam(required = false) String status) {
        ArtifactStatus parsed = null;
        if (status != null && !status.isEmpty()) {
            try {
                parsed = ArtifactStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效文物状态: " + status);
            }
        }
        return ApiResponse.ok(artifactService.listByStatus(parsed));
    }

    @PostMapping("/{id}/revoke")
    public ApiResponse<Artifact> revoke(@PathVariable Long id) {
        return ApiResponse.ok(artifactService.revoke(id));
    }
}
