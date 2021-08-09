package com.xuecheng.framework.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2018/3/6.
 */
public class Mp4VideoUtil extends VideoUtil {

    private String ffmpeg_path;
    private String video_path;
    private String mp4_name;
    private String mp4folder_path;
    public Mp4VideoUtil(String ffmpeg_path, String video_path, String mp4_name, String mp4folder_path){
        super(ffmpeg_path);
        this.ffmpeg_path = ffmpeg_path;
        this.video_path = video_path;
        this.mp4_name = mp4_name;
        this.mp4folder_path = mp4folder_path;
    }
    /**
     * 视频编码，生成mp4文件
     * @return 成功返回success，失败返回控制台日志
     */
    public String generateMp4(){
        // 获取 文件的绝对路径
        String absFilePath = mp4folder_path + mp4_name;
        // 清除已生成的mp4
        clear_mp4(absFilePath);
        /*
         * ----------------------------------------------------------------------------------------------------------
         * ffmpeg.exe -i  lucene.avi -c:v libx264 -s 1280x720 -pix_fmt yuv420p -b:a 63k -b:v 753k -r 18 .\lucene.mp4
         * ----------------------------------------------------------------------------------------------------------
         */
        List<String> commend = new ArrayList<String>();
        commend.add(ffmpeg_path);
        commend.add("-i");
        commend.add(video_path);
        commend.add("-c:v");
        commend.add("libx264");
        commend.add("-y");//覆盖输出文件
        commend.add("-s");
        commend.add("1280x720");
        commend.add("-pix_fmt");
        commend.add("yuv420p");
        commend.add("-b:a");
        commend.add("63k");
        commend.add("-b:v");
        commend.add("753k");
        commend.add("-r");
        commend.add("18");
        commend.add(absFilePath);
        String outstring = null;
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(commend);
            //将标准输入流和错误输入流合并，通过标准输入流程读取信息
            builder.redirectErrorStream(true);
            Process p = builder.start();
            outstring = waitFor(p);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Boolean check_video_time = this.check_video_time(video_path, absFilePath);
        if(!check_video_time){
            return outstring;
        }else{
            return "success";
        }
    }
    //清除已生成的mp4
    private void clear_mp4(String mp4_path){
        //删除原来已经生成的m3u8及ts文件
        File mp4file = new File(mp4_path);
        if(mp4file.exists() && mp4file.isFile()){
            mp4file.delete();
        }
    }
    // 测试调用 ffmpeg 来转换文件格式
    public static void main(String[] args) throws IOException {
        String ffmpeg_path = "E:/ffmpeg/bin/ffmpeg.exe";//ffmpeg的安装位置
        String video_path = "D:/logs/temp/lucene.avi";
        String mp4_name = "lucene.mp4";
        String mp4_path = "D:/logs/temp/lucene/";
        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4_path);
        String s = videoUtil.generateMp4();
        System.out.println(s);
    }
}
