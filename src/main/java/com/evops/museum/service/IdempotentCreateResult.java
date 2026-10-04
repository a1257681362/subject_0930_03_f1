package com.evops.museum.service;

/**
 * 幂等创建结果：record 为最终落库（或已存在）的记录；
 * created=true 表示本次新建，false 表示命中已有记录的重复请求。
 * 重复请求返回同一条记录，保证两次调用不产生第二条结果。
 */
public class IdempotentCreateResult<T> {
    private final T record;
    private final boolean created;

    private IdempotentCreateResult(T record, boolean created) {
        this.record = record;
        this.created = created;
    }

    public static <T> IdempotentCreateResult<T> created(T record) {
        return new IdempotentCreateResult<>(record, true);
    }

    public static <T> IdempotentCreateResult<T> replayed(T record) {
        return new IdempotentCreateResult<>(record, false);
    }

    public T getRecord() { return record; }
    public boolean isCreated() { return created; }
}
