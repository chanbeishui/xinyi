package com.xinyi.rongli.file.service;

import com.xinyi.rongli.platform.error.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.boxes.sampleentry.*;
import org.mp4parser.boxes.iso14496.part14.ESDescriptorBox;

/** MP4容器与轨道检查，不宣称逐帧解码或认定教材内容合规；大媒体内容从不送入解析器堆内存。 */
public class TrainingMediaInspector {
    private static final long MAX_METADATA = 8L * 1024 * 1024;
    private static final Set<String> CONTAINERS =
            Set.of("moov", "trak", "mdia", "minf", "stbl", "edts", "dinf", "mvex", "moof", "traf");

    public record Metadata(
            long durationMillis, int width, int height, String videoCodec, String audioCodec) {}

    private record Bounds(long start, long size, int header, String type) {
        long end() {
            return start + size;
        }
    }

    public Metadata inspect(Path path, long expectedSize) {
        try (FileChannel channel =
                FileChannel.open(path, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS)) {
            if (channel.size() != expectedSize) fail();
            long cursor = 0;
            Bounds movie = null;
            boolean brand = false, media = false;
            int count = 0;
            while (cursor < expectedSize) {
                if (++count > 10000) fail();
                Bounds box = box(channel, cursor, expectedSize);
                if ("ftyp".equals(box.type())) {
                    if (brand || box.size() < 16 || box.size() > 4096) fail();
                    ByteBuffer value =
                            read(
                                    channel,
                                    box.start() + box.header(),
                                    (int) (box.size() - box.header()));
                    String major = ascii(value, 0);
                    brand =
                            Set.of("isom", "iso2", "iso5", "iso6", "mp41", "mp42", "avc1", "dash")
                                    .contains(major);
                    if (!brand) fail();
                }
                if ("moov".equals(box.type())) {
                    if (movie != null || box.size() > MAX_METADATA) fail();
                    movie = box;
                }
                if ("mdat".equals(box.type()) && box.size() > box.header()) media = true;
                if ("moof".equals(box.type())) {
                    if (box.size() > MAX_METADATA) fail();
                    validate(
                            read(channel, box.start(), (int) box.size()),
                            0,
                            (int) box.size(),
                            0,
                            new int[] {0});
                }
                cursor = box.end();
            }
            if (!brand || !media || movie == null) fail();
            byte[] metadata = read(channel, movie.start(), (int) movie.size()).array();
            validate(
                    ByteBuffer.wrap(metadata).order(ByteOrder.BIG_ENDIAN),
                    0,
                    metadata.length,
                    0,
                    new int[] {0});
            // 仅有界moov进入库；不解析mdat，避免库复制大视频到临时文件或整文件入堆。
            try (IsoFile iso =
                    new IsoFile(Channels.newChannel(new ByteArrayInputStream(metadata)))) {
                MovieBox moov = iso.getMovieBox();
                if (moov == null || moov.getTrackCount() < 1 || moov.getTrackCount() > 8) fail();
                long duration =
                        millis(
                                moov.getMovieHeaderBox().getDuration(),
                                moov.getMovieHeaderBox().getTimescale());
                int width = 0, height = 0;
                String audio = null, video = null;
                for (TrackBox track : moov.getBoxes(TrackBox.class)) {
                    String handler = track.getMediaBox().getHandlerBox().getHandlerType();
                    if ("vide".equals(handler)) {
                        if (video != null) fail();
                        var entry =
                                track.getSampleTableBox()
                                        .getSampleDescriptionBox()
                                        .getSampleEntry();
                        if (!(entry instanceof VisualSampleEntry visual)
                                || !Set.of("avc1", "avc3").contains(entry.getType())) fail();
                        VisualSampleEntry visual = (VisualSampleEntry) entry;
                        width = visual.getWidth();
                        height = visual.getHeight();
                        if (width < 1 || height < 1 || (long) width * height > 25000000) fail();
                        if (visual.getBoxes().stream().noneMatch(b -> "avcC".equals(b.getType())))
                            fail();
                        video = entry.getType();
                        MediaHeaderBox mh = track.getMediaBox().getMediaHeaderBox();
                        if (mh.getDuration() > 0)
                            duration =
                                    Math.min(duration, millis(mh.getDuration(), mh.getTimescale()));
                    } else if ("soun".equals(handler)) {
                        if (audio != null) fail();
                        var entry =
                                track.getSampleTableBox()
                                        .getSampleDescriptionBox()
                                        .getSampleEntry();
                        if (!(entry instanceof AudioSampleEntry) || !"mp4a".equals(entry.getType()))
                            fail();
                        AudioSampleEntry sound = (AudioSampleEntry) entry;
                        if (sound.getChannelCount() < 1
                                || sound.getChannelCount() > 8
                                || sound.getSampleRate() <= 0
                                || sound.getSampleRate() > 192000) fail();
                        var descriptors = sound.getBoxes(ESDescriptorBox.class);
                        if (descriptors.size() != 1) fail();
                        var descriptor = descriptors.get(0).getEsDescriptor();
                        if (descriptor == null || descriptor.getDecoderConfigDescriptor() == null)
                            fail();
                        var decoder = descriptor.getDecoderConfigDescriptor();
                        if (decoder.getObjectTypeIndication() != 0x40
                                || decoder.getAudioSpecificInfo() == null
                                || decoder.getAudioSpecificInfo().getAudioObjectType() != 2) fail();
                        audio = entry.getType();
                    }
                }
                if (video == null) fail();
                return new Metadata(duration, width, height, video, audio);
            }
        } catch (IOException | RuntimeException error) {
            if (error instanceof ViException known) throw known;
            throw new ViException(ViErrorCode.VI_DATA_INVALID, "视频不是受支持的完整MP4容器或H264/AAC轨道");
        }
    }

