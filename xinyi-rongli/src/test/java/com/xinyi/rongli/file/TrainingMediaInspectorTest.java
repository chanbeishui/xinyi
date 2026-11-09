package com.xinyi.rongli.file;

import static org.junit.jupiter.api.Assertions.*;

import com.xinyi.rongli.file.service.TrainingMediaInspector;
import com.xinyi.rongli.platform.error.ViErrorCode;
import com.xinyi.rongli.platform.error.ViException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/** 不依赖启动模块或媒体样本；验证不可信长度和计数会在进入第三方解析器前被有界拒绝。 */
class TrainingMediaInspectorTest {
    @TempDir Path directory;

    private final TrainingMediaInspector inspector = new TrainingMediaInspector();

    @Test
    void hugeExtendedBoxSizeInTinyFileIsRejectedWithoutOverflowOrAllocation() throws Exception {
        byte[] header =
                ByteBuffer.allocate(16)
                        .putInt(1)
                        .put(ascii("moov"))
                        .putLong(Long.MAX_VALUE)
                        .array();
        Path file = write("overflow.mp4", concat(brand(), header));
        invalid(file);
    }

    @Test
    void oversizedMovieMetadataIsRejectedBeforeReadingItsSparsePayload() throws Exception {
        Path file = directory.resolve("oversized-moov.mp4");
        long metadataSize = 8L * 1024 * 1024 + 1;
        try (RandomAccessFile bytes = new RandomAccessFile(file.toFile(), "rw")) {
            bytes.write(brand());
            bytes.write(
                    ByteBuffer.allocate(8).putInt((int) metadataSize).put(ascii("moov")).array());
            // 稀疏文件只提供真实长度；不能为测试本身分配攻击者声明的元数据内存。
            bytes.setLength(brand().length + metadataSize);
        }
        invalid(file);
    }

    @Test
    void excessiveContainerNestingIsRejectedBeforeRecursiveParserDescent() throws Exception {
        byte[] nested = new byte[0];
        for (int depth = 0; depth < 20; depth++) {
            nested = box("trak", nested);
        }
        Path file =
                write(
                        "nested.mp4",
                        concat(brand(), box("moov", nested), box("mdat", new byte[] {1})));
        invalid(file);
    }

    @Test
    void fabricatedSampleCountsWithoutMatchingTableBytesAreRejected() throws Exception {
        for (String type : new String[] {"stsz", "stts", "stco", "co64"}) {
            ByteBuffer payload = ByteBuffer.allocate(type.equals("stsz") ? 12 : 8);
            payload.putInt(0);
            if (type.equals("stsz")) {
                payload.putInt(0);
            }
            payload.putInt(Integer.MAX_VALUE);
            Path file =
                    write(
                            type + ".mp4",
                            concat(
                                    brand(),
                                    box("moov", box(type, payload.array())),
                                    box("mdat", new byte[] {1})));
            invalid(file);
        }
    }

    private void invalid(Path file) throws Exception {
        long length = Files.size(file);
        assertTimeoutPreemptively(
                Duration.ofSeconds(2),
                () -> {
                    ViException failure =
                            assertThrows(ViException.class, () -> inspector.inspect(file, length));
                    assertEquals(ViErrorCode.VI_DATA_INVALID, failure.getErrorCode());
                });
    }

    private Path write(String name, byte[] bytes) throws Exception {
        return Files.write(directory.resolve(name), bytes);
    }

    private byte[] brand() {
        return box("ftyp", concat(ascii("isom"), new byte[4], ascii("isom")));
    }

    private byte[] box(String type, byte[] payload) {
        return ByteBuffer.allocate(8 + payload.length)
                .putInt(8 + payload.length)
                .put(ascii(type))
                .put(payload)
                .array();
    }

    private byte[] concat(byte[]... values) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        for (byte[] value : values) {
            result.writeBytes(value);
        }
        return result.toByteArray();
    }

    private byte[] ascii(String value) {
        return value.getBytes(StandardCharsets.ISO_8859_1);
    }
}
