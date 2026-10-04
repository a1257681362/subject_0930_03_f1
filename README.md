# EVOPS 基础工作区

这是 springboot的骨架项目。

技术栈：Java 8、Spring Boot 2.7、MyBatis-Plus、H2 本地文件数据库、Shiro、Thymeleaf。

启动前准备：

1. 执行 `mvn -q -DskipTests compile` 验证骨架构建。
2. 执行 `mvn spring-boot:run` 启动应用；Spring Boot 会自动执行 `src/main/resources/schema.sql`。
3. H2 数据文件默认写入工作区 `data/evops`。如需修改路径，编辑 `application.yml` 中的 `jdbc:h2:file:` 连接串。

根目录的 `..\..\docs\schema\evops.sql` 是交付副本，应与工作区 `src/main/resources/schema.sql` 保持一致。

## 博物馆文物修复台账（museum 模块）

代码位于 `com.evops.museum`，表结构见 `schema.sql` 的 `museum-1` 段。票务模块暂不实现。

**核心规则**

- 文物编号 `artifact_code`、项目编号 `project_code` 数据库唯一约束；建档/立项接口按编号幂等，重复提交返回原记录（响应 `data.created=false`），不会产生第二条结果。
- 所有撤销仅将状态置为 `REVOKED`，记录保留可查，不做物理删除。
- 链路闭环：借出（在库→借出中）→ 归还（→在库）→ 修复立项（→修复中）→ 阶段结果 → 提交验收 → 验收（→在库）。状态迁移均为条件更新，非法流转直接拒绝。

**接口一览**

| 模块 | 接口 |
| --- | --- |
| 文物档案 | `POST /api/museum/artifacts`（幂等建档）、`GET /api/museum/artifacts/{id}`、`GET /api/museum/artifacts?status=`、`POST /api/museum/artifacts/{id}/revoke` |
| 入藏登记 | `POST /api/museum/acquisitions`（照片/尺寸/库位）、`GET /api/museum/acquisitions?artifactId=`、`POST /api/museum/acquisitions/{id}/revoke` |
| 借出归还 | `POST /api/museum/loans`、`POST /api/museum/loans/{id}/return`、`POST /api/museum/loans/{id}/revoke` |
| 修复项目 | `POST /api/museum/restoration-projects`（幂等立项）、`POST .../{id}/stages`、`POST .../{id}/submit-acceptance`、`POST .../{id}/accept`、`POST .../{id}/revoke`、`GET .../{id}`（含阶段结果） |

**状态机**

- 文物：`IN_STOCK → LENT_OUT → IN_STOCK`；`IN_STOCK → RESTORING → IN_STOCK`；在库可 `REVOKED`
- 借出单：`LENT → RETURNED / REVOKED`
- 修复项目：`PLANNED → IN_PROGRESS → PENDING_ACCEPTANCE → ACCEPTED`；验收前可 `REVOKED`

集成测试 `MuseumLedgerFlowTest` 覆盖：重复请求幂等（两次提交仅一条记录）、完整链路、撤销保留记录、非法流转拦截。运行：`mvn test -Dtest=MuseumLedgerFlowTest`。


