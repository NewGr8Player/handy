package com.xavier.tomcat.server;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ProjectLoader {

    public static final String WEBAPPS_PATH = new File(".").getAbsolutePath() + "/webapps";

    public static void load() {
        /* 发现项目 */
        File[] projects = new File(WEBAPPS_PATH).listFiles(file -> file.isDirectory());

        for (File project : projects) {

        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class ProjectConfig {
        /* 项目路径 */
        public String projectPath = "";
        /* servlet */
        public Map<String, Object> servlets = new HashMap<>();
        /* servlet-mapping */
        public Map<String, Object> servletMappings = new HashMap<>();

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }
    }
}
