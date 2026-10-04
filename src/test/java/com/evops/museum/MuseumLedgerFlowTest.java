package com.evops.museum;

import com.evops.museum.dto.AcquisitionCreateRequest;
import com.evops.museum.dto.ArtifactCreateRequest;
import com.evops.museum.dto.LoanCreateRequest;
import com.evops.museum.dto.ProjectCreateRequest;
import com.evops.museum.dto.StageRecordRequest;
import com.evops.museum.entity.Acquisition;
import com.evops.museum.entity.Artifact;
import com.evops.museum.entity.LoanRecord;
import com.evops.museum.entity.RestorationProject;
import com.evops.museum.entity.RestorationStage;
import com.evops.museum.enums.AcquisitionStatus;
import com.evops.museum.enums.ArtifactStatus;
import com.evops.museum.enums.LoanStatus;
import com.evops.museum.enums.ProjectStatus;
import com.evops.museum.service.AcquisitionService;
import com.evops.museum.service.ArtifactService;
import com.evops.museum.service.IdempotentCreateResult;
import com.evops.museum.service.LoanService;
import com.evops.museum.service.RestorationProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 博物馆文物修复台账集成测试：
 * 1. 建档/立项幂等 —— 重复请求不产生第二条记录；
 * 2. 借出 -> 归还 -> 修复 -> 验收 完整链路；
 * 3. 撤销仅改状态，记录保留。
 */
@SpringBootTest
@Transactional
class MuseumLedgerFlowTest {

    @Autowired
    private ArtifactService artifactService;
    @Autowired
    private AcquisitionService acquisitionService;
    @Autowired
    private LoanService loanService;
    @Autowired
    private RestorationProjectService projectService;

    private ArtifactCreateRequest sampleArtifact(String code) {
        ArtifactCreateRequest req = new ArtifactCreateRequest();
        req.setArtifactCode(code);
        req.setName("博物馆文物修复档案起步");
        req.setMaterial("青铜");
        req.setEra("西汉");
        req.setSource("田野考古发掘");
        req.setPreservationLevel("一级");
        req.setRemark("有效样本");
        return req;
    }

    @Test
    void duplicateArtifactRequestCreatesOnlyOneRecord() {
        // 先提交有效样本，再原样重复请求，两次返回不能产生第二条结果
        IdempotentCreateResult<Artifact> first = artifactService.createArchive(sampleArtifact("WW-2026-0001"));
        assertTrue(first.isCreated());
        assertNotNull(first.getRecord().getId());

        IdempotentCreateResult<Artifact> second = artifactService.createArchive(sampleArtifact("WW-2026-0001"));
        assertFalse(second.isCreated());
        assertEquals(first.getRecord().getId(), second.getRecord().getId());

        assertEquals(1, artifactService.listByStatus(null).stream()
                .filter(a -> "WW-2026-0001".equals(a.getArtifactCode())).count());
    }

    @Test
    void duplicateProjectRequestCreatesOnlyOneRecord() {
        Artifact artifact = artifactService.createArchive(sampleArtifact("WW-2026-0002")).getRecord();

        ProjectCreateRequest req = new ProjectCreateRequest();
        req.setProjectCode("PRJ-2026-0001");
        req.setArtifactId(artifact.getId());
        req.setClientName("省文物保护中心");
        req.setPlan("除锈、缓蚀、封护三步走");

        IdempotentCreateResult<RestorationProject> first = projectService.createProject(req);
        IdempotentCreateResult<RestorationProject> second = projectService.createProject(req);
        assertTrue(first.isCreated());
        assertFalse(second.isCreated());
        assertEquals(first.getRecord().getId(), second.getRecord().getId());

        assertEquals(1, projectService.listByArtifact(artifact.getId()).size());
        // 重复请求不得重复扭转文物状态
        assertEquals(ArtifactStatus.RESTORING, artifactService.getById(artifact.getId()).getStatus());
    }

