package com.markdown.editor.service;

public interface SmsService {
    void sendPhoneCode(String phone);
    boolean verifyPhoneCode(String phone, String code);
    
    void sendEmailCode(String email);
    boolean verifyEmailCode(String email, String code);
    
    void sendRegisterEmailCode(String email);
    boolean verifyRegisterEmailCode(String email, String code);
    
    void sendResetPasswordEmailCode(String email);
    boolean verifyResetPasswordEmailCode(String email, String code);
}
