package com.xuecheng.manage_cms.restTemplates;

import org.junit.Test;

import java.nio.file.Paths;


public class CommonTest {

    @Test
    public void testdir() {
        System.out.println(Paths.get(System.getProperty("user.dir")).getParent().toString());
    }


}
