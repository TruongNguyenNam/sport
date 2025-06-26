package com.example.storesports.service.admin.customer.impl;

import com.example.storesports.core.admin.customer.payload.CustomerRequest;
import com.example.storesports.core.admin.customer.payload.CustomerResponse;
import com.example.storesports.core.admin.address.payload.AddressRequest;
import com.example.storesports.entity.Address;
import com.example.storesports.entity.User;
import com.example.storesports.entity.UserAddressMapping;
import com.example.storesports.infrastructure.constant.Role;
import com.example.storesports.repositories.AddressRepository;
import com.example.storesports.repositories.UserAddressMappingRepository;
import com.example.storesports.repositories.UserRepository;
import com.example.storesports.service.admin.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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

    // Tạo mới khách hàng
    @Transactional
    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        // Validate định dạng
        validateEmailAndPhone(request.getEmail(), request.getPhoneNumber());
        // Kiểm tra trùng email/sđt
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }

        // Tạo khách hàng
        User user = modelMapper.map(request, User.class);
        user.setRole(Role.CUSTOMER);
        user.setIsActive(true);
        user.setDeleted(false);
        user = userRepository.save(user);

        // Tạo địa chỉ
        Address address = modelMapper.map(request.getAddress(), Address.class);
        address.setDeleted(false);
        address = addressRepository.save(address);

        // Mapping khách hàng và địa chỉ
        UserAddressMapping mapping = new UserAddressMapping();
        mapping.setUser(user);
        mapping.setAddress(address);
        mapping.setDeleted(false);
        userAddressMappingRepository.save(mapping);

        return mapToResponse(user);
    }

    // Cập nhật
    @Transactional
    @Override
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Kiểm tra định dạng email và số điện thoại
        validateEmailAndPhone(request.getEmail(), request.getPhoneNumber());

        // Kiểm tra trùng email với user khác
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        // Kiểm tra trùng sđt với user khác
        if (!user.getPhoneNumber().equals(request.getPhoneNumber()) && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }

        modelMapper.map(request, user);
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);

        AddressRequest addrReq = request.getAddress();
        if (addrReq != null) {
            if (user.getUserAddressMappings() != null && !user.getUserAddressMappings().isEmpty()) {
                // cập nhật address cũ
                UserAddressMapping mapping = user.getUserAddressMappings().get(0);
                Address address = mapping.getAddress();
                if (address != null) {
                    modelMapper.map(addrReq, address);
                    addressRepository.save(address);
                }
            } else {
                //  tạo mới Address
                Address address = modelMapper.map(addrReq, Address.class);
                address.setDeleted(false);
                address = addressRepository.save(address);
                UserAddressMapping mapping = new UserAddressMapping();
                mapping.setUser(user);
                mapping.setAddress(address);
                mapping.setDeleted(false);
                userAddressMappingRepository.save(mapping);
                if (user.getUserAddressMappings() != null) {
                    user.getUserAddressMappings().add(mapping);
                }
            }
        }
        return mapToResponse(user);
    }


    @Override
    public CustomerResponse getCustomerById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }

// tìm kiếm theo nhiều trường
    @Override
    public List<CustomerResponse> searchCustomer(String keyword) {
        List<User> users;
        if (keyword.contains("@")) { // kiem tra key cos phai email ko va tim kiem
            users = userRepository.findByEmailOrderByIdDesc(keyword);
        } else if (keyword.matches("\\d+")) // neu key chi chua so
        {
            users = userRepository.findByPhoneNumberOrderByIdDesc(keyword);
        } else {
            users = userRepository.findByUsernameContainingIgnoreCaseOrderByIdDesc(keyword);
        }
        return users.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // regex email và số đt
    private void validateEmailAndPhone(String email, String phoneNumber) {
        String emailRegex = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
        String phoneRegex = "^(0|\\+84)[1-9][0-9]{8}$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
        if (!phoneNumber.matches(phoneRegex)) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ");
        }
    }

    private CustomerResponse mapToResponse(User user) {
        CustomerResponse response = new CustomerResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setActive(user.getIsActive());
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
