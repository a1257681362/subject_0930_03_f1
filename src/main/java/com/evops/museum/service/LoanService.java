package com.evops.museum.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evops.museum.dto.LoanCreateRequest;
import com.evops.museum.entity.Artifact;
import com.evops.museum.entity.LoanRecord;
import com.evops.museum.enums.ArtifactStatus;
import com.evops.museum.enums.LoanStatus;
import com.evops.museum.mapper.LoanRecordMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 借出/归还服务：借出 -> 归还形成闭环，撤销仅改状态不删记录。
 */
@Service
public class LoanService extends ServiceImpl<LoanRecordMapper, LoanRecord> {

    private final ArtifactService artifactService;
    private final BizNoGenerator noGenerator;

    public LoanService(ArtifactService artifactService, BizNoGenerator noGenerator) {
        this.artifactService = artifactService;
        this.noGenerator = noGenerator;
    }

    /**
     * 借出：仅在库文物可借；借出后文物状态置为 LENT_OUT。
     */
    @Transactional
    public LoanRecord lend(LoanCreateRequest req) {
        Artifact artifact = artifactService.requireActive(req.getArtifactId());
        if (artifact.getStatus() != ArtifactStatus.IN_STOCK) {
            throw new IllegalStateException("仅在库文物可借出，当前状态: " + artifact.getStatus());
        }
        LoanRecord loan = new LoanRecord();
        loan.setArtifactId(artifact.getId());
        loan.setBorrower(req.getBorrower());
        loan.setPurpose(req.getPurpose());
        loan.setLentAt(LocalDateTime.now());
        loan.setExpectedReturnAt(req.getExpectedReturnAt());
        loan.setStatus(LoanStatus.LENT);
        loan.setRemark(req.getRemark());
        for (int attempt = 0; attempt < 3; attempt++) {
            loan.setLoanNo(noGenerator.next("LOAN"));
            try {
                save(loan);
                break;
            } catch (DuplicateKeyException e) {
                if (attempt == 2) {
                    throw e;
                }
            }
        }
        artifactService.markLentOut(artifact.getId());
        return loan;
    }

    /**
     * 归还：借出单 LENT -> RETURNED，文物回到在库。
     */
    @Transactional
    public LoanRecord returnBack(Long id) {
        LoanRecord loan = requireLent(id);
        boolean updated = update(Wrappers.<LoanRecord>lambdaUpdate()
                .eq(LoanRecord::getId, loan.getId())
                .eq(LoanRecord::getStatus, LoanStatus.LENT)
                .set(LoanRecord::getStatus, LoanStatus.RETURNED)
                .set(LoanRecord::getReturnedAt, LocalDateTime.now())
                .set(LoanRecord::getUpdateTime, LocalDateTime.now()));
        if (!updated) {
            throw new IllegalStateException("借出单当前状态不允许归还");
        }
        artifactService.markReturned(loan.getArtifactId());
        return getById(id);
    }

    /**
     * 撤销借出：仅改状态为 REVOKED，记录保留；文物回到在库。
     */
    @Transactional
    public LoanRecord revoke(Long id) {
        LoanRecord loan = requireLent(id);
        boolean updated = update(Wrappers.<LoanRecord>lambdaUpdate()
                .eq(LoanRecord::getId, loan.getId())
                .eq(LoanRecord::getStatus, LoanStatus.LENT)
                .set(LoanRecord::getStatus, LoanStatus.REVOKED)
                .set(LoanRecord::getUpdateTime, LocalDateTime.now()));
        if (!updated) {
            throw new IllegalStateException("借出单当前状态不允许撤销");
        }
        artifactService.markReturned(loan.getArtifactId());
        return getById(id);
    }

    public LoanRecord requireLent(Long id) {
        LoanRecord loan = getById(id);
        if (loan == null) {
            throw new IllegalArgumentException("借出单不存在: " + id);
        }
        if (loan.getStatus() != LoanStatus.LENT) {
            throw new IllegalStateException("借出单当前状态为 " + loan.getStatus() + "，仅已借出状态可操作");
        }
        return loan;
    }

    public List<LoanRecord> listByArtifact(Long artifactId) {
        return list(Wrappers.<LoanRecord>lambdaQuery()
                .eq(LoanRecord::getArtifactId, artifactId)
                .orderByDesc(LoanRecord::getId));
    }
}
