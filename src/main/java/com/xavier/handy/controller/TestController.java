package com.xavier.handy.controller;

import com.xavier.handy.announce.Controller;
import com.xavier.handy.announce.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/")
public class TestController {

    @RequestMapping("/test")
    public void test(HttpServletRequest request, HttpServletResponse response, String param) {
        System.out.println(param);
        try {
            response.getWriter().write("param:" + param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
