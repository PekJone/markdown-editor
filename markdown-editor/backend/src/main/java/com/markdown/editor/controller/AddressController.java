package com.markdown.editor.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.markdown.editor.dto.ApiResponse;
import com.markdown.editor.entity.Address;
import com.markdown.editor.security.UserDetailsImpl;
import com.markdown.editor.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@Api(tags = "地址管理", description = "收货地址的增删改查接口")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    @ApiOperation(value = "获取当前用户的所有地址", notes = "获取当前登录用户的所有收货地址，支持分页")
    public ResponseEntity<?> getAddresses(Authentication authentication, 
                                         @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        if (page > 0 && size > 0) {
            IPage<Address> addressPage = addressService.getAddressesByUserIdPage(userDetails.getId(), page, size);
            Map<String, Object> result = new HashMap<>();
            result.put("content", addressPage.getRecords());
            result.put("totalElements", addressPage.getTotal());
            result.put("totalPages", addressPage.getPages());
            result.put("currentPage", addressPage.getCurrent());
            result.put("pageSize", addressPage.getSize());
            return ResponseEntity.ok(ApiResponse.success("获取地址列表成功", result));
        } else {
            List<Address> addresses = addressService.getAddressesByUserId(userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("获取地址列表成功", addresses));
        }
    }

    @GetMapping("/default")
    @ApiOperation(value = "获取默认地址", notes = "获取当前登录用户的默认收货地址")
    public ResponseEntity<?> getDefaultAddress(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Address address = addressService.getDefaultAddress(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("获取默认地址成功", address));
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取单个地址", notes = "根据地址ID获取地址详情")
    public ResponseEntity<?> getAddressById(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Address address = addressService.getAddressById(id);
        
        if (address == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 校验归属
        if (!address.getUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(ApiResponse.success("获取地址成功", address));
    }

    @PostMapping
    @ApiOperation(value = "创建新地址", notes = "创建一个新的收货地址")
    public ResponseEntity<?> createAddress(@RequestBody Address address, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        address.setUserId(userDetails.getId());
        
        Address createdAddress = addressService.createAddress(address);
        return ResponseEntity.ok(ApiResponse.success("创建地址成功", createdAddress));
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "更新地址", notes = "更新指定的收货地址")
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestBody Address address, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        address.setUserId(userDetails.getId());
        
        Address updatedAddress = addressService.updateAddress(id, address);
        
        if (updatedAddress == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(ApiResponse.success("更新地址成功", updatedAddress));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除地址", notes = "删除指定的收货地址")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Address address = addressService.getAddressById(id);
        if (address == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 校验归属
        if (!address.getUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success("删除地址成功"));
    }

    @PostMapping("/{id}/default")
    @ApiOperation(value = "设置默认地址", notes = "将指定地址设置为默认地址")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Address address = addressService.getAddressById(id);
        if (address == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 校验归属
        if (!address.getUserId().equals(userDetails.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        addressService.setDefaultAddress(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("设置默认地址成功"));
    }
}