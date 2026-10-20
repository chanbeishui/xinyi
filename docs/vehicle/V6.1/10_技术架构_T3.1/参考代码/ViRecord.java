// [R4] 参考草稿，非权威实现。唯一权威 ViRecord 见 14_工程模板/backend/src/main/java/com/xinyi/rongli/core/ViRecord.java（含 Long id）。
// 自然键实体(vi_driver_occupancy/vi_dispatch_cursor/vi_driver_location/vi_handover_attempt_guard)不套本基类，单独映射。
import java.time.Instant;
/** New vi entity base only. HTTP write/query DTOs must be separate classes. */
public abstract class ViRecord {
    private Instant createdAt;
    private Instant updatedAt;
    private long version;
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant value) { this.createdAt=value; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant value) { this.updatedAt=value; }
    public long getVersion() { return version; }
    public void setVersion(long value) { if(value<0) throw new IllegalArgumentException("version<0"); this.version=value; }
}
