package com.xuecheng.framework.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 此文件用于视频文件处理，步骤如下：
 * 1、生成mp4
 * 2、生成m3u8
 *
 */
public class HlsVideoUtil extends  VideoUtil {

    private String ffmpeg_path; //ffmpeg的安装位置
    private String video_path;
    private String m3u8_name;
    private String m3u8folder_path;
    public HlsVideoUtil(String ffmpeg_path, String video_path, String m3u8_name,String m3u8folder_path){
        super(ffmpeg_path);
        this.ffmpeg_path = ffmpeg_path;
        this.video_path = video_path;
        this.m3u8_name = m3u8_name;
        this.m3u8folder_path = m3u8folder_path;
    }

    private void clear_m3u8(String m3u8_path){
        //删除原来已经生成的m3u8及ts文件
        File m3u8dir = new File(m3u8_path);
        if (!m3u8dir.exists()) {
            m3u8dir.mkdirs();
        }
        //在hls目录方可删除，以免错误删除
        if (m3u8dir.exists() && m3u8_path.contains("/hls/")) {
            File[] files = m3u8dir.listFiles();
            //删除目录中的文件
            assert files != null;
            Stream.of(files).map(File::delete);
        }
    }

    /**
     * 生成m3u8文件
     * ----------------------------------------------------------------
     * @return 成功则返回success，失败返回控制台日志
     */
    public String generateM3u8(){
        String  m3u8FilePath = m3u8folder_path + m3u8_name;
        //清理m3u8文件目录
        clear_m3u8(m3u8folder_path);
        /*
        * ------------------------------------------------------------------------------------------------------------------
        * ffmpeg -i  lucene.mp4   -hls_time 10 -hls_list_size 0   -hls_segment_filename ./hls/lucene_%05d.ts ./hls/lucene.m3u8
        * ------------------------------------------------------------------------------------------------------------------
        */
        List<String> commend = new ArrayList<String>();
        commend.add(ffmpeg_path);
        commend.add("-i");
        commend.add(video_path);
        commend.add("-hls_time");
        commend.add("10");
        commend.add("-hls_list_size");
        commend.add("0");
        commend.add("-hls_segment_filename");
        String ts_file_name = m3u8folder_path +
                m3u8_name.substring(0,m3u8_name.lastIndexOf(".")) + "_%05d.ts";
        commend.add(ts_file_name);
        commend.add(m3u8FilePath);
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
        //通过查看视频时长判断是否成功
        Boolean check_video_time = check_video_time(video_path, m3u8FilePath);
        if(!check_video_time){
            return outstring;
        }
        //通过查看m3u8列表判断是否成功
        List<String> ts_list = get_ts_list();
        if(ts_list == null){
            return outstring;
        }
        return "success";
    }

    /**
     * 检查视频处理是否完成
     * @return ts列表
     */
    public List<String> get_ts_list() {
        List<String> fileList = new ArrayList<String>();
        List<String> tsList = new ArrayList<String>();
        String m3u8file_path = m3u8folder_path + m3u8_name;
        BufferedReader br = null;
        String str = null;
        String bottomline = "";
        try {
            br = new BufferedReader(new FileReader(m3u8file_path));
            while ((str = br.readLine()) != null) {
                bottomline = str;
                if(bottomline.endsWith(".ts")){
                    tsList.add(bottomline);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    br = null;
                }
            }
        }
        // m3u8 以固定字符结束
        if (bottomline.contains("#EXT-X-ENDLIST")) {
            fileList.addAll(tsList);
            return fileList;
        }
        return null;
    }


    public static void main(String[] args) throws IOException {
        String ffmpeg_path = "E:/ffmpeg/bin/ffmpeg.exe";//ffmpeg的安装位置
        String video_path = "D:/logs/temp/lucene.avi";
        String m3u8_name = "lucene.m3u8";
        String m3u8_path = "D:/logs/temp/lucene/chunks/";
        HlsVideoUtil videoUtil = new HlsVideoUtil(
                ffmpeg_path,video_path,m3u8_name,m3u8_path
        );
        String s = videoUtil.generateM3u8();
        System.out.println(s);
        System.out.println(videoUtil.get_ts_list());

    }
}
