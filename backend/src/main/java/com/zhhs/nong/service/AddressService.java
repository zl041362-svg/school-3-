package com.zhhs.nong.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhhs.nong.common.PageUtils;
import com.zhhs.nong.common.exception.BizException;
import com.zhhs.nong.dto.trade.SaveAddressRequest;
import com.zhhs.nong.mapper.AddressMapper;
import com.zhhs.nong.model.Address;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class AddressService {

    private final AddressMapper addressMapper;

    public AddressService(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAddresses(Long userId) {
        List<Address> addresses = addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getId));
        return listResponse(addresses);
    }

    @Transactional
    public Address saveAddress(Long userId, Long id, SaveAddressRequest request) {
        Address address;
        boolean exists = id != null;
        Address existing = null;
        if (exists) {
            existing = requireAddress(userId, id);
            address = existing;
            address.setReceiver(request.receiver());
            address.setPhone(request.phone());
            address.setAddress(request.address());
            address.setUpdatedAt(LocalDateTime.now());
        } else {
            address = new Address();
            address.setUserId(userId);
            address.setReceiver(request.receiver());
            address.setPhone(request.phone());
            address.setAddress(request.address());
            address.setCreatedAt(LocalDateTime.now());
            address.setUpdatedAt(LocalDateTime.now());
        }

        boolean currentDefault = request.isDefault() != null
                ? request.isDefault()
                : exists && existing != null && existing.getIsDefault() != null && existing.getIsDefault() == 1;
        if (!exists && count(userId) == 0) {
            currentDefault = true;
        }

        if (currentDefault) {
            clearDefaults(userId);
            address.setIsDefault(1);
        } else if (exists) {
            address.setIsDefault(0);
        } else {
            address.setIsDefault(0);
        }

        if (exists) {
            addressMapper.updateById(address);
        } else {
            addressMapper.insert(address);
        }

        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearDefaults(userId, address.getId());
        }
        return address;
    }

    @Transactional
    public void removeAddress(Long userId, Long id) {
        Address removed = requireAddress(userId, id);
        int deleted = addressMapper.deleteById(id);
        if (deleted == 0) {
            throw new BizException(4044, "address not found");
        }
        if (removed.getIsDefault() != null && removed.getIsDefault() == 1) {
            Address next = addressMapper.selectList(new LambdaQueryWrapper<Address>()
                            .eq(Address::getUserId, userId)
                            .orderByDesc(Address::getId))
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (next != null) {
                next.setIsDefault(1);
                next.setUpdatedAt(LocalDateTime.now());
                addressMapper.updateById(next);
            }
        }
    }

    private Address requireAddress(Long userId, Long id) {
        Address address = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, id)
                .eq(Address::getUserId, userId));
        if (address == null) {
            throw new BizException(4044, "address not found");
        }
        return address;
    }

    private long count(Long userId) {
        return addressMapper.selectCount(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
    }

    private void clearDefaults(Long userId) {
        Address addr = new Address();
        addr.setIsDefault(0);
        addr.setUpdatedAt(LocalDateTime.now());
        addressMapper.update(addr, new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1));
    }

    private void clearDefaults(Long userId, Long exceptId) {
        Address addr = new Address();
        addr.setIsDefault(0);
        addr.setUpdatedAt(LocalDateTime.now());
        addressMapper.update(addr, new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .ne(Address::getId, exceptId)
                .eq(Address::getIsDefault, 1));
    }

    private Map<String, Object> listResponse(List<Address> items) {
        List<Address> sorted = new ArrayList<>(items);
        sorted.sort(Comparator.comparing(Address::getIsDefault, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(Address::getId, Comparator.reverseOrder()));
        return PageUtils.listResponse(sorted);
    }
}


