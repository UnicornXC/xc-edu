package com.xuecheng.mange_media;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 测试文件分块合并
 */
public class TestFileChunkSliceOrMerge {

    @Test
    public void testFileSlice() throws IOException {

        // 源文件
        File sourceFile = new File("D:/logs/temp/lucene.avi");
        // 块文件的目录
        String chunksPath = "D:/logs/temp/chunks/";
        File chunkFolder = new File(chunksPath);
        if (!chunkFolder.exists()){
            chunkFolder.mkdir();
        }
        // 先定义块文件的大小
        long chunkFileSize = 1 * 1024 * 1024;

        // 文件的块数
        long chunkFileNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkFileSize);
        if (chunkFileNum <= 0){
            chunkFileNum = 1;
        }
        // 创建读取文件的对象
        RandomAccessFile ref_read = new RandomAccessFile(sourceFile, "r");

        // 写入文件的缓冲区
        byte[] buf = new byte[1024];

        for (long i = 0; i < chunkFileNum; i++) {
            // 在块文件夹中创建一个块文件
            File chunkFile = new File(chunksPath + "dv" + + i);
            boolean created = chunkFile.createNewFile();
            if(created){
                RandomAccessFile ref_write = new RandomAccessFile(chunkFile, "rw");
                int len = -1;
                while ((len = ref_read.read(buf)) != -1){
                    ref_write.write(buf, 0, len);
                    // 如果块文件的大小达到 1M 开始写下一块
                    if(chunkFile.length() >= chunkFileSize){
                        break;
                    }
                }
                ref_write.close();
            }
        }
        ref_read.close();
    }

    // 测试分块文件的合并
    @Test
    public void testFileChunkMerge() throws IOException {
        // 块文件的目录
        String folderPath = "D:/logs/temp/chunks";
        // 块文件对象
        File chunkFileFolder = new File(folderPath);
        //
        if(!chunkFileFolder.exists()){
            System.out.println("文件不存在");
            return;
        }
        // 合并文件的对象
        File mergeFile = new File("D:/logs/temp/merge_lucene.avi");
        boolean newFile = mergeFile.createNewFile();

        // 拿到文件夹中的分块文件列表
        File[] files = chunkFileFolder.listFiles();

        // 创建一个写文件的对象
        RandomAccessFile ref_write = new RandomAccessFile(mergeFile, "rw");

        assert files != null;
        List<String> sortFiles = Stream.of(files)
                .map(File::getName)
                .sorted(Comparator.comparingInt(a -> Integer.parseInt(a.substring(2))))
                .collect(Collectors.toList());

        byte[] buf = new byte[1024];
        sortFiles.forEach(e->{
            // System.out.println(chunkFileFolder + "\\" + e);
            File f = new File(chunkFileFolder + "\\" + e);
            try {
                RandomAccessFile ref_read = new RandomAccessFile(f, "r");
                int len = -1;
                while ((len=ref_read.read(buf))!=-1){
                    ref_write.write(buf, 0, len);
                }
                ref_read.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        ref_write.close();
    }
}
