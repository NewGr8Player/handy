package com.xavier.tomcat;

import com.alibaba.fastjson.JSONObject;
import com.xavier.tomcat.config.BaseConfig;
import com.xavier.tomcat.util.YamlUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.String;

@Slf4j
public class WebServer {

    public static ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        BaseConfig config = JSONObject.parseObject(JSONObject.toJSONString(YamlUtil.getConfigWithDefault()), BaseConfig.class);
        try {
            /* 绑定端口 */
            ServerSocket serverSocket = new ServerSocket(config.getServer().getPort());

            log.info("Server started at port {}.", config.getServer().getPort());
            while (!serverSocket.isClosed()) {

                Socket request = serverSocket.accept();

                threadPool.execute(() -> {
                            try {
                                InputStream inputStream = request.getInputStream();
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

                                String msg;

                                StringBuilder requestInfo = new StringBuilder("{\n");
                                while ((msg = bufferedReader.readLine()) != null) {
                                    if (msg.length() == 0) {
                                        break;
                                    } else {
                                        requestInfo.append(msg).append("\n");
                                    }
                                }
                                requestInfo.append("}");

                                log.info("{}", requestInfo);
                            } catch (IOException e) {
                                log.error("{}",e.getMessage());
                                e.printStackTrace();
                            }
                        }
                );
            }
        } catch (IOException e) {
            log.error("Server start failed on port {}", config);
            e.printStackTrace();
        }
    }

}
