package com.xinyi.web.controller.common;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.xinyi.common.core.domain.AjaxResult;
import com.xinyi.common.utils.StringUtils;
import com.xinyi.common.utils.file.FileUploadUtils;
import com.xinyi.common.utils.file.FileUtils;
import com.xinyi.common.utils.file.OssFileService;

/**
 * 通用请求处理
 *
 * <p>上传与下载都走对象存储，服务器本地磁盘不落地。</p>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/common")
public class CommonController
{
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    private static final String FILE_DELIMITER = ",";

    /**
     * 通用下载请求
     *
     * @param fileName 对象 key 或完整地址
     * @param delete 是否删除
     */
    @GetMapping("/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request)
    {
        try
        {
            // fileName 现在是对象地址（可能是完整 URL），校验扩展名只看最后一段
            if (!FileUtils.checkAllowDownload(FileUtils.getName(fileName)))
            {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + "_" + fileName.substring(fileName.lastIndexOf('/') + 1);

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(fileName, response.getOutputStream());
            if (delete)
            {
                FileUtils.deleteFile(fileName);
            }
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求（单个）
     *
     * @param biz 上传用途，决定对象前缀：rich-text / image / file，缺省落到 upload/
     */
    @PostMapping("/upload")
    public AjaxResult uploadFile(MultipartFile file, String biz) throws Exception
    {
        try
        {
            // 上传到对象存储，返回可直接访问的完整地址
            String url = FileUploadUtils.upload(OssFileService.dirForBiz(biz), file);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", url);
            ajax.put("newFileName", FileUtils.getName(url));
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 通用上传请求（多个）
     *
     * @param biz 上传用途，同单个上传
     */
    @PostMapping("/uploads")
    public AjaxResult uploadFiles(List<MultipartFile> files, String biz) throws Exception
    {
        try
        {
            List<String> urls = new ArrayList<String>();
            List<String> fileNames = new ArrayList<String>();
            List<String> newFileNames = new ArrayList<String>();
            List<String> originalFilenames = new ArrayList<String>();
            String dir = OssFileService.dirForBiz(biz);
            for (MultipartFile file : files)
            {
                String url = FileUploadUtils.upload(dir, file);
                urls.add(url);
                fileNames.add(url);
                newFileNames.add(FileUtils.getName(url));
                originalFilenames.add(file.getOriginalFilename());
            }
            AjaxResult ajax = AjaxResult.success();
            ajax.put("urls", StringUtils.join(urls, FILE_DELIMITER));
            ajax.put("fileNames", StringUtils.join(fileNames, FILE_DELIMITER));
            ajax.put("newFileNames", StringUtils.join(newFileNames, FILE_DELIMITER));
            ajax.put("originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMITER));
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 资源通用下载
     *
     * @param resource 对象 key 或完整地址
     */
    @GetMapping("/download/resource")
    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
        try
        {
            if (!FileUtils.checkAllowDownload(FileUtils.getName(resource)))
            {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", resource));
            }
            String downloadName = StringUtils.substringAfterLast(resource, "/");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(resource, response.getOutputStream());
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }
}
