package com.xinyi.rongli.driver.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.common.utils.SecurityUtils;
import com.xinyi.rongli.driver.dto.DriverDtos;
import com.xinyi.rongli.notification.service.MessageService;
import com.xinyi.rongli.driver.service.PenaltyService;
import com.xinyi.rongli.driver.service.TrainingService;
import com.xinyi.rongli.aftersale.service.AfterSaleAdminService;
import com.xinyi.rongli.fulfill.service.FulfillAdminService;

/**
 * 管理端 driver 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminTrainingController
{
    @Autowired
    private TrainingService trainingService;

    @GetMapping("/training-courses")
    @PreAuthorize("@ss.hasPermi('biz:trainingcourse:list')")
    public Map<String, Object> courses(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize)
    {
        return trainingService.adminCourses(status, pageNum, pageSize);
    }
    @PostMapping("/training-courses")
    @PreAuthorize("@ss.hasPermi('biz:trainingcourse:create')")
    public Map<String, Object> createCourse(@RequestBody DriverDtos.TrainingCourseReq req)
    {
        return trainingService.saveCourse(null, req, SecurityUtils.getUserId());
    }
    @GetMapping("/training-courses/{trainingId}")
    @PreAuthorize("@ss.hasPermi('biz:trainingcourse:query')")
    public Map<String, Object> course(@PathVariable Long trainingId)
    {
        return trainingService.adminCourse(trainingId);
    }
    @PutMapping("/training-courses/{trainingId}")
    @PreAuthorize("@ss.hasPermi('biz:trainingcourse:edit')")
    public Map<String, Object> updateCourse(@PathVariable Long trainingId,
            @RequestBody DriverDtos.TrainingCourseReq req)
    {
        return trainingService.saveCourse(trainingId, req, SecurityUtils.getUserId());
    }
    @PostMapping("/training-courses/{trainingId}/publish")
    @PreAuthorize("@ss.hasPermi('biz:trainingcourse:publish')")
    public Map<String, Object> publishCourse(@PathVariable Long trainingId)
    {
        return trainingService.publishCourse(trainingId, SecurityUtils.getUserId());
    }
}
