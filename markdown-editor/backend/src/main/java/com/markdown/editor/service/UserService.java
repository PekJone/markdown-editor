package com.markdown.editor.service;

import com.markdown.editor.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    User selectById(Long id);
    User selectByUsername(String username);
    User selectByEmail(String email);
    User selectByNickname(String nickname);
    java.util.List<User> selectAll();
    java.util.List<User> searchUsers(String keyword);
    int insert(User user);
    int update(User user);
    int updateAvatar(Long userId, String avatar);
    String uploadAvatar(Long userId, MultipartFile file);
    String getAvatar(Long userId);
    int deleteById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    int countByEmail(String email);
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
    boolean resetPasswordByEmail(String email, String newPassword);
}
