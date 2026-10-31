package com.xinyi.rongli.identity.mapper;

/**
 * 员工目录保护行（vi_employee_directory_guard，首期唯一 GLOBAL_DIRECTORY）。
 * 自动关联与目录写入统一在此低频互斥行 SELECT FOR UPDATE 串行化（V6.4 §4 锁序之首）。
 * 先确保唯一保护行存在，再加锁；微信外部调用绝不在此锁内。
 */
public interface EmployeeDirectoryGuardMapper {

    /** 幂等确保 GLOBAL_DIRECTORY 行存在。 */
    int ensureGuard();

    /** 取 GLOBAL_DIRECTORY 行并加排他锁（返回行 id）。须在事务内、作为锁序第一步。 */
    long lockGuard();
}