    @Test
    void fullChainLoanReturnRestoreAccept() {
        // 建档 -> 入藏登记
        Artifact artifact = artifactService.createArchive(sampleArtifact("WW-2026-0003")).getRecord();
        assertEquals(ArtifactStatus.IN_STOCK, artifact.getStatus());

        AcquisitionCreateRequest acqReq = new AcquisitionCreateRequest();
        acqReq.setArtifactId(artifact.getId());
        acqReq.setPhotoUrls(Arrays.asList("/photos/front.jpg", "/photos/back.jpg"));
        acqReq.setDimensions("通高32.5cm，口径15.2cm");
        acqReq.setStorageLocation("一号库房-A区-03柜");
        Acquisition acquisition = acquisitionService.register(acqReq);
        assertEquals(AcquisitionStatus.REGISTERED, acquisition.getStatus());
        assertEquals("/photos/front.jpg,/photos/back.jpg", acquisition.getPhotos());

        // 借出 -> 归还
        LoanCreateRequest loanReq = new LoanCreateRequest();
        loanReq.setArtifactId(artifact.getId());
        loanReq.setBorrower("市博物馆展览部");
        loanReq.setPurpose("临时展览");
        LoanRecord loan = loanService.lend(loanReq);
        assertEquals(LoanStatus.LENT, loan.getStatus());
        assertEquals(ArtifactStatus.LENT_OUT, artifactService.getById(artifact.getId()).getStatus());

        LoanRecord returned = loanService.returnBack(loan.getId());
        assertEquals(LoanStatus.RETURNED, returned.getStatus());
        assertNotNull(returned.getReturnedAt());
        assertEquals(ArtifactStatus.IN_STOCK, artifactService.getById(artifact.getId()).getStatus());

        // 修复立项 -> 阶段结果 -> 提交验收 -> 验收
        ProjectCreateRequest projectReq = new ProjectCreateRequest();
        projectReq.setProjectCode("PRJ-2026-0002");
        projectReq.setArtifactId(artifact.getId());
        projectReq.setClientName("省文物保护中心");
        projectReq.setPlan("清洗-矫形-补配-封护");
        RestorationProject project = projectService.createProject(projectReq).getRecord();
        assertEquals(ProjectStatus.PLANNED, project.getStatus());
        assertEquals(ArtifactStatus.RESTORING, artifactService.getById(artifact.getId()).getStatus());

        StageRecordRequest stageReq = new StageRecordRequest();
        stageReq.setStageName("清洗除锈");
        stageReq.setResult("表面锈蚀已清除，无新增损伤");
        RestorationStage stage = projectService.recordStage(project.getId(), stageReq);
        assertEquals(1, stage.getStageOrder());
        assertEquals(ProjectStatus.IN_PROGRESS, projectService.getById(project.getId()).getStatus());

        RestorationProject pending = projectService.submitAcceptance(project.getId());
        assertEquals(ProjectStatus.PENDING_ACCEPTANCE, pending.getStatus());

        RestorationProject accepted = projectService.accept(project.getId());
        assertEquals(ProjectStatus.ACCEPTED, accepted.getStatus());
        assertNotNull(accepted.getAcceptedAt());
        assertEquals(ArtifactStatus.IN_STOCK, artifactService.getById(artifact.getId()).getStatus());

        // 项目详情含阶段结果
        assertEquals(1, projectService.detail(project.getId()).getStages().size());
    }

    @Test
    void revokeKeepsRecordAndOnlyChangesStatus() {
        Artifact artifact = artifactService.createArchive(sampleArtifact("WW-2026-0004")).getRecord();

        AcquisitionCreateRequest acqReq = new AcquisitionCreateRequest();
        acqReq.setArtifactId(artifact.getId());
        acqReq.setDimensions("残高18cm");
        acqReq.setStorageLocation("二号库房-B区-01柜");
        Acquisition acquisition = acquisitionService.register(acqReq);

        Acquisition revokedAcq = acquisitionService.revoke(acquisition.getId());
        assertEquals(AcquisitionStatus.REVOKED, revokedAcq.getStatus());
        // 撤销不抹除记录
        assertNotNull(acquisitionService.getById(acquisition.getId()));

        Artifact revokedArtifact = artifactService.revoke(artifact.getId());
        assertEquals(ArtifactStatus.REVOKED, revokedArtifact.getStatus());
        assertNotNull(artifactService.getById(artifact.getId()));
    }

    @Test
    void illegalTransitionsAreRejected() {
        Artifact artifact = artifactService.createArchive(sampleArtifact("WW-2026-0005")).getRecord();

        // 修复中的文物不能借出
        ProjectCreateRequest projectReq = new ProjectCreateRequest();
        projectReq.setProjectCode("PRJ-2026-0003");
        projectReq.setArtifactId(artifact.getId());
        projectReq.setClientName("市文物修复所");
        RestorationProject project = projectService.createProject(projectReq).getRecord();

        LoanCreateRequest loanReq = new LoanCreateRequest();
        loanReq.setArtifactId(artifact.getId());
        loanReq.setBorrower("某展览公司");
        assertThrows(IllegalStateException.class, () -> loanService.lend(loanReq));

        // 未登记阶段结果不能提交验收
        assertThrows(IllegalStateException.class, () -> projectService.submitAcceptance(project.getId()));

        // 撤销项目后文物回到在库，项目记录保留
        RestorationProject revoked = projectService.revoke(project.getId());
        assertEquals(ProjectStatus.REVOKED, revoked.getStatus());
        assertEquals(ArtifactStatus.IN_STOCK, artifactService.getById(artifact.getId()).getStatus());
        assertNotNull(projectService.getById(project.getId()));
    }
}
