package com.evops.museum.controller;

import com.evops.common.ApiResponse;
import com.evops.museum.dto.AcquisitionCreateRequest;
import com.evops.museum.entity.Acquisition;
import com.evops.museum.service.AcquisitionService;
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
 * 入藏登记接口：照片、尺寸、库位随登记落库；撤销仅改状态。
 */
@RestController
@RequestMapping("/api/museum/acquisitions")
public class AcquisitionController {

    private final AcquisitionService acquisitionService;

    public AcquisitionController(AcquisitionService acquisitionService) {
        this.acquisitionService = acquisitionService;
    }

    @PostMapping
    public ApiResponse<Acquisition> register(@Validated @RequestBody AcquisitionCreateRequest req) {
        return ApiResponse.ok(acquisitionService.register(req));
    }

    @GetMapping("/{id}")
    public ApiResponse<Acquisition> detail(@PathVariable Long id) {
        Acquisition acquisition = acquisitionService.getById(id);
        if (acquisition == null) {
            throw new IllegalArgumentException("入藏登记不存在: " + id);
        }
        return ApiResponse.ok(acquisition);
    }

    @GetMapping
    public ApiResponse<List<Acquisition>> listByArtifact(@RequestParam Long artifactId) {
        return ApiResponse.ok(acquisitionService.listByArtifact(artifactId));
    }

    @PostMapping("/{id}/revoke")
    public ApiResponse<Acquisition> revoke(@PathVariable Long id) {
        return ApiResponse.ok(acquisitionService.revoke(id));
    }
}
