package com.aatroxc.wecommunity.controller;

import com.aatroxc.wecommunity.utils.RedisKeyUtils;
import com.aatroxc.wecommunity.model.enums.UserActivationStatus;
import com.aatroxc.wecommunity.model.enums.ExpiredTime;
import com.aatroxc.wecommunity.model.entity.User;
import com.aatroxc.wecommunity.model.support.UserHolder;
import com.aatroxc.wecommunity.model.support.UserInfo;
import com.aatroxc.wecommunity.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

/**
 * @author mafei007
 * @date 2020/4/1 17:47
 */

@Controller
@Slf4j
public class LoginController {

    private UserService userService;
    private StringRedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;


    public LoginController(UserService userService, StringRedisTemplate redisTemplate) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }


    @GetMapping("register")
    public String getRegisterPage() {
        return "site/register";
    }

    @GetMapping("login")
    public String getLoginPage() {
        // 已经登陆的就重定向到主页
        UserInfo userInfo = UserHolder.get();
        if (userInfo != null){
            return "redirect:index";
        }
        return "site/login";
    }


    @PostMapping("register")
    public String register(Model model, User user, HttpServletRequest request) {

        Map<String, Object> map = userService.register(user);

        String ip = request.getRemoteHost();
        // 没有错误，注册成功
        if (CollectionUtils.isEmpty(map)) {

            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了" +
                    "一封激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            log.info(String.format("ip: %s 注册了账号: User=%s", ip, user.toString()));
            return "site/operate-result";
        }

        // 注册出现错误
        model.addAttribute("usernameMsg", map.get("usernameMsg"));
        model.addAttribute("emailMsg", map.get("emailMsg"));
        log.info(String.format("ip: %s 注册账号错误, User=%s, errMsg=%s", ip, user.toString(), map));
        return "site/register";
    }

    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model,
                             @PathVariable("userId") int userId,
                             @PathVariable("code") String code) {

        UserActivationStatus userActivationStatus = userService.activation(userId, code);

        switch (userActivationStatus) {
            case ACTIVED:
                model.addAttribute("msg", "激活成功，您的账号可以正常使用了！");
                model.addAttribute("target", "/login");
                break;
            case REPEAT:
                model.addAttribute("msg", "无效操作，该账号已经激活！");
                model.addAttribute("target", "/index");
                break;
            case FAILURE:
                model.addAttribute("msg", "激活失败，激活码不正确！");
                model.addAttribute("target", "/index");
                break;
            default:

        }

        return "site/operate-result";
    }


    @GetMapping("kaptcha")
    public void genKaptcha(HttpServletResponse response, @RequestParam String captchaId) {
        BufferedImage image = userService.genCaptcha(captchaId);

        response.setContentType("image/png");
        try {
            ServletOutputStream output = response.getOutputStream();
            ImageIO.write(image, "png", output);
        } catch (IOException e) {
            log.error("响应验证码失败：" + e.getMessage(), e);
        }

    }

    @PostMapping("login")
    public String login(String username, String password,
                        String code, String captchaId,
                        @RequestParam(required = false, defaultValue = "false") Boolean remeberMe,
                        Model model, HttpServletResponse response, HttpServletRequest request) {

        // 检查验证码
        String captchaKey = RedisKeyUtils.getCaptchaKey(captchaId);
        String realCode = redisTemplate.opsForValue().get(captchaKey);
        if (realCode == null) {
            model.addAttribute("codeMsg", "验证码已过期！");
            return "site/login";
        }

        // 不管是否正确都删除redis 中的验证码
        redisTemplate.delete(captchaKey);

        if (!StringUtils.equalsIgnoreCase(code, realCode)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "site/login";
        }

        // 验证账号密码
        ExpiredTime expiredTime = remeberMe ? ExpiredTime.REMEMBER_EXPIRED : ExpiredTime.DEFAULT_EXPIRED;
        Map<String, Object> map = userService.login(username, password);

        if (map.containsKey("ticket")) {

            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge((int) expiredTime.getTimeUnit().toSeconds(expiredTime.getTimeout()));
            response.addCookie(cookie);

            return "redirect:/index";
        }

        model.addAttribute("usernameMsg", map.get("usernameMsg"));
        model.addAttribute("passwordMsg", map.get("passwordMsg"));
        log.info(String.format("ip: %s 登陆账号失败, username=%s, password=%s, errMsg=%s",
                request.getRemoteHost(), username, password, map));

        return "site/login";
    }

    @GetMapping("logout")
    public String logout(@CookieValue("ticket") String ticket, HttpServletResponse response){

        userService.logout(ticket);

        // 删除客户端 cookie
        Cookie cookie = new Cookie("ticket", "");
        cookie.setPath(contextPath);
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        SecurityContextHolder.clearContext();

        return "redirect:/login";
    }

}
