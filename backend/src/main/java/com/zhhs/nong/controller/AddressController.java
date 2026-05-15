package com.zhhs.nong.controller;

import com.zhhs.nong.dto.trade.SaveAddressRequest;
import com.zhhs.nong.model.Address;
import com.zhhs.nong.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public Map<String, Object> list(Authentication authentication) {
        return addressService.getAddresses(userId(authentication));
    }

    @PostMapping
    public Address create(Authentication authentication, @Valid @RequestBody SaveAddressRequest request) {
        return addressService.saveAddress(userId(authentication), null, request);
    }

    @PutMapping("/{id}")
    public Address update(Authentication authentication,
                          @PathVariable Long id,
                          @Valid @RequestBody SaveAddressRequest request) {
        return addressService.saveAddress(userId(authentication), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(Authentication authentication, @PathVariable Long id) {
        addressService.removeAddress(userId(authentication), id);
    }

    private Long userId(Authentication authentication) {
        return Long.parseLong(String.valueOf(authentication.getPrincipal()));
    }
}

