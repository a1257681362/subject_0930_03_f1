package com.evops.museum.enums;

/**
 * 借出状态机：LENT(已借出) -> RETURNED(已归还)；LENT -> REVOKED(已撤销)。
 */
public enum LoanStatus {
    LENT,
    RETURNED,
    REVOKED
}
