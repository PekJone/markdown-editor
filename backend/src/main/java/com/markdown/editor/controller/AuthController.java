package com.markdown.editor.controller;

import com.markdown.editor.dto.ApiResponse;
import com.markdown.editor.dto.JwtResponse;
import com.markdown.editor.dto.LoginRequest;
import com.markdown.editor.dto.RegisterRequest;
import com.markdown.editor.entity.User;
import com.markdown.editor.security.JwtUtils;
import com.markdown.editor.security.UserDetailsImpl;
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

    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "使用用户名和密码登录系统，返回 JWT token")
    public ResponseEntity<?> authenticateUser(
            @ApiParam(value = "登录请求参数，包含用户名和密码", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for user: {}", loginRequest.getUsername());
        
        try {
            // 先检查用户是否存在
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

            return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getNickname(),
                userDetails.getAvatar()));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // 密码错误
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("密码错误"));
        } catch (Exception e) {
            // 其他错误
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("登录失败"));
        }

    }

    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "新用户注册，需要填写用户名、密码，昵称和邮箱可选")
    public ResponseEntity<?> registerUser(
            @ApiParam(value = "注册请求参数，包含用户名、密码、昵称、邮箱", required = true)
            @Valid @RequestBody RegisterRequest signUpRequest) {
        if (userService.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Error: Username is already taken!"));
        }

        if (signUpRequest.getEmail() != null && !signUpRequest.getEmail().isEmpty() && userService.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Error: Email is already in use!"));
        }

        if (signUpRequest.getNickname() != null && !signUpRequest.getNickname().isEmpty() && userService.existsByNickname(signUpRequest.getNickname())) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Error: Nickname is already taken!"));
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setEmail(signUpRequest.getEmail() != null && !signUpRequest.getEmail().isEmpty() ? signUpRequest.getEmail() : null);
        user.setNickname(signUpRequest.getNickname() != null && !signUpRequest.getNickname().isEmpty() ? signUpRequest.getNickname() : signUpRequest.getUsername());

        userService.insert(user);

        return ResponseEntity.ok(ApiResponse.success("User registered successfully!", null));
    }
}
