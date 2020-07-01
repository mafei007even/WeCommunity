package com.aatroxc.wecommunity.service;

import cn.hutool.core.bean.BeanUtil;
import com.aatroxc.wecommunity.utils.RedisKeyUtils;
import com.google.code.kaptcha.Producer;
import com.aatroxc.wecommunity.model.enums.UserActivationStatus;
import com.aatroxc.wecommunity.dao.UserMapper;
import com.aatroxc.wecommunity.model.entity.User;
import com.aatroxc.wecommunity.model.enums.UserType;
import com.aatroxc.wecommunity.model.support.UserInfo;
import com.aatroxc.wecommunity.utils.CodecUtils;
import com.aatroxc.wecommunity.utils.JsonUtils;
import com.aatroxc.wecommunity.utils.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import tk.mybatis.mapper.entity.Example;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author mafei007
 * @date 2020/3/30 19:11
 */

@Service
public class UserService {

    private final UserMapper userMapper;
    private final MailClient mailClient;
    private final TemplateEngine templateEngine;
    private final StringRedisTemplate redisTemplate;
    private final Producer kapchaProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public UserService(UserMapper userMapper, MailClient mailClient, TemplateEngine templateEngine, StringRedisTemplate redisTemplate, Producer kapchaProducer) {
        this.userMapper = userMapper;
        this.mailClient = mailClient;
        this.templateEngine = templateEngine;
        this.redisTemplate = redisTemplate;
        this.kapchaProducer = kapchaProducer;
    }


    public User findUserById(Integer userId) {
        User user = getUserCache(userId);
        if (user == null) {
            user = initUserCache(userId);
        }
        return user;
    }


    public Map<String, Object> register(User user) {

        HashMap<String, Object> map = new HashMap<>();


        // 验证账号是否存在
        Example example = new Example(User.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", user.getUsername());

        int count = userMapper.selectCountByExample(example);
        if (count > 0) {
            map.put("usernameMsg", "该账号已经存在!");
            return map;
        }

        // 验证邮箱
        Example example2 = new Example(User.class);
        Example.Criteria criteria2 = example2.createCriteria();
        criteria2.andEqualTo("email", user.getEmail());

        int count2 = userMapper.selectCountByExample(example2);
        if (count2 > 0) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        // 注册
        String salt = CodecUtils.generateUUID();
        String encryptedPw = CodecUtils.md5Hex(user.getPassword(), salt);

        user.setSalt(salt);
        user.setPassword(encryptedPw);
        user.setStatus(UserActivationStatus.NOT_ACTIVED);
        user.setType(UserType.ORDINARY);
        user.setActivationCode(CodecUtils.generateUUID());

        String urlTemplate = "/head/%dt.png";
        String headerUrl = String.format(urlTemplate, new Random().nextInt(1000));
        user.setHeaderUrl(headerUrl);
        user.setCreateTime(new Date());

        // 插入后自增长 id 会回显
        userMapper.insertSelective(user);

        // 发送激活邮箱
        sendActivationEmail(user);

        return map;
    }

    private void sendActivationEmail(User user) {

        Context context = new Context();
        context.setVariable("username", user.getUsername());

        // http://localhost:8080/community/activation/{uid}/{code}
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);

        String content = templateEngine.process("/mail/activation", context);

        mailClient.sendMail(user.getEmail(), "激活账号", content);

    }


    /**
     * 激活账号
     *
     * @param userId
     * @param code
     * @return
     */
    public UserActivationStatus activation(int userId, String code) {
        User user = userMapper.selectByPrimaryKey(userId);

        if (user == null) {
            return UserActivationStatus.FAILURE;
        }

        // 是否已经激活
        if (UserActivationStatus.ACTIVED.equals(user.getStatus())) {
            return UserActivationStatus.REPEAT;
        }

        if (user.getActivationCode().equals(code)) {
            // 更新激活状态
            user.setStatus(UserActivationStatus.ACTIVED);
            userMapper.updateByPrimaryKeySelective(user);
            // 清除缓存
            clearUserCache(userId);
            return UserActivationStatus.ACTIVED;
        }

        return UserActivationStatus.FAILURE;
    }


    public BufferedImage genCaptcha(String captchaId) {

        String code = kapchaProducer.createText();
        // 存入 redis
        String captchaKey = RedisKeyUtils.getCaptchaKey(captchaId);
        redisTemplate.opsForValue().set(captchaKey, code, 60, TimeUnit.SECONDS);

        BufferedImage image = kapchaProducer.createImage(code);

        return image;
    }


