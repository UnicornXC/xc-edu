package com.xuecheng.test.freemarker;

import com.xuecheng.test.freemarker.model.Student;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.*;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FreemarkerTest {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    //基于模板生成html文件
    @Test
    public void testGenerateHtml() throws IOException, TemplateException {
        //定义配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //定义模板
        //获取地址、
        String classPath = this.getClass().getResource("/").getPath();
        configuration.setDirectoryForTemplateLoading(new File(classPath + "/templates/"));
        //获取模板文件分内容
        Template template = configuration.getTemplate("test01.ftl");
        //定义数据模型
        Map map = getMap();
        //静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        InputStream is = IOUtils.toInputStream(content);
        FileOutputStream fos = new FileOutputStream(
                new File("d:/abc/test-freemarker.html"));
        IOUtils.copy(is, fos);
        fos.close();
        is.close();

    }

    //基于字符串生成静态页面文件
    @Test
    public void testGenerateHtmlByString() throws IOException, TemplateException {
        //定义配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //设置字符模板
        String templateString = "<html>\n" +
                "<head></head>\n" +
                "<body>\n" +
                "hello ${name}！\n" +
                "</body>\n" +
                "</html>";
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateString);
        //在配置中设置使用的模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        Template template = configuration.getTemplate("template", "utf-8");
        //定义数据模型
        Map map = getMap();
        //静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        InputStream is = IOUtils.toInputStream(content);
        FileOutputStream fos = new FileOutputStream(
                new File("d:/abc/test02-freemarker.html"));
        IOUtils.copy(is, fos);
        fos.close();
        is.close();
    }

    //获取数据模型
    public Map getMap() {
        Map map = new HashMap();
        //向数据模型放数据       
        // 使用Map作为传值对象，相当于jsp中的Model对象  
        map.put("name", "黑马程序员");

        //测试list数据
        //========================================
        Student stu1 = new Student();
        stu1.setName("小明");
        stu1.setAge(18);
        stu1.setMoney(100.86f);
        stu1.setBirthday(new Date());
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19); //        
        stu2.setBirthday(new Date());
        List<Student> friends = new ArrayList<>();
        friends.add(stu1);
        stu2.setFriends(friends);
        stu2.setBestFriend(stu1);
        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);
        //向数据模型放数据         
        map.put("stus", stus);

        //准备map数据         
        //========================================
        HashMap<String, Student> stuMap = new HashMap<>();
        stuMap.put("stu1", stu1);
        stuMap.put("stu2", stu2);
        //向数据模型放数据         
        map.put("stu1", stu1);
        //向数据模型放数据         
        map.put("stuMap", stuMap);
        //返回模板文件名称         
        return map;
    }

    /**
     * 模板通过测试之后要在数据库中进行保存:
     * - 1. 模板信息保存在xc_cms 数据库（MongoDB）的cms_template中
     * - 2. 模板文件保存在MongoDB的GridFS中。
     * ---------------------------------------------------------------
     * 由于本项目中没有模板管理模块，所以我们使用 unit 的方式进行操作，主要分为
     * 以下两 步操作。
     * - 将course.flt 文件存储到 GridFS 中，保存成功后返回模板的id
     * - 在cms_template 中添加记录。
     */
    @Test
    public void testStoreCourseTemplate() throws FileNotFoundException {

        File file = new File(this.getClass().getResource("/").getPath() + "/templates/course.ftl");
        FileInputStream fis = new FileInputStream(file);
        // 使用mongoDB 提供的 GridFS 操作API 存储文件
        ObjectId id = gridFsTemplate.store(fis, "课程详情模板文件", "");
        System.out.println(id.toString());

    }
}
