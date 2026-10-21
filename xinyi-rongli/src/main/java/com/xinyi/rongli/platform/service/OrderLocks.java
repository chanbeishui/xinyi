package com.xinyi.rongli.platform.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * 有序锁工具（ADR-05/06）。多行 SELECT...FOR UPDATE 必须按业务定义的固定顺序加锁，
 * 否则并发交叉加锁会死锁。本工具给出规范化顺序：去重 + 升序。
 * 业务 Mapper 按此顺序逐行加锁；E06 只提供顺序契约，不代替业务执行加锁。
 */
public final class OrderLocks {

    private OrderLocks() {
    }

    /** 内部 Long 主键的规范加锁顺序：去重、升序、剔除 null。 */
    public static List<Long> orderedIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        TreeSet<Long> set = new TreeSet<>();
        for (Long id : ids) {
            if (id != null) {
                set.add(id);
            }
        }
        return new ArrayList<>(set);
    }

    /** 字符串业务键（如 source fee entry ID）的规范加锁顺序：去重、字典序升序。 */
    public static List<String> orderedKeys(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        TreeSet<String> set = new TreeSet<>();
        for (String k : keys) {
            if (k != null) {
                set.add(k);
            }
        }
        return new ArrayList<>(set);
    }
}
