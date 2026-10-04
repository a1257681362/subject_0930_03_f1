package com.evops.museum.enums;

/**
 * 修复项目状态机：
 * PLANNED(已立项) -> IN_PROGRESS(修复中) -> PENDING_ACCEPTANCE(待验收) -> ACCEPTED(已验收)
 * 验收前任意状态 -> REVOKED(已撤销)，撤销仅改状态不删记录。
 */
public enum ProjectStatus {
    PLANNED,
    IN_PROGRESS,
    PENDING_ACCEPTANCE,
    ACCEPTED,
    REVOKED
}
