package com.example.storesports.service.admin.customer.impl;

import com.example.storesports.core.admin.customer.payload.CustomerResponse;
import com.example.storesports.entity.Address;
import com.example.storesports.entity.User;
import com.example.storesports.infrastructure.constant.Role;
import com.example.storesports.repositories.AddressRepository;
import com.example.storesports.repositories.UserAddressMappingRepository;
import com.example.storesports.repositories.UserRepository;
import com.example.storesports.service.admin.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;

    private final AddressRepository addressRepository;

    private final UserAddressMappingRepository userAddressMappingRepository;

    private final ModelMapper modelMapper;

    @Override
    public List<CustomerResponse> getAll() {
        List<User> users = userRepository.findAllWithAddresses();
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Override
    public List<CustomerResponse> getCustomersNotReceivedCoupon(Long couponId) {
        List<User> users = userRepository.findCustomersNotReceivedCoupon(Role.CUSTOMER, couponId);
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CustomerResponse mapToResponse(User user) {
        CustomerResponse response = new CustomerResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setActive(Boolean.FALSE.equals(user.getIsActive()));
        response.setRole(user.getRole().toString());
        response.setGender(user.getGender() != null ? user.getGender().name() : null);
        if (user.getUserAddressMappings() != null && !user.getUserAddressMappings().isEmpty()) {
            Address address = user.getUserAddressMappings().get(0).getAddress();
            if (address != null) {
                response.setAddressStreet(address.getStreet());
                response.setAddressWard(address.getWard());             // Phường
                response.setAddressDistrict(address.getDistrict());     // Quận/Huyện
                response.setAddressProvince(address.getProvince());     // Tỉnh/Thành phố
                response.setAddressCity(address.getCity());
                response.setAddressState(address.getState());
                response.setAddressCountry(address.getCountry());
                response.setAddressZipcode(address.getZipcode());
            }
        }
        return response;
    }


}
