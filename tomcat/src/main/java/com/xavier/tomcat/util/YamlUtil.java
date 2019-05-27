package com.xavier.tomcat.util;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class YamlUtil {

    public static Map<String, Object> getConfig() {
        Yaml yaml = new Yaml();
        Map<String, Object> map;
        try {
            map = yaml.loadAs(YamlUtil.class.getClassLoader().getResource("application.yml").openStream(), HashMap.class);
        } catch (IOException e) {
            map = new HashMap();
            e.printStackTrace();
        }
        return map;
    }

    public static Map<String, Object> getConfigWithDefault() {
        Map<String, Object> baseConfig = new HashMap<>();
        Map<String,Object> port = new HashMap<>();
        port.put("port",8080);
        baseConfig.put("server",port);

        baseConfig.putAll(getConfig());

        return baseConfig;
    }
}
