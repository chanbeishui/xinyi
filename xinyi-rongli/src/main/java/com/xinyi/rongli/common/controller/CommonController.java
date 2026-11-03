package com.xinyi.rongli.common.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.xinyi.rongli.driver.dto.DriverDtos;
import com.xinyi.rongli.common.dto.MiniDtos;
import com.xinyi.rongli.auth.service.AuthService;
import com.xinyi.rongli.order.service.BookingService;
import com.xinyi.rongli.media.service.MediaService;
import com.xinyi.rongli.notification.service.MessageService;
import com.xinyi.system.service.ISysDictTypeService;
import com.xinyi.rongli.common.support.MiniAuthInterceptor;

/**
 * common 域接口。由原跨域控制器按端点路径拆分而来，URL 保持不变。
 */
@RestController
@RequestMapping("/api/common")
public class CommonController
{
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ISysDictTypeService dictTypeService;

    @GetMapping("/dict/{dictType}")
    public Map<String, Object> dict(@PathVariable String dictType)
    {
        List<Map<String, Object>> items = new ArrayList<>();
        var data = dictTypeService.selectDictDataByType(dictType);
        if (data != null)
        {
            data.forEach(d ->
            {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("label", d.getDictLabel());
                item.put("value", d.getDictValue());
                item.put("sort", d.getDictSort());
                item.put("cssClass", d.getListClass());
                items.add(item);
            });
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("dictType", dictType);
        view.put("total", items.size());
        view.put("items", items);
        return view;
    }
    @GetMapping("/stations")
    public List<Map<String, Object>> stations()
    {
        return bookingService.visibleStations();
    }
}
