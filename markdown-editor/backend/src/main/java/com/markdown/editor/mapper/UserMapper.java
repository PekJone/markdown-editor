package com.markdown.editor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.markdown.editor.entity.User;

public interface UserMapper extends BaseMapper<User> {
    User selectByUsername(String username);
    User selectByEmail(String email);
    User selectByNickname(String nickname);
}
