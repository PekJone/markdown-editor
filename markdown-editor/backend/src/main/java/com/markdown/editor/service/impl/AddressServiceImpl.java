package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markdown.editor.entity.Address;
import com.markdown.editor.mapper.AddressMapper;
import com.markdown.editor.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Resource
    private AddressMapper addressMapper;

    @Override
    public List<Address> getAddressesByUserId(Long userId) {
        return addressMapper.selectByUserId(userId);
    }

    @Override
    public IPage<Address> getAddressesByUserIdPage(Long userId, int page, int size) {
        Page<Address> pageParam = new Page<>(page, size);
        return addressMapper.selectByUserIdPage(pageParam, userId);
    }

    @Override
    public Address getDefaultAddress(Long userId) {
        return addressMapper.selectDefaultAddress(userId);
    }

    @Override
    public Address getAddressById(Long id) {
        return addressMapper.selectById(id);
    }

    @Override
    @Transactional
    public Address createAddress(Address address) {
        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.updateDefaultByUserId(address.getUserId(), 0);
        } else if (address.getIsDefault() == null) {
            // 如果没有设置默认，检查是否是第一个地址，如果是则设为默认
            List<Address> existingAddresses = addressMapper.selectByUserId(address.getUserId());
            if (existingAddresses == null || existingAddresses.isEmpty()) {
                address.setIsDefault(1);
            } else {
                address.setIsDefault(0);
            }
        }
        
        addressMapper.insert(address);
        return address;
    }

    @Override
    @Transactional
    public Address updateAddress(Long id, Address address) {
        Address existingAddress = addressMapper.selectById(id);
        if (existingAddress == null) {
            return null;
        }

        // 校验归属
        if (!existingAddress.getUserId().equals(address.getUserId())) {
            return null;
        }

        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.updateDefaultByUserId(address.getUserId(), 0);
        }

        existingAddress.setName(address.getName());
        existingAddress.setPhone(address.getPhone());
        existingAddress.setProvince(address.getProvince());
        existingAddress.setCity(address.getCity());
        existingAddress.setDistrict(address.getDistrict());
        existingAddress.setDetail(address.getDetail());
        existingAddress.setIsDefault(address.getIsDefault());

        addressMapper.updateById(existingAddress);
        return existingAddress;
    }

    @Override
    @Transactional
    public void deleteAddress(Long id) {
        addressMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        // 先取消所有默认地址
        addressMapper.updateDefaultByUserId(userId, 0);
        
        // 设置指定地址为默认
        Address address = addressMapper.selectById(addressId);
        if (address != null && address.getUserId().equals(userId)) {
            address.setIsDefault(1);
            addressMapper.updateById(address);
        }
    }
}