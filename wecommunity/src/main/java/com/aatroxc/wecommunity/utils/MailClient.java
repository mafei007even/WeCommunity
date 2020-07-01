package com.aatroxc.wecommunity.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.w3c.dom.html.HTMLDListElement;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author mafei007
 * @date 2020/3/31 22:38
 */

@Component
@Slf4j
public class MailClient {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public MailClient(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    @Async
    public void sendMail(String to, String subject, String content) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            //设置发件人
            helper.setFrom(mailProperties.getUsername());
            helper.setTo(to);
            helper.setSubject(subject);
            // 指定内容为 html 文本
            helper.setText(content, true);
            mailSender.send(helper.getMimeMessage());

        } catch (MessagingException e) {
            log.error("发送邮件失败：" + e.getMessage(), e);
        }
    }

}
