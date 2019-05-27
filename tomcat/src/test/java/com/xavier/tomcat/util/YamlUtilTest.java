package com.xavier.tomcat.util;

import org.junit.Test;


public class YamlUtilTest {

    @Test
    public void getConfig() {
        System.out.println(YamlUtil.getConfig());
    }

    @Test
    public void getConfigWithDefault() {
        System.out.println(YamlUtil.getConfigWithDefault());
    }
}