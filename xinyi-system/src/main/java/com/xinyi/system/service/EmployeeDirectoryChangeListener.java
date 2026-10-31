package com.xinyi.system.service;

/**
 * 员工目录变更监听 SPI（V6.4 §4）。框架受控写入口（新增/导入/改手机号/启停/删除）在事务提交后
 * 通知实现方（如 vi 员工自动关联域）按 GLOBAL_DIRECTORY 保护重查最新状态并失效旧工作能力。
 * 实现方不得反向写 sys_user；无实现 Bean 时框架行为不变（前向兼容）。
 */
public interface EmployeeDirectoryChangeListener {

    /** changeType: PHONE_CHANGED / STATUS_CHANGED / DELETED / IMPORTED / CREATED。 */
    void onDirectoryChanged(Long userId, String changeType);
}