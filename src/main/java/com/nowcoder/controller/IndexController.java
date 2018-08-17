package com.nowcoder.controller;

import com.nowcoder.aspect.LogAspect;
import com.nowcoder.model.User;
import com.nowcoder.service.ToutiaoService;
import jdk.nashorn.internal.objects.annotations.Constructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.Banner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.file.attribute.GroupPrincipal;
import java.util.*;

/**
 * Created by nowcoder on 2018/6/26.
 */
@Controller
public class IndexController {
    /**
     *     Logger 就是用于输出日志的一个工具类，很简单，就是封装了一下boost库的log
     *     使用指定类初始化日志对象，在日志输出的时候，可以打印出日志信息所在类
     *     如：Logger logger = LoggerFactory.getLogger(com.Book.class);
     *
     *     logger.debug("日志信息");
     *     将会打印出: com.Book  : 日志信息
    */
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
   //自动注入youtiaoService ,讲对象复制过来
    @Autowired
    private ToutiaoService toutiaoService;
    //接收访问主页的地址（通过index也可以访问127.0.0.1 ：8080 这个主页，path和value一样的
    @RequestMapping(path = {"/", "/index"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody//返回的是一个主页
    public String index(HttpSession session) {
       //AOP面向切面，来自logger库的规范
        logger.info("Visit Index");
        return "Hello NowCoder," + session.getAttribute("msg")
                + "<br> Say:" + toutiaoService.say();
    }

    @RequestMapping(value = {"/profile/{groupId}/{userId}"}) //参数传递
    @ResponseBody           //@PathVariable ：path中的参数    requestParam
    public String profile(@PathVariable("groupId") String groupId,
                          @PathVariable("userId") int userId,
                          @RequestParam(value = "type", defaultValue = "1") int type,
                          @RequestParam(value = "key", defaultValue = "nowcoder") String key) {
        //返回参数，显示在网页上
        return String.format("GID{%s},UID{%d},TYPE{%d},KEY{%s}", groupId, userId, type, key);
    }
   @RequestMapping(value = {"/vm"})  //vm的模板。从这里进入
    public String news(Model model) {  //他是 一个后端与渲染交迭的数据格式
        model.addAttribute("value1", "vv1");
        //传递一个List，map
        List<String> colors = Arrays.asList(new String[]{"RED", "GREEN", "BLUE"});

        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < 4; ++i) {
            map.put(String.valueOf(i), String.valueOf(i * i));
        }

        model.addAttribute("colors", colors);
        model.addAttribute("map", map);
        model.addAttribute("user", new User("Jim"));

        return "news";
    }

    @RequestMapping(value = {"/request"})
    @ResponseBody  //把请求包装为规范的类，直接注入
    public String request(HttpServletRequest request,
                          HttpServletResponse response,
                          HttpSession session) {
        StringBuilder sb = new StringBuilder();//因为操作量有点大
        Enumeration<String> headerNames = request.getHeaderNames();//导出所有
       //直接读取所有的head
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            sb.append(name + ":" + request.getHeader(name) + "<br>");
        }
        //加每个cookie都是有value 和值，对cookie最行包装
        for (Cookie cookie : request.getCookies()) {
            sb.append("Cookie:");
            sb.append(cookie.getName());
            sb.append(":");
            sb.append(cookie.getValue());
            sb.append("<br>");
        }
        //ctrl+F5 重新启动
        sb.append("getMethod:" + request.getMethod() + "<br>");
        //request.getPathInfo() url里面的东西
        sb.append("getPathInfo:" + request.getPathInfo() + "<br>");
        sb.append("getQueryString:" + request.getQueryString() + "<br>");
        sb.append("getRequestURI:" + request.getRequestURI() + "<br>");

        return sb.toString();

    }

    @RequestMapping(value = {"/response"})
    @ResponseBody
    //从cookies里面直接解析（除了从request得到之外）
    public String response(@CookieValue(value = "nowcoderid", defaultValue = "a") String nowcoderId,
                           @RequestParam(value = "key", defaultValue = "key") String key,
                           @RequestParam(value = "value", defaultValue = "value") String value,
                           HttpServletResponse response) {
        //把cookie加入到response里面
        response.addCookie(new Cookie(key, value));
        response.addHeader(key, value);
        return "NowCoderId From Cookie:" + nowcoderId;
    }

    //临时转移
    @RequestMapping("/redirect/{code}")
    public String redirect(@PathVariable("code") int code,
                           HttpSession session) {
        //只要不关闭，所有的回话就是同一个session
        /*

        RedirectView red = new RedirectView("/", true);
        if (code == 301) {//301永久性的转移   302临时转移是默认的
            red.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        }
        return red;*/
        //一次打开网页，所有的session表示一次回话，把消息存在session中
        session.setAttribute("msg", "Jump from redirect.");
        //302跳转，直接跳转到首页
        return "redirect:/";
    }

    @RequestMapping("/admin")
    @ResponseBody
    public String admin(@RequestParam(value = "key", required = false) String key) {
        if ("admin".equals(key)) {
            return "hello admin";
        }
        throw new IllegalArgumentException("Key 错误");
    }
   //自定义的一个error
    @ExceptionHandler()
    @ResponseBody
    public String error(Exception e) {
        return "error:" + e.getMessage();
    }

}
