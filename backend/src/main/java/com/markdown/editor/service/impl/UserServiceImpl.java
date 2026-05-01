package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.markdown.editor.entity.User;
import com.markdown.editor.mapper.UserMapper;
import com.markdown.editor.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public User selectById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User selectByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public User selectByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    @Override
    public User selectByNickname(String nickname) {
        return userMapper.selectByNickname(nickname);
    }

    @Override
    public int insert(User user) {
        String avatar = user.getAvatar();
        boolean hasAvatar = avatar != null && !avatar.isEmpty();
        
        if (!hasAvatar) {
            int result = userMapper.insert(user);
            if (result > 0) {
                Long id = user.getId();
                if (id != null) {
                    String defaultAvatar = "https://api.dicebear.com/7.x/pixel-art/svg?seed=" + id;
                    user.setAvatar(defaultAvatar);
                    userMapper.updateById(user);
                }
            }
            return result;
        }
        return userMapper.insert(user);
    }

    @Override
    public int update(User user) {
        return userMapper.updateById(user);
    }

    @Override
    public int deleteById(Long id) {
        return userMapper.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return userMapper.exists(wrapper);
    }

    @Override
    public boolean existsByEmail(String email) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        return userMapper.exists(wrapper);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("nickname", nickname);
        return userMapper.exists(wrapper);
    }

    @Override
    public List<User> selectAll() {
        return userMapper.selectList(null);
    }

    @Override
    public List<User> searchUsers(String keyword) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("username", keyword).or().like("nickname", keyword);
        return userMapper.selectList(wrapper);
    }

    @Override
    public int updateAvatar(Long userId, String avatar) {
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatar);
        return userMapper.updateById(user);
    }

    @Override
    public String getAvatar(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            String avatar = user.getAvatar();
            if (avatar != null && !avatar.isEmpty()) {
                return avatar;
            }
        }
        return "https://api.dicebear.com/7.x/pixel-art/svg?seed=" + userId;
    }
}