    private static long millis(long duration, long scale) {
        if (duration <= 0 || scale <= 0 || duration / (double) scale > 86400) fail();
        long value = (long) (duration * 1000.0 / scale);
        if (value <= 0 || value > 86400000) fail();
        return value;
    }

    private static Bounds box(FileChannel channel, long start, long end) throws IOException {
        if (end - start < 8) fail();
        ByteBuffer head = read(channel, start, (int) Math.min(16, end - start));
        long size = Integer.toUnsignedLong(head.getInt(0));
        String type = ascii(head, 4);
        int header = 8;
        if (size == 1) {
            if (head.limit() < 16) fail();
            size = head.getLong(8);
            header = 16;
        }
        if (size == 0) size = end - start;
        if (size < header || size > end - start) fail();
        return new Bounds(start, size, header, type);
    }

    private static ByteBuffer read(FileChannel channel, long position, int length)
            throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(length).order(ByteOrder.BIG_ENDIAN);
        while (buffer.hasRemaining()) {
            int n = channel.read(buffer, position + buffer.position());
            if (n < 0) fail();
            if (n == 0) throw new EOFException();
        }
        buffer.flip();
        return buffer;
    }

    private static String ascii(ByteBuffer buffer, int at) {
        byte[] name = new byte[4];
        for (int i = 0; i < 4; i++) name[i] = buffer.get(at + i);
        return new String(name, StandardCharsets.ISO_8859_1);
    }

    private static void validate(ByteBuffer bytes, int start, int end, int depth, int[] count) {
        if (depth > 16) fail();
        int cursor = start;
        while (cursor < end) {
            if (++count[0] > 10000 || end - cursor < 8) fail();
            long length = Integer.toUnsignedLong(bytes.getInt(cursor));
            String type = ascii(bytes, cursor + 4);
            int header = 8;
            if (length == 1) {
                if (end - cursor < 16) fail();
                length = bytes.getLong(cursor + 8);
                header = 16;
            }
            if (length < header || length > end - cursor) fail();
            int limit = cursor + (int) length, payload = cursor + header;
            if (CONTAINERS.contains(type)) validate(bytes, payload, limit, depth + 1, count);
            else if ("stsd".equals(type) || "dref".equals(type)) {
                if (limit - payload < 8) fail();
                long entries = Integer.toUnsignedLong(bytes.getInt(payload + 4));
                if (entries > 8) fail();
                validate(bytes, payload + 8, limit, depth + 1, count);
            } else if (Set.of("avc1", "avc3").contains(type)) {
                if (limit - payload < 78) fail();
                validate(bytes, payload + 78, limit, depth + 1, count);
            } else if ("mp4a".equals(type)) {
                if (limit - payload < 28) fail();
                int soundVersion = Short.toUnsignedInt(bytes.getShort(payload + 8));
                if (soundVersion != 0) fail();
                validate(bytes, payload + 28, limit, depth + 1, count);
            } else if ("avcC".equals(type)) validateAvc(bytes, payload, limit);
            else if ("esds".equals(type)) {
                if (limit - payload < 7 || limit - payload > 65536) fail();
                validateDescriptors(bytes, payload + 4, limit, 0, new int[] {0});
            } else if ("url ".equals(type)) {
                if (limit - payload < 4 || (bytes.getInt(payload) & 1) != 1) fail();
            } else if ("urn ".equals(type) || Set.of("encv", "enca").contains(type)) fail();
            else if ("stsz".equals(type)) {
                if (limit - payload < 12) fail();
                long amount = Integer.toUnsignedLong(bytes.getInt(payload + 8));
                if (amount > 1000000
                        || bytes.getInt(payload + 4) == 0 && amount * 4 > limit - payload - 12)
                    fail();
            } else if (Set.of("stts", "ctts", "stsc", "stco", "co64", "stss").contains(type)) {
                if (limit - payload < 8) fail();
                long entries = Integer.toUnsignedLong(bytes.getInt(payload + 4));
                int unit =
                        switch (type) {
                            case "stsc" -> 12;
                            case "stco", "stss" -> 4;
                            default -> 8;
                        };
                if (entries > 1000000 || entries * unit > limit - payload - 8) fail();
            }
            cursor = limit;
        }
        if (cursor != end) fail();
    }

    /** 解析库处理esds前先核对变长描述符边界，防止伪长度触发与实际box不符的大数组分配。 */
    private static void validateDescriptors(
            ByteBuffer data, int start, int end, int depth, int[] count) {
        if (depth > 8) fail();
        int cursor = start;
        while (cursor < end) {
            if (++count[0] > 100 || end - cursor < 2) fail();
            int tag = Byte.toUnsignedInt(data.get(cursor++));
            int length = 0, parts = 0, part;
            do {
                if (cursor >= end || ++parts > 4) fail();
                part = Byte.toUnsignedInt(data.get(cursor++));
                length = (length << 7) | (part & 127);
            } while ((part & 128) != 0);
            if (length > end - cursor) fail();
            int limit = cursor + length, child = limit;
            if (tag == 3) {
                if (limit - cursor < 3) fail();
                int flags = Byte.toUnsignedInt(data.get(cursor + 2));
                child = cursor + 3;
                if ((flags & 128) != 0) child += 2;
                if ((flags & 64) != 0) fail(); // 拒绝外部URL依赖，教材只能来自已验证原件。
                if ((flags & 32) != 0) child += 2;
                if (child > limit) fail();
            } else if (tag == 4) {
                if (limit - cursor < 13) fail();
                child = cursor + 13;
            }
            if (child < limit) validateDescriptors(data, child, limit, depth + 1, count);
            cursor = limit;
        }
    }

    private static void validateAvc(ByteBuffer data, int start, int end) {
        if (end - start < 7 || data.get(start) != 1) fail();
        int sps = data.get(start + 5) & 31, cursor = start + 6;
        if (sps < 1) fail();
        for (int i = 0; i < sps; i++) {
            if (end - cursor < 2) fail();
            int size = Short.toUnsignedInt(data.getShort(cursor));
            cursor += 2;
            if (size < 1 || size > end - cursor) fail();
            cursor += size;
        }
        if (cursor >= end) fail();
        int pps = Byte.toUnsignedInt(data.get(cursor++));
        if (pps < 1) fail();
        for (int i = 0; i < pps; i++) {
            if (end - cursor < 2) fail();
            int size = Short.toUnsignedInt(data.getShort(cursor));
            cursor += 2;
            if (size < 1 || size > end - cursor) fail();
            cursor += size;
        }
    }

    private static void fail() {
        throw new ViException(ViErrorCode.VI_DATA_INVALID, "MP4结构、编码或时长无效");
    }
}
