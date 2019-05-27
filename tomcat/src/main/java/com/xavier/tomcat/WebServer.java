package com.xavier.tomcat;

import com.alibaba.fastjson.JSONObject;
import com.xavier.tomcat.config.BaseConfig;
import com.xavier.tomcat.util.YamlUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
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

                                log.info("Request:{}", requestInfo);

                                StringBuilder response = new StringBuilder();

                                String content = "<html><head><title>Index</title></head><body>OK!</body></html>";
                                
                                response.append("HTTP/1.1 200 OK").append("\r\n");
                                response.append("Date: ").append(new Date()).append("\r\n");
                                response.append("Content-Type: ").append("text/html").append(";charset=").append("UTF-8").append("\r\n");
                                response.append("Content-Length: ").append(content.getBytes().length).append("\r\n");
                                response.append("\r\n").append(content);

                                OutputStream outputStream = request.getOutputStream();
                                outputStream.write(response.toString().getBytes());
                                outputStream.flush();

                            } catch (IOException e) {
                                log.error("{}", e.getMessage());
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
