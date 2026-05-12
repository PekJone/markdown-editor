package com.markdown.editor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.markdown.editor.entity.LoginRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LoginRecordMapper extends BaseMapper<LoginRecord> {
    
    @Select("SELECT * FROM login_records WHERE user_id = #{userId} ORDER BY login_time DESC LIMIT #{limit}")
    List<LoginRecord> findByUserIdOrderByLoginTimeDesc(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    @Select("SELECT * FROM login_records WHERE user_id = #{userId} ORDER BY login_time DESC")
    List<LoginRecord> findAllByUserIdOrderByLoginTimeDesc(@Param("userId") Long userId);
}