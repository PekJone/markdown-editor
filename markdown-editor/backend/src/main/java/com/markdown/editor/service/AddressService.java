package com.markdown.editor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.markdown.editor.entity.Address;

import java.util.List;

public interface AddressService {
    List<Address> getAddressesByUserId(Long userId);
    
    IPage<Address> getAddressesByUserIdPage(Long userId, int page, int size);
    
    Address getDefaultAddress(Long userId);
    
    Address getAddressById(Long id);
    
    Address createAddress(Address address);
    
    Address updateAddress(Long id, Address address);
    
    void deleteAddress(Long id);
    
    void setDefaultAddress(Long userId, Long addressId);
}