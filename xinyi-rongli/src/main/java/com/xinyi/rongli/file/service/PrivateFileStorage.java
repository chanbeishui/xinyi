package com.xinyi.rongli.file.service;

import com.xinyi.common.config.RuoYiConfig;
import com.xinyi.rongli.platform.error.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 私有磁盘适配器。目录由部署环境明确提供，不能复用公开 profile，也不接受客户端路径。 */
@Component
public class PrivateFileStorage {
    public static final long MAX_SIZE = 10 * 1024 * 1024;
    private final String configuredRoot;

    public PrivateFileStorage(@Value("${vi.files.root:${VI_PRIVATE_FILE_ROOT:}}") String root) {
        configuredRoot = root;
    }

    public Path root() {
        try {
            if (configuredRoot.isBlank()) throw new IOException();
            Path path = Path.of(configuredRoot);
            if (!path.isAbsolute()) throw new IOException();
            path = path.normalize();
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(
                            path,
                            java.nio.file.attribute.PosixFilePermissions.asFileAttribute(
                                    java.nio.file.attribute.PosixFilePermissions.fromString(
                                            "rwx------")));
                } catch (UnsupportedOperationException unsupported) {
                    Files.createDirectories(path);
                }
            }
            Path real = path.toRealPath();
            String profile = RuoYiConfig.getProfile();
            if (profile != null && !profile.isBlank()) {
                Path publicPath = Path.of(profile).toAbsolutePath().normalize();
                if (Files.exists(publicPath)) publicPath = publicPath.toRealPath();
                if (real.startsWith(publicPath) || publicPath.startsWith(real))
                    throw new IOException();
            }
            if (!real.equals(path)) throw new IOException();
            return real;
        } catch (IOException | InvalidPathException error) {
            throw new ViException(ViErrorCode.VI_CAPABILITY_NOT_CONFIGURED, "请配置独立、非公开的私有文件目录");
        }
    }

    private Path object(String key) {
        if (key == null || !key.matches("[a-f0-9]{32}"))
            throw new ViException(ViErrorCode.VI_DATA_INVALID);
        Path file = root().resolve(key);
        if (Files.isSymbolicLink(file)) throw new ViException(ViErrorCode.VI_DATA_INVALID);
        return file;
    }

    public void store(String key, InputStream input, long expectedSize) {
        Path target = object(key);
        Path temp = target.resolveSibling(".part-" + UUID.randomUUID());
        try {
            long total = 0;
            try {
                Files.createFile(
                        temp,
                        java.nio.file.attribute.PosixFilePermissions.asFileAttribute(
                                java.nio.file.attribute.PosixFilePermissions.fromString(
                                        "rw-------")));
            } catch (UnsupportedOperationException unsupported) {
                Files.createFile(temp);
            }
            try (OutputStream out = Files.newOutputStream(temp, StandardOpenOption.WRITE)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    if (total > MAX_SIZE || total > expectedSize)
                        throw new ViException(ViErrorCode.VI_DATA_INVALID, "文件大小与声明不符");
                    out.write(buffer, 0, count);
                }
            }
            if (total != expectedSize)
                throw new ViException(ViErrorCode.VI_DATA_INVALID, "文件大小与声明不符");
            // 只有一个数据库上传租约能写该对象；目标已存在时拒绝，防止覆盖已核验凭证。
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS))
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT);
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException error) {
            throw new ViException(ViErrorCode.VI_UPSTREAM_UNAVAILABLE, "私有文件写入失败，请重新申请上传");
        } finally {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
                /* 临时残片不提供读取入口。 */
            }
        }
    }

    public record Verified(String mime, long size, String sha256) {}

    public Verified verify(String key, long size, String mime) {
        byte[] bytes = read(key);
        if (bytes.length != size) throw new ViException(ViErrorCode.VI_DATA_INVALID, "实际文件大小不匹配");
        try (var stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new IOException();
            var reader = readers.next();
            try {
                reader.setInput(stream, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                String actual =
                        switch (format) {
                            case "png" -> "image/png";
                            case "jpeg", "jpg" -> "image/jpeg";
                            default -> "";
                        };
                // 先检查尺寸再解码，拒绝解压炸弹；声明 MIME 不能让脚本或其他格式被当作图片。
                if (!actual.equals(mime)
                        || reader.getWidth(0) <= 0
                        || reader.getHeight(0) <= 0
                        || (long) reader.getWidth(0) * reader.getHeight(0) > 25000000)
                    throw new IOException();
                if (reader.read(0) == null) throw new IOException();
                return new Verified(
                        actual,
                        bytes.length,
                        HexFormat.of()
                                .formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
            } finally {
                reader.dispose();
            }
        } catch (IOException | NoSuchAlgorithmException | RuntimeException error) {
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "文件不是有效的 JPEG 或 PNG 图片");
        }
    }

    public byte[] read(String key) {
        Path path = object(key);
        try {
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                throw new ViException(ViErrorCode.VI_STATE_CONFLICT, "尚未收到完整文件");
            if (Files.size(path) > MAX_SIZE) throw new ViException(ViErrorCode.VI_DATA_INVALID);
            try (InputStream in = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) {
                byte[] bytes = in.readNBytes((int) MAX_SIZE + 1);
                if (bytes.length > MAX_SIZE) throw new ViException(ViErrorCode.VI_DATA_INVALID);
                return bytes;
            }
        } catch (IOException error) {
            throw new ViException(ViErrorCode.VI_UPSTREAM_UNAVAILABLE);
        }
    }
}
