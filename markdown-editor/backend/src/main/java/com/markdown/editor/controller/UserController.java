package com.markdown.editor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markdown.editor.dto.ApiResponse;
import com.markdown.editor.dto.UserStatsDTO;
import com.markdown.editor.entity.Document;
import com.markdown.editor.entity.DocumentCollection;
import com.markdown.editor.entity.User;
import com.markdown.editor.security.UserDetailsImpl;
import com.markdown.editor.service.CommentService;
import com.markdown.editor.service.DocumentService;
import com.markdown.editor.service.DocumentCollectionService;
import com.markdown.editor.service.StatisticsService;
import com.markdown.editor.service.UserService;
import com.markdown.editor.entity.LoginRecord;
import com.markdown.editor.service.LoginRecordService;
import com.markdown.editor.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.markdown.editor.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Api(tags = "用户管理", description = "用户信息查询、统计、收藏、头像等相关接口")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentCollectionService documentCollectionService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private StatisticsService statisticsService;
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private LoginRecordService loginRecordService;

    @GetMapping("/stats")
    @ApiOperation(value = "获取当前登录用户统计数据", notes = "获取当前登录用户的文档数、粉丝数、关注数等统计信息")
    public ResponseEntity<?> getUserStats(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        UserStatsDTO stats = statisticsService.calculateUserStats(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("获取用户统计数据成功", stats));
    }

    @GetMapping("/{userId}/stats")
    @ApiOperation(value = "获取指定用户统计数据", notes = "获取指定用户的文档数、粉丝数、关注数等统计信息")
    public ResponseEntity<?> getUserStats(
            @ApiParam(value = "用户ID", required = true)
            @PathVariable Long userId) {
        LogUtils.info(logger, "开始获取用户统计数据，userId: {}", userId);
        UserStatsDTO stats = statisticsService.calculateUserStats(userId);
        LogUtils.info(logger, "获取用户统计数据成功，stats: {}", stats);
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(ApiResponse.success("获取用户统计数据成功", stats));
    }

    @GetMapping("/info")
    @ApiOperation(value = "获取当前登录用户信息", notes = "获取当前登录用户的详细信息")
    public ResponseEntity<?> getUserInfo(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userService.selectById(userDetails.getId());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", user));
    }

    @GetMapping("/{userId}/collections")
    @ApiOperation(value = "获取用户收藏的文档列表", notes = "获取指定用户收藏的所有文档")
    public ResponseEntity<?> getUserCollections(
            @ApiParam(value = "用户ID", required = true)
            @PathVariable Long userId) {
        List<Document> collectionDocuments = documentService.selectUserCollections(userId);
        return ResponseEntity.ok(ApiResponse.success("获取收藏列表成功", collectionDocuments));
    }

    @GetMapping("/{userId}")
    @ApiOperation(value = "获取指定用户信息", notes = "根据用户ID获取用户详细信息")
    public ResponseEntity<?> getUserById(
            @ApiParam(value = "用户ID", required = true)
            @PathVariable Long userId) {
        User user = userService.selectById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    @ApiOperation(value = "搜索用户", notes = "根据关键词搜索用户，支持用户名、昵称模糊搜索")
    public ResponseEntity<?> searchUsers(
            @ApiParam(value = "搜索关键词", required = true)
            @RequestParam String keyword) {
        List<User> users = userService.searchUsers(keyword);
        return ResponseEntity.ok(ApiResponse.success("搜索用户成功", users));
    }

    @PostMapping("/avatar")
    @ApiOperation(value = "更新当前用户头像", notes = "更新当前登录用户的头像")
    public ResponseEntity<?> updateAvatar(
            @ApiParam(value = "头像文件", required = true)
            @RequestParam("avatar") MultipartFile file, 
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请选择要上传的图片"));
        }
        
        String avatarUrl = userService.uploadAvatar(userDetails.getId(), file);
        
        Map<String, Object> result = new HashMap<>();
        result.put("url", avatarUrl);
        return ResponseEntity.ok(ApiResponse.success("更新头像成功", result));
    }

    @GetMapping("/{userId}/avatar")
    @ApiOperation(value = "获取指定用户头像", notes = "获取指定用户的头像URL")
    public ResponseEntity<?> getAvatar(
            @ApiParam(value = "用户ID", required = true)
            @PathVariable Long userId) {
        String avatar = userService.getAvatar(userId);
        return ResponseEntity.ok(ApiResponse.success("获取头像成功", avatar));
    }

    @PutMapping("/info")
    @ApiOperation(value = "更新用户信息", notes = "更新当前登录用户的基本信息")
    public ResponseEntity<?> updateUserInfo(
            @RequestBody User user,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User existingUser = userService.selectById(userDetails.getId());
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (user.getNickname() != null) {
            existingUser.setNickname(user.getNickname());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }
        if (user.getGender() != null) {
            existingUser.setGender(user.getGender());
        }
        if (user.getBio() != null) {
            existingUser.setBio(user.getBio());
        }
        if (user.getLocation() != null) {
            existingUser.setLocation(user.getLocation());
        }
        if (user.getBirthDate() != null) {
            existingUser.setBirthDate(user.getBirthDate());
        }
        if (user.getSchool() != null) {
            existingUser.setSchool(user.getSchool());
        }
        if (user.getMajor() != null) {
            existingUser.setMajor(user.getMajor());
        }
        if (user.getEnrollmentDate() != null) {
            existingUser.setEnrollmentDate(user.getEnrollmentDate());
        }
        if (user.getEducation() != null) {
            existingUser.setEducation(user.getEducation());
        }
        if (user.getStartWorkDate() != null) {
            existingUser.setStartWorkDate(user.getStartWorkDate());
        }
        
        userService.update(existingUser);
        return ResponseEntity.ok(ApiResponse.success("更新用户信息成功", existingUser));
    }

    @GetMapping("/profile")
    @ApiOperation(value = "获取当前用户完整资料", notes = "获取当前登录用户的完整个人资料信息")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userService.selectById(userDetails.getId());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(ApiResponse.success("获取用户资料成功", user));
    }

    @PutMapping("/profile")
    @ApiOperation(value = "更新当前用户完整资料", notes = "更新当前登录用户的完整个人资料信息")
    public ResponseEntity<?> updateProfile(
            @RequestBody User user,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User existingUser = userService.selectById(userDetails.getId());
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (user.getNickname() != null) {
            existingUser.setNickname(user.getNickname());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }
        if (user.getGender() != null) {
            existingUser.setGender(user.getGender());
        }
        if (user.getBio() != null) {
            existingUser.setBio(user.getBio());
        }
        if (user.getLocation() != null) {
            existingUser.setLocation(user.getLocation());
        }
        if (user.getBirthDate() != null) {
            existingUser.setBirthDate(user.getBirthDate());
        }
        if (user.getSchool() != null) {
            existingUser.setSchool(user.getSchool());
        }
        if (user.getMajor() != null) {
            existingUser.setMajor(user.getMajor());
        }
        if (user.getEnrollmentDate() != null) {
            existingUser.setEnrollmentDate(user.getEnrollmentDate());
        }
        if (user.getEducation() != null) {
            existingUser.setEducation(user.getEducation());
        }
        if (user.getStartWorkDate() != null) {
            existingUser.setStartWorkDate(user.getStartWorkDate());
        }
        
        userService.update(existingUser);
        return ResponseEntity.ok(ApiResponse.success("更新用户资料成功", existingUser));
    }

    @PutMapping("/password")
    @ApiOperation(value = "修改密码", notes = "修改当前登录用户的密码")
    public ResponseEntity<?> updatePassword(
            @RequestBody Map<String, String> passwordData,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请提供原密码和新密码"));
        }
        
        boolean success = userService.updatePassword(userDetails.getId(), oldPassword, newPassword);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("密码修改成功"));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("原密码不正确"));
        }
    }

    @PostMapping("/password/reset/code")
    @ApiOperation(value = "发送密码重置邮箱验证码", notes = "向指定邮箱发送密码重置验证码")
    public ResponseEntity<?> sendResetPasswordCode(@RequestBody Map<String, String> data) {
        String email = data.get("email");
        
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请提供邮箱"));
        }
        
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请输入正确的邮箱"));
        }
        
        if (!userService.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("该邮箱未注册"));
        }
        
        smsService.sendResetPasswordEmailCode(email);
        return ResponseEntity.ok(ApiResponse.success("验证码已发送"));
    }

    @PostMapping("/password/reset")
    @ApiOperation(value = "通过邮箱验证码重置密码", notes = "使用邮箱验证码验证后重置密码")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> data) {
        String email = data.get("email");
        String code = data.get("code");
        String newPassword = data.get("newPassword");
        
        if (email == null || code == null || newPassword == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请提供邮箱、验证码和新密码"));
        }
        
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请输入正确的邮箱"));
        }
        
        if (!smsService.verifyResetPasswordEmailCode(email, code)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("验证码不正确或已过期"));
        }
        
        boolean success = userService.resetPasswordByEmail(email, newPassword);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("密码重置成功"));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("密码重置失败"));
        }
    }

    @PostMapping("/phone/code")
    @ApiOperation(value = "发送手机验证码", notes = "向指定手机号发送验证码")
    public ResponseEntity<?> sendPhoneCode(@RequestBody Map<String, String> data) {
        String phone = data.get("phone");
        
        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请提供手机号"));
        }
        
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请输入正确的手机号"));
        }
        
        smsService.sendPhoneCode(phone);
        return ResponseEntity.ok(ApiResponse.success("验证码已发送"));
    }

    @PutMapping("/phone")
    @ApiOperation(value = "修改手机号", notes = "通过验证码验证后修改手机号")
    public ResponseEntity<?> updatePhone(
            @RequestBody Map<String, String> data,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        String phone = data.get("phone");
        String code = data.get("code");
        
        if (phone == null || code == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请提供手机号和验证码"));
        }
        
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请输入正确的手机号"));
        }
        
        if (!smsService.verifyPhoneCode(phone, code)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("验证码不正确或已过期"));
        }
        
        User user = userService.selectById(userDetails.getId());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        user.setPhone(phone);
        userService.update(user);
        
        return ResponseEntity.ok(ApiResponse.success("手机号修改成功"));
    }

    @PostMapping("/email/code")
    @ApiOperation(value = "发送邮箱验证码", notes = "向指定邮箱发送验证码")
    public ResponseEntity<?> sendEmailCode(@RequestBody Map<String, String> data) {
        String email = data.get("email");
        
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请提供邮箱"));
        }
        
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请输入正确的邮箱"));
        }
        
        smsService.sendEmailCode(email);
        return ResponseEntity.ok(ApiResponse.success("验证码已发送"));
    }

    @PutMapping("/email")
    @ApiOperation(value = "修改邮箱", notes = "通过验证码验证后修改邮箱")
    public ResponseEntity<?> updateEmail(
            @RequestBody Map<String, String> data,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        String email = data.get("email");
        String code = data.get("code");
        
        if (email == null || code == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请提供邮箱和验证码"));
        }
        
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请输入正确的邮箱"));
        }
        
        if (!smsService.verifyEmailCode(email, code)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("验证码不正确或已过期"));
        }
        
        User user = userService.selectById(userDetails.getId());
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        user.setEmail(email);
        userService.update(user);
        
        return ResponseEntity.ok(ApiResponse.success("邮箱修改成功"));
    }
    
    @PostMapping("/register/email/code")
    @ApiOperation(value = "发送注册邮箱验证码", notes = "向指定邮箱发送注册验证码")
    public ResponseEntity<?> sendRegisterEmailCode(@RequestBody Map<String, String> data) {
        String email = data.get("email");
        
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请提供邮箱"));
        }
        
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("请输入正确的邮箱"));
        }
        
        int count = userService.countByEmail(email);
        if (count >= 3) {
            return ResponseEntity.badRequest().body(ApiResponse.error("该邮箱已注册超过3个账号，无法继续注册"));
        }
        
        smsService.sendRegisterEmailCode(email);
        return ResponseEntity.ok(ApiResponse.success("验证码已发送"));
    }
    
    @GetMapping("/login-records")
    @ApiOperation(value = "获取当前用户登录记录", notes = "获取当前登录用户的登录历史记录，包括登录时间、IP地址、地点等信息")
    public ResponseEntity<?> getLoginRecords(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        List<LoginRecord> records = loginRecordService.getLoginRecords(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("获取登录记录成功", records));
    }
    
    @GetMapping("/login-records/recent")
    @ApiOperation(value = "获取最近登录记录", notes = "获取当前登录用户最近的登录记录")
    public ResponseEntity<?> getRecentLoginRecords(
            Authentication authentication,
            @RequestParam(defaultValue = "10") Integer limit) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        List<LoginRecord> records = loginRecordService.getRecentLoginRecords(userDetails.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success("获取最近登录记录成功", records));
    }
}
