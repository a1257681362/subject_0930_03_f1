package com.evops.museum.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evops.museum.dto.ArtifactCreateRequest;
import com.evops.museum.entity.Artifact;
import com.evops.museum.enums.ArtifactStatus;
import com.evops.museum.mapper.ArtifactMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文物档案服务。
 * 幂等约定：artifactCode 为幂等键，重复提交返回原档案，绝不产生第二条记录。
 */
@Service
public class ArtifactService extends ServiceImpl<ArtifactMapper, Artifact> {

    @Transactional
    public IdempotentCreateResult<Artifact> createArchive(ArtifactCreateRequest req) {
        Artifact existing = findByCode(req.getArtifactCode());
        if (existing != null) {
            return IdempotentCreateResult.replayed(existing);
        }
        Artifact artifact = new Artifact();
        artifact.setArtifactCode(req.getArtifactCode());
        artifact.setName(req.getName());
        artifact.setMaterial(req.getMaterial());
        artifact.setEra(req.getEra());
        artifact.setSource(req.getSource());
        artifact.setPreservationLevel(req.getPreservationLevel());
        artifact.setStatus(ArtifactStatus.IN_STOCK);
        artifact.setRemark(req.getRemark());
        try {
            save(artifact);
        } catch (DuplicateKeyException e) {
            // 并发下唯一约束兜底：返回已存在的记录，保证重复请求不产生第二条
            return IdempotentCreateResult.replayed(findByCode(req.getArtifactCode()));
        }
        return IdempotentCreateResult.created(artifact);
    }

    public Artifact findByCode(String artifactCode) {
        return getOne(Wrappers.<Artifact>lambdaQuery()
                .eq(Artifact::getArtifactCode, artifactCode), false);
    }

    public List<Artifact> listByStatus(ArtifactStatus status) {
        return list(Wrappers.<Artifact>lambdaQuery()
                .eq(status != null, Artifact::getStatus, status)
                .orderByDesc(Artifact::getId));
    }

    /**
     * 取存在且未撤销的文物，供各链路操作前校验。
     */
    public Artifact requireActive(Long id) {
        Artifact artifact = getById(id);
        if (artifact == null) {
            throw new IllegalArgumentException("文物不存在: " + id);
        }
        if (artifact.getStatus() == ArtifactStatus.REVOKED) {
            throw new IllegalStateException("文物已撤销，禁止业务操作: " + id);
        }
        return artifact;
    }

    /**
     * 撤销档案：仅将状态置为 REVOKED，记录保留可查。需在库状态方可撤销。
     */
    @Transactional
    public Artifact revoke(Long id) {
        Artifact artifact = requireActive(id);
        if (artifact.getStatus() != ArtifactStatus.IN_STOCK) {
            throw new IllegalStateException("文物当前状态为 " + artifact.getStatus() + "，仅在库状态可撤销");
        }
        transition(id, ArtifactStatus.IN_STOCK, ArtifactStatus.REVOKED, "撤销");
        return getById(id);
    }

    /** 借出链路：在库 -> 借出中 */
    public void markLentOut(Long artifactId) {
        transition(artifactId, ArtifactStatus.IN_STOCK, ArtifactStatus.LENT_OUT, "借出");
    }

    /** 归还链路：借出中 -> 在库 */
    public void markReturned(Long artifactId) {
        transition(artifactId, ArtifactStatus.LENT_OUT, ArtifactStatus.IN_STOCK, "归还");
    }

    /** 修复链路：在库 -> 修复中 */
    public void markRestoring(Long artifactId) {
        transition(artifactId, ArtifactStatus.IN_STOCK, ArtifactStatus.RESTORING, "立项修复");
    }

    /** 验收/撤销链路：修复中 -> 在库 */
    public void markRestorationFinished(Long artifactId) {
        transition(artifactId, ArtifactStatus.RESTORING, ArtifactStatus.IN_STOCK, "修复完结");
    }

    /**
     * 条件更新状态，源状态不匹配即失败，防止并发下链路状态错乱。
     */
    private void transition(Long id, ArtifactStatus from, ArtifactStatus to, String action) {
        boolean updated = update(Wrappers.<Artifact>lambdaUpdate()
                .eq(Artifact::getId, id)
                .eq(Artifact::getStatus, from)
                .set(Artifact::getStatus, to)
                .set(Artifact::getUpdateTime, LocalDateTime.now()));
        if (!updated) {
            throw new IllegalStateException("文物当前状态不允许执行[" + action + "]");
        }
    }
}
