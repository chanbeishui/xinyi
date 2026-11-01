package com.xinyi.rongli.address.service;

import com.xinyi.rongli.address.domain.ViAddress;
import com.xinyi.rongli.address.dto.AddressView;
import com.xinyi.rongli.address.dto.GeoPoint;
import com.xinyi.rongli.address.mapper.ViAddressMapper;
import com.xinyi.rongli.core.ViId;
import com.xinyi.rongli.identity.mapper.ViAccountMapper;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;
import com.xinyi.rongli.platform.json.ViJson;
import com.xinyi.rongli.platform.service.AuditService;
import com.xinyi.rongli.platform.service.OutboxService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 车主地址簿（A02）。新增/更新/删除/列表/设常用；归属本人（越权 404 不泄露）；version CAS；
 * 设常用以账号行锁串行+清旧默认（排除目标）保证并发不出现多个默认；地址/位置加密存储；
 * 临时地址不强制先保存（报价可携带草稿，本服务仅管理已保存地址）。审计/outbox 同事务。
 */
@Service
public class AddressService {

    private final ViAddressMapper addressMapper;
    private final ViAccountMapper accountMapper;
    private final AddressCrypto crypto;
    private final AuditService auditService;
    private final OutboxService outboxService;
    private final TransactionTemplate tx;

    public AddressService(ViAddressMapper addressMapper, ViAccountMapper accountMapper, AddressCrypto crypto,
                          AuditService auditService, OutboxService outboxService, PlatformTransactionManager txManager) {
        this.addressMapper = addressMapper;
        this.accountMapper = accountMapper;
        this.crypto = crypto;
        this.auditService = auditService;
        this.outboxService = outboxService;
        this.tx = new TransactionTemplate(txManager);
        this.tx.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    public List<AddressView> list(long accountId) {
        List<AddressView> out = new ArrayList<>();
        for (ViAddress a : addressMapper.selectByOwner(accountId)) {
            out.add(toView(a));
        }
        return out;
    }

    public AddressView create(long accountId, String label, String address, GeoPoint location, boolean isDefault) {
        validate(address, location);
        return tx.execute(s -> {
            lockOwner(accountId);
            ViAddress a = new ViAddress();
            a.setOwnerAccountId(accountId);
            a.setLabel(label);
            a.setAddressCipher(crypto.encrypt(address));
            a.setLocationCipher(crypto.encrypt(ViJson.write(location)));
            a.setIsDefault(isDefault ? 1 : 0);
            addressMapper.insert(a);
            if (isDefault) {
                addressMapper.clearDefault(accountId, a.getId());
            }
            audit(accountId, a.getId(), "ADDRESS_CREATE");
            return toView(addressMapper.selectById(a.getId()));
        });
    }

    public AddressView update(long accountId, long id, String label, String address, GeoPoint location,
                              Boolean isDefault, long expectedVersion) {
        validate(address, location);
        return tx.execute(s -> {
            lockOwner(accountId);
            ViAddress row = owned(accountId, id);
            int newDefault = isDefault == null ? row.getIsDefault() : (isDefault ? 1 : 0);
            if (newDefault == 1) {
                addressMapper.clearDefault(accountId, id);
            }
            int updated = addressMapper.update(id, accountId, label, crypto.encrypt(address),
                    crypto.encrypt(ViJson.write(location)), newDefault, expectedVersion);
            if (updated != 1) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "地址已变化，请刷新后重试");
            }
            audit(accountId, id, "ADDRESS_UPDATE");
            return toView(addressMapper.selectById(id));
        });
    }

    public void delete(long accountId, long id, long expectedVersion, String reason) {
        tx.execute(s -> {
            lockOwner(accountId);
            owned(accountId, id);
            int updated = addressMapper.softDelete(id, accountId, Instant.now(), expectedVersion);
            if (updated != 1) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "地址已变化，请刷新后重试");
            }
            auditService.recordSuccess(Actor.mini(accountId, "OWNER", null, null, null, null),
                    "VI_ADDRESS", id, "ADDRESS_DELETE", null, Map.of("reason", reason), null);
            outboxService.publish("address-delete:" + id, "VI_ADDRESS", id, "ADDRESS_DELETED", Map.of());
            return null;
        });
    }

    public AddressView setDefault(long accountId, long id, long expectedVersion, String reason) {
        return tx.execute(s -> {
            lockOwner(accountId);
            owned(accountId, id);
            addressMapper.clearDefault(accountId, id);
            int updated = addressMapper.setDefaultFlag(id, accountId, 1, expectedVersion);
            if (updated != 1) {
                throw new ViException(ViErrorCode.VI_VERSION_CONFLICT, "地址已变化，请刷新后重试");
            }
            audit(accountId, id, "ADDRESS_SET_DEFAULT");
            return toView(addressMapper.selectById(id));
        });
    }

    private void lockOwner(long accountId) {
        if (accountMapper.selectByIdForUpdate(accountId) == null) {
            throw new ViException(ViErrorCode.VI_AUTH_REQUIRED);
        }
    }

    private ViAddress owned(long accountId, long id) {
        ViAddress row = addressMapper.selectByIdForUpdate(id);
        if (row == null || row.getOwnerAccountId() != accountId || row.getDeletedAt() != null) {
            throw new ViException(ViErrorCode.VI_RESOURCE_NOT_VISIBLE);
        }
        return row;
    }

    private void validate(String address, GeoPoint location) {
        if (address == null || address.isBlank() || address.length() > 255) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "地址文本必填且不超过 255 字");
        }
        if (location == null || location.lng() < -180 || location.lng() > 180
                || location.lat() < -90 || location.lat() > 90 || !"GCJ02".equals(location.coordinateSystem())) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "位置须为合法 GCJ02 坐标");
        }
    }

    private void audit(long accountId, long id, String action) {
        auditService.recordSuccess(Actor.mini(accountId, "OWNER", null, null, null, null),
                "VI_ADDRESS", id, action, null, Map.of(), null);
        outboxService.publish("address:" + action.toLowerCase() + ":" + id, "VI_ADDRESS", id, action, Map.of());
    }

    private AddressView toView(ViAddress a) {
        GeoPoint loc = ViJson.read(crypto.decrypt(a.getLocationCipher()), GeoPoint.class);
        return new AddressView(ViId.external(a.getId()), a.getLabel(), crypto.decrypt(a.getAddressCipher()),
                loc, a.getIsDefault() != null && a.getIsDefault() == 1, a.getVersion());
    }
}