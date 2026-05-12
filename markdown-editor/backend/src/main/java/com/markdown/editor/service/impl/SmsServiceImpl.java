package com.markdown.editor.service.impl;

import com.markdown.editor.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);
    
    private final Map<String, VerificationCode> phoneCodes = new ConcurrentHashMap<>();
    private final Map<String, VerificationCode> emailCodes = new ConcurrentHashMap<>();
    private final Map<String, VerificationCode> registerEmailCodes = new ConcurrentHashMap<>();
    private final Map<String, VerificationCode> resetPasswordEmailCodes = new ConcurrentHashMap<>();
    
    @Autowired
    private JavaMailSender javaMailSender;
    
    private static class VerificationCode {
        String code;
        LocalDateTime expireTime;
        
        VerificationCode(String code, LocalDateTime expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
    }
    
    private String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
    
    @Override
    public void sendPhoneCode(String phone) {
        String code = generateCode();
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(5);
        
        System.out.println("[短信模拟] 发送验证码到手机 " + phone + ": " + code);
        
        phoneCodes.put(phone, new VerificationCode(code, expireTime));
    }
    
    @Override
    public boolean verifyPhoneCode(String phone, String code) {
        VerificationCode verificationCode = phoneCodes.get(phone);
        if (verificationCode == null) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(verificationCode.expireTime)) {
            phoneCodes.remove(phone);
            return false;
        }
        
        boolean valid = verificationCode.code.equals(code);
        if (valid) {
            phoneCodes.remove(phone);
        }
        
        return valid;
    }
    
    @Override
    public void sendEmailCode(String email) {
        String code = generateCode();
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(5);
        
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setHeader("Content-Transfer-Encoding", "base64");
            
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("742662307@qq.com");
            helper.setTo(email);
            helper.setSubject("Markdown Editor 验证码");
            
            String htmlContent = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body>" +
                "<div style=\"font-family: 'Microsoft YaHei', 'PingFang SC', Arial, sans-serif; max-width: 400px; margin: 20px auto;\">" +
                "<h2 style=\"color: #667eea; margin-bottom: 20px;\">Markdown Editor</h2>" +
                "<p style=\"color: #333; font-size: 14px; line-height: 1.6;\">您好，您正在进行邮箱验证操作，您的验证码是：</p>" +
                "<div style=\"background: #f5f7fa; padding: 20px; border-radius: 8px; text-align: center; margin: 20px 0;\">" +
                "<span style=\"font-size: 32px; font-weight: bold; color: #667eea;\">" + code + "</span>" +
                "</div>" +
                "<p style=\"color: #999; font-size: 12px; line-height: 1.6;\">验证码有效期为5分钟，请在有效期内完成验证。</p>" +
                "<p style=\"color: #999; font-size: 12px; line-height: 1.6;\">如非本人操作，请忽略此邮件。</p>" +
                "</div></body></html>";
            
            String textContent = "Markdown Editor 验证码\n\n" +
                "您好，您正在进行邮箱验证操作，您的验证码是：\n\n" +
                code + "\n\n" +
                "验证码有效期为5分钟，请在有效期内完成验证。\n" +
                "如非本人操作，请忽略此邮件。";
            
            helper.setText(textContent, htmlContent);
            
            javaMailSender.send(message);
            logger.info("验证码邮件发送成功，收件人: {}", email);
            
        } catch (MessagingException e) {
            logger.error("发送邮件失败: {}", e.getMessage());
            throw new RuntimeException("发送邮件失败: " + e.getMessage());
        }
        
        emailCodes.put(email, new VerificationCode(code, expireTime));
    }
    
    @Override
    public boolean verifyEmailCode(String email, String code) {
        VerificationCode verificationCode = emailCodes.get(email);
        if (verificationCode == null) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(verificationCode.expireTime)) {
            emailCodes.remove(email);
            return false;
        }
        
        boolean valid = verificationCode.code.equals(code);
        if (valid) {
            emailCodes.remove(email);
        }
        
        return valid;
    }
    
    @Override
    public void sendRegisterEmailCode(String email) {
        String code = generateCode();
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(5);
        
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setHeader("Content-Transfer-Encoding", "base64");
            
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("742662307@qq.com");
            helper.setTo(email);
            helper.setSubject("Markdown Editor 注册验证");
            
            String htmlContent = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body>" +
                "<div style=\"font-family: 'Microsoft YaHei', 'PingFang SC', Arial, sans-serif; max-width: 400px; margin: 20px auto;\">" +
                "<h2 style=\"color: #667eea; margin-bottom: 20px;\">Markdown Editor</h2>" +
                "<p style=\"color: #333; font-size: 14px; line-height: 1.6;\">您好，欢迎注册 Markdown Editor！您的注册验证码是：</p>" +
                "<div style=\"background: #f5f7fa; padding: 20px; border-radius: 8px; text-align: center; margin: 20px 0;\">" +
                "<span style=\"font-size: 32px; font-weight: bold; color: #667eea;\">" + code + "</span>" +
                "</div>" +
                "<p style=\"color: #999; font-size: 12px; line-height: 1.6;\">验证码有效期为5分钟，请在有效期内完成注册。</p>" +
                "<p style=\"color: #999; font-size: 12px; line-height: 1.6;\">如非本人操作，请忽略此邮件。</p>" +
                "</div></body></html>";
            
            String textContent = "Markdown Editor 注册验证\n\n" +
                "您好，欢迎注册 Markdown Editor！您的注册验证码是：\n\n" +
                code + "\n\n" +
                "验证码有效期为5分钟，请在有效期内完成注册。\n" +
                "如非本人操作，请忽略此邮件。";
            
            helper.setText(textContent, htmlContent);
            
            javaMailSender.send(message);
            logger.info("注册验证码邮件发送成功，收件人: {}", email);
            
        } catch (MessagingException e) {
            logger.error("发送注册邮件失败: {}", e.getMessage());
            throw new RuntimeException("发送邮件失败: " + e.getMessage());
        }
        
        registerEmailCodes.put(email, new VerificationCode(code, expireTime));
    }
    
    @Override
    public boolean verifyRegisterEmailCode(String email, String code) {
        VerificationCode verificationCode = registerEmailCodes.get(email);
        if (verificationCode == null) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(verificationCode.expireTime)) {
            registerEmailCodes.remove(email);
            return false;
        }
        
        boolean valid = verificationCode.code.equals(code);
        if (valid) {
            registerEmailCodes.remove(email);
        }
        
        return valid;
    }
    
    @Override
    public void sendResetPasswordEmailCode(String email) {
        String code = generateCode();
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(5);
        
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setHeader("Content-Transfer-Encoding", "base64");
            
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("742662307@qq.com");
            helper.setTo(email);
            helper.setSubject("Markdown Editor 密码重置");
            
            String htmlContent = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body>" +
                "<div style=\"font-family: 'Microsoft YaHei', 'PingFang SC', Arial, sans-serif; max-width: 400px; margin: 20px auto;\">" +
                "<h2 style=\"color: #667eea; margin-bottom: 20px;\">Markdown Editor</h2>" +
                "<p style=\"color: #333; font-size: 14px; line-height: 1.6;\">您好，您正在进行密码重置操作，您的验证码是：</p>" +
                "<div style=\"background: #f5f7fa; padding: 20px; border-radius: 8px; text-align: center; margin: 20px 0;\">" +
                "<span style=\"font-size: 32px; font-weight: bold; color: #667eea;\">" + code + "</span>" +
                "</div>" +
                "<p style=\"color: #999; font-size: 12px; line-height: 1.6;\">验证码有效期为5分钟，请在有效期内完成密码重置。</p>" +
                "<p style=\"color: #999; font-size: 12px; line-height: 1.6;\">如非本人操作，请忽略此邮件。</p>" +
                "</div></body></html>";
            
            String textContent = "Markdown Editor 密码重置\n\n" +
                "您好，您正在进行密码重置操作，您的验证码是：\n\n" +
                code + "\n\n" +
                "验证码有效期为5分钟，请在有效期内完成密码重置。\n" +
                "如非本人操作，请忽略此邮件。";
            
            helper.setText(textContent, htmlContent);
            
            javaMailSender.send(message);
            logger.info("密码重置验证码邮件发送成功，收件人: {}", email);
            
        } catch (MessagingException e) {
            logger.error("发送密码重置邮件失败: {}", e.getMessage());
            throw new RuntimeException("发送邮件失败: " + e.getMessage());
        }
        
        resetPasswordEmailCodes.put(email, new VerificationCode(code, expireTime));
    }
    
    @Override
    public boolean verifyResetPasswordEmailCode(String email, String code) {
        VerificationCode verificationCode = resetPasswordEmailCodes.get(email);
        if (verificationCode == null) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(verificationCode.expireTime)) {
            resetPasswordEmailCodes.remove(email);
            return false;
        }
        
        boolean valid = verificationCode.code.equals(code);
        if (valid) {
            resetPasswordEmailCodes.remove(email);
        }
        
        return valid;
    }
}