    public Map<String, Object> login(String username, String password) {
        HashMap<String, Object> map = new HashMap<>();

        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        // 验证
        User t = new User();
        t.setUsername(username);
        User user = userMapper.selectOne(t);

        if (user == null) {
            map.put("usernameMsg", "账号或密码错误！");
            map.put("passwordMsg", "账号或密码错误！");
            return map;
        }

        // 验证密码
        String encryptedPw = CodecUtils.md5Hex(password, user.getSalt());
        if (!StringUtils.equals(encryptedPw, user.getPassword())) {
            map.put("usernameMsg", "账号或密码错误！");
            map.put("passwordMsg", "账号或密码错误！");
            return map;
        }

        // 验证账号是否激活
        if (UserActivationStatus.NOT_ACTIVED.equals(user.getStatus())) {
            map.put("usernameMsg", "账号未激活！");
            return map;
        }

        // 生成登陆凭证，存到 redis，并要返回给客户端 cookie 的
        String uuid = CodecUtils.generateUUID();

        UserInfo userInfo = new UserInfo();
        BeanUtil.copyProperties(user, userInfo, "status");
        userInfo.setStatus(1);

        saveUserInfo(userInfo, uuid);

        // 返回凭证
        map.put("ticket", uuid);

        return map;
    }

    /**
     * 永久保存 redis 中的登陆凭证，要用于数据统计
     * @param userInfo
     * @param ticket
     */
    public void saveUserInfo(UserInfo userInfo, String ticket) {
        String json = JsonUtils.objectToJson(userInfo);
        String ticketKey = RedisKeyUtils.getTicketKey(ticket);
        redisTemplate.opsForValue().set(ticketKey, json);
    }

    /**
     * 退出时不删除 ticket，而是将 status 改为 0，
     * 数据留着用于后续统计
     * @param ticket
     */
    public void logout(String ticket) {
        String ticketKey = RedisKeyUtils.getTicketKey(ticket);
        String json = redisTemplate.opsForValue().get(ticketKey);
        if (json == null){
            return;
        }
        UserInfo userInfo = JsonUtils.jsonToPojo(json, UserInfo.class);
        userInfo.setStatus(0);
        // 没有指定过期时间的话会成为永久的 key
        // 因为 ticket 使用 UUID, 不用担心 key 重复
        redisTemplate.opsForValue().set(ticketKey, JsonUtils.objectToJson(userInfo));
    }

    public UserInfo findUserInfo(String ticket) {
        if (ticket == null) {
            return null;
        }
        String ticketKey = RedisKeyUtils.getTicketKey(ticket);
        String json = redisTemplate.opsForValue().get(ticketKey);
        UserInfo userInfo = JsonUtils.jsonToPojo(json, UserInfo.class);
        if (userInfo == null) {
            return null;
        }
        // status 为 0 说明当前的 ticket 已经 logout了
        if (userInfo.getStatus().equals(0)) {
            return null;
        }
        return userInfo;
    }


    public int updateHeader(Integer userId, String headerUrl) {
        User user = new User();
        user.setId(userId);
        user.setHeaderUrl(headerUrl);
        int rows = userMapper.updateByPrimaryKeySelective(user);

        // 清除缓存
        clearUserCache(userId);
        return rows;
    }

    public User findUserByUsername(String username){
        User user = new User();
        user.setUsername(username);
        return userMapper.selectOne(user);
    }

    /**
     * 优先从缓存中取数据
     * @param userId
     * @return
     */
    private User getUserCache(Integer userId){
        String userKey = RedisKeyUtils.getUserKey(userId);
        String json = redisTemplate.opsForValue().get(userKey);
        if (json == null){
            return null;
        }
        return JsonUtils.jsonToPojo(json, User.class);
    }

    /**
     * 缓存取不到时初始化缓存数据
     * @param userId
     * @return
     */
    private User initUserCache(Integer userId){
        String userKey = RedisKeyUtils.getUserKey(userId);
        User user = userMapper.selectByPrimaryKey(userId);
        if (user != null) {
            // 保存1个小时
            redisTemplate.opsForValue().set(userKey, JsonUtils.objectToJson(user), 1, TimeUnit.HOURS);
            return user;
        }
        return null;
    }

    /**
     * 数据变更时清除缓存数据
     * @param userId
     */
    private void clearUserCache(Integer userId){
        String userKey = RedisKeyUtils.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    public Collection<? extends GrantedAuthority> getAutorities(Integer userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getType().name();
            }
        });
        return list;
    }

    public Collection<? extends GrantedAuthority> getAutorities(UserInfo userInfo) {
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userInfo.getType().name();
            }
        });
        return list;
    }

}
