package com.evops.museum.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evops.museum.dto.AcquisitionCreateRequest;
import com.evops.museum.entity.Acquisition;
import com.evops.museum.entity.Artifact;
import com.evops.museum.enums.AcquisitionStatus;
import com.evops.museum.mapper.AcquisitionMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入藏登记服务：关联文物档案，记录照片、尺寸、库位；撤销仅改状态。
 */
@Service
public class AcquisitionService extends ServiceImpl<AcquisitionMapper, Acquisition> {

    private final ArtifactService artifactService;
    private final BizNoGenerator noGenerator;

    public AcquisitionService(ArtifactService artifactService, BizNoGenerator noGenerator) {
        this.artifactService = artifactService;
        this.noGenerator = noGenerator;
    }

    @Transactional
    public Acquisition register(AcquisitionCreateRequest req) {
        Artifact artifact = artifactService.requireActive(req.getArtifactId());
        Acquisition acquisition = new Acquisition();
        acquisition.setArtifactId(artifact.getId());
        acquisition.setPhotos(joinPhotos(req.getPhotoUrls()));
        acquisition.setDimensions(req.getDimensions());
        acquisition.setStorageLocation(req.getStorageLocation());
        acquisition.setAcquiredAt(req.getAcquiredAt() != null ? req.getAcquiredAt() : LocalDateTime.now());
        acquisition.setStatus(AcquisitionStatus.REGISTERED);
        acquisition.setRemark(req.getRemark());
        // 单号由系统生成，唯一约束兜底，冲突时换号重试
        for (int attempt = 0; attempt < 3; attempt++) {
            acquisition.setAcquisitionNo(noGenerator.next("ACQ"));
            try {
                save(acquisition);
                return acquisition;
            } catch (DuplicateKeyException e) {
                if (attempt == 2) {
                    throw e;
                }
            }
        }
        return acquisition;
    }

    /**
     * 撤销登记：仅将状态置为 REVOKED，记录保留。
     */
    @Transactional
    public Acquisition revoke(Long id) {
        Acquisition acquisition = requireRegistered(id);
        boolean updated = update(Wrappers.<Acquisition>lambdaUpdate()
                .eq(Acquisition::getId, acquisition.getId())
                .eq(Acquisition::getStatus, AcquisitionStatus.REGISTERED)
                .set(Acquisition::getStatus, AcquisitionStatus.REVOKED)
                .set(Acquisition::getUpdateTime, LocalDateTime.now()));
        if (!updated) {
            throw new IllegalStateException("登记单当前状态不允许撤销");
        }
        return getById(id);
    }

    public Acquisition requireRegistered(Long id) {
        Acquisition acquisition = getById(id);
        if (acquisition == null) {
            throw new IllegalArgumentException("入藏登记不存在: " + id);
        }
        if (acquisition.getStatus() != AcquisitionStatus.REGISTERED) {
            throw new IllegalStateException("入藏登记已撤销: " + id);
        }
        return acquisition;
    }

    public List<Acquisition> listByArtifact(Long artifactId) {
        return list(Wrappers.<Acquisition>lambdaQuery()
                .eq(Acquisition::getArtifactId, artifactId)
                .orderByDesc(Acquisition::getId));
    }

    private String joinPhotos(List<String> photoUrls) {
        if (photoUrls == null || photoUrls.isEmpty()) {
            return null;
        }
        return String.join(",", photoUrls);
    }
}
