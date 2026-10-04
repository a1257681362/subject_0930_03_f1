package com.evops.museum.enums;

/**
 * 文物状态机：
 * IN_STOCK(在库) -> LENT_OUT(借出中) -> IN_STOCK
 * IN_STOCK(在库) -> RESTORING(修复中) -> IN_STOCK
 * 任意在办状态 -> REVOKED(已撤销)，撤销仅改状态不删记录。
 */
public enum ArtifactStatus {
    IN_STOCK,
    LENT_OUT,
    RESTORING,
    REVOKED
}
