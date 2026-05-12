package com.markdown.editor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.markdown.editor.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AddressMapper extends BaseMapper<Address> {
    List<Address> selectByUserId(@Param("userId") Long userId);
    
    IPage<Address> selectByUserIdPage(IPage<Address> page, @Param("userId") Long userId);
    
    Address selectDefaultAddress(@Param("userId") Long userId);
    
    int updateDefaultByUserId(@Param("userId") Long userId, @Param("isDefault") Integer isDefault);
}