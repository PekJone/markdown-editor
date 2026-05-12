package com.markdown.editor.controller;

import com.markdown.editor.dto.ApiResponse;
import com.markdown.editor.dto.JwtResponse;
import com.markdown.editor.dto.LoginRequest;
import com.markdown.editor.dto.RegisterRequest;
import com.markdown.editor.entity.User;
import com.markdown.editor.security.JwtUtils;
import com.markdown.editor.security.UserDetailsImpl;
import com.markdown.editor.service.LoginRecordService;
import com.markdown.editor.service.SmsService;
import com.markdown.editor.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Api(tags = "认证管理", description = "用户登录、注册等认证相关接口")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SmsService smsService;
    
    @Autowired
    private LoginRecordService loginRecordService;

    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "使用用户名和密码登录系统，返回 JWT token")
    public ResponseEntity<?> authenticateUser(
            @ApiParam(value = "登录请求参数，包含用户名和密码", required = true)
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        logger.info("Login attempt for user: {}", loginRequest.getUsername());
        
        try {
            if (!userService.existsByUsername(loginRequest.getUsername())) {
                return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("用户不存在"));
            }
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            String ipAddress = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            loginRecordService.recordLogin(userDetails.getId(), ipAddress, userAgent);

            return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getNickname(),
                userDetails.getAvatar()));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("密码错误"));
        } catch (Exception e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("登录失败"));
        }
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "新用户注册，需要填写用户名、密码、邮箱和邮箱验证码")
    public ResponseEntity<?> registerUser(
            @ApiParam(value = "注册请求参数，包含用户名、密码、邮箱、邮箱验证码、昵称", required = true)
            @Valid @RequestBody RegisterRequest signUpRequest) {
        if (userService.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("用户名已被使用"));
        }

        String email = signUpRequest.getEmail();
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("请输入正确的邮箱格式"));
        }

        int emailCount = userService.countByEmail(email);
        if (emailCount >= 3) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("该邮箱已注册超过3个账号，无法继续注册"));
        }

        if (!smsService.verifyRegisterEmailCode(email, signUpRequest.getEmailCode())) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("邮箱验证码不正确或已过期"));
        }

        if (signUpRequest.getNickname() != null && !signUpRequest.getNickname().isEmpty() && userService.existsByNickname(signUpRequest.getNickname())) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("昵称已被使用"));
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setEmail(email);
        user.setNickname(signUpRequest.getNickname() != null && !signUpRequest.getNickname().isEmpty() ? signUpRequest.getNickname() : signUpRequest.getUsername());

        userService.insert(user);

        return ResponseEntity.ok(ApiResponse.success("注册成功", null));
    }
}
