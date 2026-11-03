package com.xinyi.rongli.controller.admin;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xinyi.common.annotation.Log;
import com.xinyi.common.enums.BusinessType;
import com.xinyi.rongli.service.PayCertService;

/**
 * 微信支付平台证书（回调验签用）。
 *
 * <p>对应支付配置页里"平台证书"那一栏：不再需要手工下载粘贴，用已保存的商户号 +
 * 证书序列号 + 商户私钥 + APIv3 密钥直接向微信拉取并解密。</p>
 */
@RestController
@RequestMapping("/api/admin/config/payment/platform-cert")
public class AdminPayCertController
{
    @Autowired
    private PayCertService payCertService;

    /** 只读预览：能看到证书清单即说明商户凭据与 APIv3 密钥可用 */
    @GetMapping
    @PreAuthorize("@ss.hasPermi('biz:config:payment')")
    public Map<String, Object> preview()
    {
        return payCertService.preview();
    }

    /**
     * 拉取并（可选）落库。
     *
     * <p>{@code persist=true} 时写入 platformCert 与 platformSerialNo，回调验签立即使用新证书。</p>
     */
    @PostMapping("/refresh")
    @PreAuthorize("@ss.hasPermi('biz:config:payment')")
    @Log(title = "微信支付平台证书", businessType = BusinessType.UPDATE)
    public Map<String, Object> refresh(@RequestBody(required = false) Map<String, Object> body)
    {
        boolean persist = body != null
                && (Boolean.TRUE.equals(body.get("persist")) || "true".equals(String.valueOf(body.get("persist"))));
        return payCertService.refresh(persist);
    }
}
