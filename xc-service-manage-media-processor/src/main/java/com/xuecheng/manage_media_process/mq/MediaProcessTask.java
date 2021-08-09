package com.xuecheng.manage_media_process.mq;


import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class MediaProcessTask {

    // ffmpeg 绝对路径
    @Value("${xc-service-manage-media.ffmpeg-path}")
    private String ffmpeg_path;

    // 上传文件根目录
    @Value("${xc-service-manage-media.video-location}")
    private String serverPath;

    @Autowired
    private MediaFileRepository mediaFileRepository;

    /**
     * 此视频处理流程目前只处理 avi 视频
     * ---------------------------------------------------------------------------
     *  监听 MQ 中的消息进行处理
     * ---------------------------------------------------------------------------
     * - @RabbitListener()
     *     - queue = {}  配置消费者需要监听的队列,(可同时监听多个队列)
     *     - containerFactory ="factoryBean" 配置容器工厂参数，实现多线程处理消息。
     * ---------------------------------------------------------------------------
     * @param msg
     * @throws IOException
     */

    @RabbitListener(
            queues = {"${xc-service-manage-media.mq.queue-media-video-processor}"},
            containerFactory = "customContainerFactory"
    )
    public void receiveMediaProcessTask(String msg) throws IOException {
        Map msgMap = JSON.parseObject(msg, Map.class);
        log.info("receive nedia process task msg: {}", msgMap);
        // 解析消息 (消息传递以 json 格式)
        //-----------------------------------
        //  { "mediaId": 57834 }
        //-----------------------------------
        // 媒资文件 Id
        String mediaId = msgMap.get("mediaId").toString();
        // 获取媒资文件消息
        Optional<MediaFile> optionalMediaFile = mediaFileRepository.findById(mediaId);
        if(!optionalMediaFile.isPresent()){
            return;
        }
        MediaFile mediaFile = optionalMediaFile.get();
        // 获取媒资文件的类型
        String fileType = mediaFile.getFileType();
        // 此视频处理流程目前只处理 avi 视频
        if (fileType == null || !fileType.equals("avi")) {
            // 设置状态为无需处理
            mediaFile.setProcessStatus("303004");
            mediaFileRepository.save(mediaFile);
            return;
        }else{
            // 处理状态为未处理
            mediaFile.setProcessStatus("303001");
            mediaFileRepository.save(mediaFile);
        }
        //
        //-------------------------------------------------------------------
        // 1、 生成 mp4 文件
        //-------------------------------------------------------------------
        String video_path
                = serverPath + mediaFile.getFilePath() + mediaFile.getFileName();
        String mp4_name = mediaFile.getFileId() + ".mp4";
        // 转换好的视频文件存储在源视频文件的相同目录
        String mp4_folder_path = serverPath + mediaFile.getFilePath();
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(
                ffmpeg_path, video_path, mp4_name, mp4_folder_path
        );
        String result = mp4VideoUtil.generateMp4();
        if (null == result || !"success".equals(result)) {
            // 操作失败记录失败日志
            mediaFile.setProcessStatus("303003"); // 处理状态为失败
            MediaFileProcess_m3u8 m3u8 = new MediaFileProcess_m3u8();
            m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        //-------------------------------------------------------------------
        // 2、 将文件处理 m3u8
        //-------------------------------------------------------------------

        // 获取 Mp4 文件的地址
        String mp4_video_path = mp4_folder_path + mp4_name;
        String m3u8_name = mediaFile.getFileId() + ".m3u8";
        String m3u8_folder_path = mp4_folder_path + "hls/";
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(
                ffmpeg_path, mp4_video_path, m3u8_name, m3u8_folder_path
        );
        result = hlsVideoUtil.generateM3u8();
        if (null == result || !"success".equals(result)) {
            // 操作失败记录日志
            mediaFile.setProcessStatus("303003"); // 处理失败
            MediaFileProcess_m3u8 m3u8 = new MediaFileProcess_m3u8();
            m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        // 获取 m3u8 列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        // 更新处理状态为成功
        mediaFile.setProcessStatus("303002"); // 更新处理状态为成功
        MediaFileProcess_m3u8 m3u8 = new MediaFileProcess_m3u8();
        m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(m3u8);
        // 设置文件的 url 地址
        mediaFile.setFileUrl(mediaFile.getFilePath() + "hls/" + m3u8_name);
        // 更新存储的文件记录
        mediaFileRepository.save(mediaFile);
    }
}
