package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MediaUploadService {

    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${xc-service-manage-media.upload-location}")
    private String uploadLocation;

    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    private String routingkey_media_video;

    /**
     * 文件在服务器上存储时的存储规则 :: 按照文件的Md5值创建目录
     * ------------------------------------------------
     * 一级目录: Md5 值的第一位
     * 二级目录: Md5 值的第二位
     * 三级目录: Md5 值
     * 存储文件名: Md5值.扩展名
     * ------------------------------------------------
     *
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */

    // 文件在服务器上注册
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {

        // 根据文件存储规则获取文件夹和文件文件对象，判断是否存在
        // 先拿到文件的父级目录
        String fileDir  = getFileDir(fileMd5);
        // 获取文件最终应该存在的位置
        String filePath = getFilePath(fileMd5,fileExt);

        // 1、检查文件在磁盘上是否存在，
        File file = new File(filePath);
        // 文件信息是否已经在 mongoDB 中存在 (mongoDB 中存储文件信息时的id就是文件的Md5)
        Optional<MediaFile> optionalMediaFile = mediaFileRepository.findById(fileMd5);
        if (file.exists() && optionalMediaFile.isPresent()) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        // 文件不存在时需要创建一个文件夹
        File fileFolder = new File(fileDir);
        if(!fileFolder.exists()){
            boolean maked = fileFolder.mkdirs();
            if(!maked){
                ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_FAIL);
            }
        };
        return new ResponseResult(CommonCode.SUCCESS);
    }


    /**
     * 块文件是临时文件目录，会存储在三级目录的 chunks 临时文件夹中
     * ----------------------------------------------------------------
     *  检查分块文件是否存在
     * @param fileMd5
     * @param chunk
     * @param chunkSize
     * @return
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        // 获取分块文件存储的临时文件夹
        String chunkFileFolder = getChunkFileFolder(fileMd5);
        // 获取块文件
        File chunkFile = new File(chunkFileFolder + "/" + chunk);
        if(chunkFile.exists()){
            // 块文件已经存在
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, true);
        }else{
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, false);
        }
    }

    /**
     *  处理前端上传的块文件
     *  -------------------------------------------------------------
     *  以文件流的方式存储在分块文件临时文件夹内
     *
     * @param file
     * @param chunk
     * @param fileMd5
     * @return
     */
    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5) {
        // 检查分块目录是否存在，不存在自动创建
        String chunkFileFolderPath = this.getChunkFileFolder(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()){
            boolean result = chunkFileFolder.mkdirs();
            if(!result){
                ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_FAIL);
            }
        }
        // 获取到分块文件路径
        String chunkFilePath = chunkFileFolderPath + "/" + chunk;
        // 获取上传文件
        File chunkFile = new File(chunkFilePath);

        // 获取上传的文件流() try(resource) {}  自动关闭资源
        try(
            InputStream in = file.getInputStream();
            FileOutputStream out = new FileOutputStream(chunkFile);
        ){
            IOUtils.copy(in, out);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 合并所有的分块文件
     * ------------------------------------------------------------
     * 1、合并临时块文件夹中的所有的文件。
     * 2、将合并之后的文件按照一样的算法求取Md5,与传递的Md5进行比较
     * 3、校验通过, 将文件信息写入mongoDB 数据库
     * ------------------------------------------------------------
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    public ResponseResult mergechunk(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //1、 合并所有的文件分块
        // 获取到分块文件的所属目录
        String chunkFileFolderPath = getChunkFileFolder(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        File[] listFiles = chunkFileFolder.listFiles();

        // 获取到合并之后的文件对象
        File targetFile = new File(getFilePath(fileMd5,fileExt));

        // 执行文件的合并
        assert listFiles != null;
        File mergedFile = MergeFileChunks(Arrays.asList(listFiles),targetFile);
        if(null == mergedFile){
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }
        // 2、执行文件的 Md5 值检验
        if(!checkFileMd5(mergedFile,fileMd5)){
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        // 3、校验通过, 将文件信息写入到 mongoDB 中
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileName(fileMd5 + "." + fileExt);
        // 文件的原始文件名
        mediaFile.setFileOriginalName(fileName);
        // 文件保存在服务器上的相对路径
        mediaFile.setFilePath(getFileFolderRelativePath(fileMd5,fileExt));
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);

        // 向 MQ 发送消息处理视频
        sendProcessVideoMsg(fileMd5);

        // 成功返回
        return new ResponseResult(CommonCode.SUCCESS);
    }


    /**
     *  向 MQ 发送处理视频文件的消息
     * @param mediaId
     * @return
     */
    public ResponseResult sendProcessVideoMsg(String mediaId){
        Optional<MediaFile> optionalMediaFile = mediaFileRepository.findById(mediaId);
        // 判断媒资文件是否存在
        if (!optionalMediaFile.isPresent()) {
            return new ResponseResult(CommonCode.FAIL);
        }
        MediaFile mediaFile = optionalMediaFile.get();
        // 构建视频处理消息
        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("mediaId", mediaFile.getFileId());
        String msg = JSON.toJSONString(msgMap);
        // 发送视频处理消息
        try {
            this.rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EX_MEDIA_PROCESSTASK,
                    routingkey_media_video,
                    msg
            );
        }catch (Exception e){
            e.printStackTrace();
            log.error("send media process task error, msg is {}",msg);
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }


    /**
     *  获取文件存储的相对路径
     *  --------------------------------------------------------------
     * @param fileMd5
     * @param fileExt
     * @return
     */
    private String getFileFolderRelativePath(String fileMd5,String fileExt) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" ;
    }
    /**
     * 获取 分块文件存储的目录
     *  --------------------------------------------------------------
     * @param fileMd5
     * @return
     */
    private String getChunkFileFolder(String fileMd5) {
        if (StringUtil.isNullOrEmpty(fileMd5)){
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGiSTER_FAIL);
        }
        return this.getFileDir(fileMd5) + "/chunks";
    }

    /**
     * 获取 文件的路径
     * ---------------------------------------------------------------
     * @param fileMd5
     * @param fileExt
     * @return
     */
    private String getFilePath(String fileMd5, String fileExt) {
        if (StringUtil.isNullOrEmpty(fileMd5)){
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGiSTER_FAIL);
        }
        if(StringUtil.isNullOrEmpty(fileExt)){
            return this.getFileDir(fileMd5) + "/" + fileMd5;
        }
        return this.getFileDir(fileMd5) + "/" + fileMd5 + "." + fileExt;
    }

    /**
     * 获取文件所在的文件夹
     * @param fileMd5
     * @return
     */
    private String getFileDir(String fileMd5) {
        if (StringUtil.isNullOrEmpty(fileMd5)){
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGiSTER_FAIL);
        }
        return uploadLocation + "/" + fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5;
    }

    /**
     * 合并文件分块，并返回合并之后的完整文件
     * ---------------------------------------------------------
     * @param chunks
     * @param targetFile
     * @return
     */
    private File MergeFileChunks(List<File> chunks,File targetFile) {
        if(targetFile.exists()){
            //targetFile.delete();
            return targetFile;
        }
        // 对文件进行排序
        List<File> collect = chunks
                .stream()
                .sorted(Comparator.comparingInt(a -> Integer.parseInt(a.getName())))
                .collect(Collectors.toList());
        try {
            // 创建一个新文件，
            if(!targetFile.createNewFile()) {
                ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_FAIL);
            }
            // 将文件按照排序进行合并
            RandomAccessFile ref_write = new RandomAccessFile(targetFile, "rw");
            byte[] buf = new byte[1024];
            for (File e : collect) {
                RandomAccessFile ref_read = new RandomAccessFile(e, "r");
                int len = -1;
                while ((len = ref_read.read(buf)) != -1) {
                    ref_write.write(buf, 0, len);
                }
                ref_read.close();
            }
            ref_write.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return targetFile;
    }
    /**
     * 校验文件的 Md5 与预设值是否相等
     * ---------------------------------------------------------------
     * @param mergedFile
     * @param fileMd5
     * @return
     */
    private boolean checkFileMd5(File mergedFile, String fileMd5) {
        boolean result = true;
        if(mergedFile == null || StringUtil.isNullOrEmpty(fileMd5)){
            return false;
        }
        // 计算合并之后的文件的Md5 值
        try(FileInputStream fin = new FileInputStream(mergedFile)) {
            String Md5Hex = DigestUtils.md5Hex(fin);
            // 与传入的文件Md5 进行比较，
            if(!fileMd5.equalsIgnoreCase(Md5Hex)){
                result = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("checkMd5 error: file is {}, Md5 is {}",
                mergedFile.getAbsoluteFile(),
                fileMd5
            );
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        return result;
    }
}
