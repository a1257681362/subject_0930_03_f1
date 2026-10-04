package com.evops.museum.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evops.museum.dto.ProjectCreateRequest;
import com.evops.museum.dto.ProjectDetailResponse;
import com.evops.museum.dto.StageRecordRequest;
import com.evops.museum.entity.Artifact;
import com.evops.museum.entity.RestorationProject;
import com.evops.museum.entity.RestorationStage;
import com.evops.museum.enums.ArtifactStatus;
import com.evops.museum.enums.ProjectStatus;
import com.evops.museum.mapper.RestorationProjectMapper;
import com.evops.museum.mapper.RestorationStageMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 修复项目服务：立项 -> 阶段结果 -> 提交验收 -> 验收 形成完整链路。
 * 幂等约定：projectCode 为幂等键，重复提交返回原项目，不产生第二条记录，
 * 且重复请求不会重复改变文物状态。
 */
@Service
public class RestorationProjectService extends ServiceImpl<RestorationProjectMapper, RestorationProject> {

    private final ArtifactService artifactService;
    private final RestorationStageMapper stageMapper;

    public RestorationProjectService(ArtifactService artifactService, RestorationStageMapper stageMapper) {
        this.artifactService = artifactService;
        this.stageMapper = stageMapper;
    }

    @Transactional
    public IdempotentCreateResult<RestorationProject> createProject(ProjectCreateRequest req) {
        RestorationProject existing = findByCode(req.getProjectCode());
        if (existing != null) {
            return IdempotentCreateResult.replayed(existing);
        }
        Artifact artifact = artifactService.requireActive(req.getArtifactId());
        if (artifact.getStatus() != ArtifactStatus.IN_STOCK) {
            throw new IllegalStateException("仅在库文物可立项修复，当前状态: " + artifact.getStatus());
        }
        RestorationProject project = new RestorationProject();
        project.setProjectCode(req.getProjectCode());
        project.setArtifactId(artifact.getId());
        project.setClientName(req.getClientName());
        project.setPlan(req.getPlan());
        project.setStatus(ProjectStatus.PLANNED);
        project.setRemark(req.getRemark());
        try {
            save(project);
        } catch (DuplicateKeyException e) {
            // 并发下唯一约束兜底：返回已存在的项目，保证重复请求不产生第二条
            return IdempotentCreateResult.replayed(findByCode(req.getProjectCode()));
        }
        artifactService.markRestoring(artifact.getId());
        return IdempotentCreateResult.created(project);
    }

    /**
     * 登记阶段结果：立项/修复中可登记；首次登记后项目进入 IN_PROGRESS。
     */
    @Transactional
    public RestorationStage recordStage(Long projectId, StageRecordRequest req) {
        RestorationProject project = requireProject(projectId);
        if (project.getStatus() != ProjectStatus.PLANNED && project.getStatus() != ProjectStatus.IN_PROGRESS) {
            throw new IllegalStateException("项目当前状态为 " + project.getStatus() + "，不允许登记阶段结果");
        }
        Long count = stageMapper.selectCount(Wrappers.<RestorationStage>lambdaQuery()
                .eq(RestorationStage::getProjectId, projectId));
        RestorationStage stage = new RestorationStage();
        stage.setProjectId(projectId);
        stage.setStageOrder(count.intValue() + 1);
        stage.setStageName(req.getStageName());
        stage.setResult(req.getResult());
        stage.setRecordedAt(LocalDateTime.now());
        stageMapper.insert(stage);
        if (project.getStatus() == ProjectStatus.PLANNED) {
            transition(projectId, ProjectStatus.PLANNED, ProjectStatus.IN_PROGRESS, "开始修复");
        }
        return stage;
    }

    /**
     * 提交验收：须至少完成一个阶段（即项目已进入 IN_PROGRESS）。
     */
    @Transactional
    public RestorationProject submitAcceptance(Long projectId) {
        RestorationProject project = requireProject(projectId);
        transition(projectId, ProjectStatus.IN_PROGRESS, ProjectStatus.PENDING_ACCEPTANCE, "提交验收");
        return getById(projectId);
    }

    /**
     * 验收通过：项目闭环，文物回到在库。
     */
    @Transactional
    public RestorationProject accept(Long projectId) {
        RestorationProject project = requireProject(projectId);
        boolean updated = update(Wrappers.<RestorationProject>lambdaUpdate()
                .eq(RestorationProject::getId, projectId)
                .eq(RestorationProject::getStatus, ProjectStatus.PENDING_ACCEPTANCE)
                .set(RestorationProject::getStatus, ProjectStatus.ACCEPTED)
                .set(RestorationProject::getAcceptedAt, LocalDateTime.now())
                .set(RestorationProject::getUpdateTime, LocalDateTime.now()));
        if (!updated) {
            throw new IllegalStateException("项目当前状态不允许执行[验收]");
        }
        artifactService.markRestorationFinished(project.getArtifactId());
        return getById(projectId);
    }

    /**
     * 撤销项目：仅改状态为 REVOKED，记录保留；修复中的文物回到在库。
     * 已验收项目为闭环档案，不允许撤销。
     */
    @Transactional
    public RestorationProject revoke(Long projectId) {
        RestorationProject project = requireProject(projectId);
        if (project.getStatus() == ProjectStatus.ACCEPTED || project.getStatus() == ProjectStatus.REVOKED) {
            throw new IllegalStateException("项目当前状态为 " + project.getStatus() + "，不允许撤销");
        }
        boolean updated = update(Wrappers.<RestorationProject>lambdaUpdate()
                .eq(RestorationProject::getId, projectId)
                .in(RestorationProject::getStatus, ProjectStatus.PLANNED, ProjectStatus.IN_PROGRESS, ProjectStatus.PENDING_ACCEPTANCE)
                .set(RestorationProject::getStatus, ProjectStatus.REVOKED)
                .set(RestorationProject::getUpdateTime, LocalDateTime.now()));
        if (!updated) {
            throw new IllegalStateException("项目当前状态不允许撤销");
        }
        artifactService.markRestorationFinished(project.getArtifactId());
        return getById(projectId);
    }

    public RestorationProject findByCode(String projectCode) {
        return getOne(Wrappers.<RestorationProject>lambdaQuery()
                .eq(RestorationProject::getProjectCode, projectCode), false);
    }

    public RestorationProject requireProject(Long id) {
        RestorationProject project = getById(id);
        if (project == null) {
            throw new IllegalArgumentException("修复项目不存在: " + id);
        }
        return project;
    }

    public ProjectDetailResponse detail(Long id) {
        RestorationProject project = requireProject(id);
        List<RestorationStage> stages = stageMapper.selectList(Wrappers.<RestorationStage>lambdaQuery()
                .eq(RestorationStage::getProjectId, id)
                .orderByAsc(RestorationStage::getStageOrder));
        return new ProjectDetailResponse(project, stages);
    }

    public List<RestorationProject> listByArtifact(Long artifactId) {
        return list(Wrappers.<RestorationProject>lambdaQuery()
                .eq(RestorationProject::getArtifactId, artifactId)
                .orderByDesc(RestorationProject::getId));
    }

    private void transition(Long id, ProjectStatus from, ProjectStatus to, String action) {
        boolean updated = update(Wrappers.<RestorationProject>lambdaUpdate()
                .eq(RestorationProject::getId, id)
                .eq(RestorationProject::getStatus, from)
                .set(RestorationProject::getStatus, to)
                .set(RestorationProject::getUpdateTime, LocalDateTime.now()));
        if (!updated) {
            throw new IllegalStateException("项目当前状态不允许执行[" + action + "]");
        }
    }
}
