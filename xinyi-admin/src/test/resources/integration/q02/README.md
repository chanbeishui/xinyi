# Q02 私有视频测试样本

`flower.mp4` 来自 Mozilla MDN 的公开 CC0 示例视频：
https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4

- 下载日期：2026-11-09。
- SHA-256：`0cd83d944a6ca7822b4a8306cecc60a36e859b041f6702c6a1ad9ead78924451`。
- 文件大小：1,128,375 字节。
- 本机 AVFoundation 实际读取：视频为 H.264 / `avc1`，960 × 540，约 29.97 fps；
  视频轨道约 5.005 秒，容器约 5.011 秒。

集成测试从原始文件字节经真实 HTTP 上传、私有磁盘核验后得到 READY 文件；不直接伪造 READY。
此样本仅验证媒体传输与课程配置，不是运营教材，也不代表完成 Q03 学习、考试或正式资格培训。
