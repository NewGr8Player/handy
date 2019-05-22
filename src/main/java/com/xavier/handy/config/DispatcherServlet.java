package com.xavier.handy.config;

import com.xavier.handy.announce.Controller;
import com.xavier.handy.announce.RequestMapping;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DispatcherServlet extends HttpServlet {


    /**
     * 这是加载application.properties 的配置
     */
    private Properties properties = new Properties();

    /**
     * 这是扫描controller 所在的包集合，
     */
    private List<String> classNames = new ArrayList<>();

    /**
     * 这是IOC的容器
     */
    private Map<String, Object> ioc = new HashMap<>();

    /**
     * 这是绑定请求路径的
     */
    private Map<String, Method> handlerMapping = new HashMap<>();

    /**
     * 这是绑定控制层的
     */
    private Map<String, Object> controllerMap = new HashMap<>();


    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            /* 1.加载配置文件,其实就是web.xml 的文件 */
            doLoadConfig(config.getInitParameter("contextConfigLocation"));

            /* 2.初始化所有相关联的类,扫描用户设定的包下面所有的类，这里不需要判已经做了非空判断 */
            doScanner(properties.getProperty("scanPackage"));

            /* 3.拿到扫描到的类,通过反射机制,实例化,并且放到ioc容器中(k-v  beanName-bean) beanName默认是首字母小写 */
            doInstance();

            /* 4.初始化HandlerMapping(将url和method对应上) */
            initHandlerMapping();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500!! Server Exception");
            throw new ServletException(e);
        }
    }


    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (!handlerMapping.isEmpty()) {

            String url = req.getRequestURI();
            String contextPath = req.getContextPath();
            /* 拼接url并把多个/替换成一个 */
            url = url.replace(contextPath, "").replaceAll("/+", "/");
            if (!this.handlerMapping.containsKey(url)) {
                resp.getWriter().write("404 NOT FOUND!"); /* 没找到页面~ */
                return;
            }
            Method method = this.handlerMapping.get(url);
            /* 获取方法的参数列表 */
            Class<?>[] parameterTypes = method.getParameterTypes();
            /* 获取请求的参数 */
            Map<String, String[]> parameterMap = req.getParameterMap();
            /* 保存参数值 */
            Object[] paramValues = new Object[parameterTypes.length];
            /* 方法的参数列表 */
            for (int i = 0; i < parameterTypes.length; i++) {
                /* 根据参数名称做处理 */
                String requestParam = parameterTypes[i].getSimpleName();
                switch (requestParam) {
                    case "HttpServletRequest":
                        paramValues[i] = req;
                        break;
                    case "HttpServletResponse":
                        paramValues[i] = resp;
                        break;
                    case "String":
                        for (Entry<String, String[]> param : parameterMap.entrySet()) {
                            String value = Arrays.toString(param.getValue())
                                    .replaceAll("[\\[\\]]", "")
                                    .replaceAll(",\\s", ",");
                            paramValues[i] = value;
                        }
                        break;
                }
            }
            try {
                method.invoke(this.controllerMap.get(url), paramValues);/* obj是method所对应的实例 在ioc容器中 */
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void doLoadConfig(String location) {
        //把web.xml中的contextConfigLocation对应value值的文件加载到留里面
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(location);
        try {
            //用Properties文件加载文件里的内容
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关流
            if (null != resourceAsStream) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void doScanner(String packageName) {
        /* 把所有的.替换成 路径分隔符 */
        URL url = this.getClass().getClassLoader().getResource(File.separator + packageName.replaceAll("\\.", "/"));
        File dir = new File(Objects.requireNonNull(url).getFile());
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                /* 递归读取包 */
                doScanner(packageName + "." + file.getName());
            } else {
                /* 把.class 给替换掉 */
                String className = packageName + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }


    private void doInstance() {
        if (!classNames.isEmpty()) {
            for (String className : classNames) {
                try {
                    /* 实例化被@Controller修饰的方法 */
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(Controller.class)) {
                        ioc.put(toLowerFirstWord(clazz.getSimpleName()), clazz.newInstance());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void initHandlerMapping() {
        if (!ioc.isEmpty()) {

            try {
                for (Entry<String, Object> entry : ioc.entrySet()) {
                    Class<?> clazz = entry.getValue().getClass();
                    if (!clazz.isAnnotationPresent(Controller.class)) {
                        continue;
                    }
                    /* 拼url时,是controller头的url拼上方法上的url */
                    String baseUrl = "";
                    if (clazz.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping annotation = clazz.getAnnotation(RequestMapping.class);
                        baseUrl = annotation.value();
                    }
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        if (!method.isAnnotationPresent(RequestMapping.class)) {
                            continue;
                        }
                        RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                        String url = annotation.value();
                        url = (baseUrl + "/" + url).replaceAll("/+", "/");
                        handlerMapping.put(url, method);
                        controllerMap.put(url, clazz.newInstance());
                        System.out.println(url + "," + method);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 把字符串的首字母小写
     *
     * @param str 字符串
     * @return 首字母小写的单词
     */
    private String toLowerFirstWord(String str) {
        char[] charArray = str.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }
}