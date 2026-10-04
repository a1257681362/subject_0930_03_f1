package com.evops.museum.controller;

import com.evops.common.ApiResponse;
import com.evops.museum.dto.LoanCreateRequest;
import com.evops.museum.entity.LoanRecord;
import com.evops.museum.service.LoanService;
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
 * 借出/归还接口：借出 -> 归还闭环，撤销仅改状态。
 */
@RestController
@RequestMapping("/api/museum/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    public ApiResponse<LoanRecord> lend(@Validated @RequestBody LoanCreateRequest req) {
        return ApiResponse.ok(loanService.lend(req));
    }

    @PostMapping("/{id}/return")
    public ApiResponse<LoanRecord> returnBack(@PathVariable Long id) {
        return ApiResponse.ok(loanService.returnBack(id));
    }

    @PostMapping("/{id}/revoke")
    public ApiResponse<LoanRecord> revoke(@PathVariable Long id) {
        return ApiResponse.ok(loanService.revoke(id));
    }

    @GetMapping
    public ApiResponse<List<LoanRecord>> listByArtifact(@RequestParam Long artifactId) {
        return ApiResponse.ok(loanService.listByArtifact(artifactId));
    }
}
