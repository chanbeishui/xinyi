package com.xinyi.rongli.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xinyi.rongli.mapper.MiscMapper;
import com.xinyi.rongli.support.Json;
import com.xinyi.rongli.support.Row;

/**
 * outbox 可靠异步消费。
 *
 * <p>业务写入与 outbox 记录同事务落库，本消费者负责把事件投递到外部渠道（当前为 MOCK）。
 * 投递失败按指数退避重试，累计 5 次进入 FAILED 死信，由管理端观测后人工处理；
 * 投递成功仅标记 DONE，不回写业务，保证"通知失败不影响业务"。</p>
 */
@Service
public class OutboxConsumer
{
    private static final Logger log = LoggerFactory.getLogger(OutboxConsumer.class);

    @Autowired
    private MiscMapper miscMapper;

    @Autowired
    private SubscribeService subscribeService;

    /** 单批最多消费条数 */
    private static final int BATCH = 50;

    /**
     * 消费一轮到期事件，返回成功条数。
     */
    public int consume()
    {
        List<Map<String, Object>> due = miscMapper.listDueOutbox(BATCH);
        int done = 0;
        for (Map<String, Object> item : due)
        {
            Row row = Row.of(item);
            Long id = row.lng("id");
            try
            {
                deliver(row);
                miscMapper.markOutboxDone(id);
                done++;
            }
            catch (RuntimeException e)
            {
                log.warn("outbox 投递失败 id={} type={}", id, row.str("event_type"), e);
                miscMapper.markOutboxRetry(id);
            }
        }
        return done;
    }

    /**
     * 渠道投递。
     *
     * <p>SUBSCRIBE_PUSH 走微信订阅消息；其余事件为业务事实广播，当前只记录投递轨迹。
     * 任一渠道抛异常都会触发 outbox 退避重试。</p>
     */
    private void deliver(Row row)
    {
        String eventType = row.str("event_type");
        String aggregateType = row.str("aggregate_type");
        Long aggregateId = row.lng("aggregate_id");
        String payload = row.str("payload");

        if ("SUBSCRIBE_PUSH".equals(eventType))
        {
            SubscribeService.Result result = subscribeService.deliver(Json.read(payload));
            log.info("订阅推送 aggregate={}:{} 结果={} 说明={}", aggregateType, aggregateId,
                    result.channel(), result.reason());
            return;
        }

        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("eventType", eventType);
        trace.put("aggregateType", aggregateType);
        trace.put("aggregateId", aggregateId);
        trace.put("payload", payload);
        log.info("outbox 已投递 {}", trace);
    }

    /** 死信列表（管理端观测） */
    public List<Map<String, Object>> deadLetters(int pageNum, int pageSize)
    {
        return miscMapper.listOutbox("FAILED", (Math.max(pageNum, 1) - 1) * pageSize, pageSize);
    }
}
