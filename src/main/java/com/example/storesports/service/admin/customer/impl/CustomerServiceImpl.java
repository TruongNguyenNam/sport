package com.example.storesports.service.admin.customer.impl;

import com.example.storesports.core.admin.address.payload.AddressResponse;
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
import com.example.storesports.service.admin.address.AddressService;
import com.example.storesports.service.admin.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.apache.commons.lang3.RandomStringUtils; // tạo mật khẩu ngẫu nhiên
import com.example.storesports.infrastructure.email.EmailService; // service gửi email


import java.security.SecureRandom;
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
    private final AddressService addressService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


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

        //.Tạo mật khẩu
        String rawPassword = generateRandomPassword(5);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        // Tạo khách hàng
        User user = modelMapper.map(request, User.class);
        user.setRole(Role.CUSTOMER);
        user.setIsActive(true);
        user.setDeleted(false);
        user.setPassword(encodedPassword); // GÁN MẬT KHẨU VÀO USER
        user = userRepository.save(user);

        // gửi email thông tin tài khoản
        emailService.sendAccountInfo(user.getEmail(), user.getUsername(), rawPassword);

        // Tạo địa chỉ
        Address address = modelMapper.map(request.getAddress(), Address.class);
        address.setDeleted(false);
        address = addressRepository.save(address);

        // Mapping khách hàng và địa chỉ
        UserAddressMapping mapping = new UserAddressMapping();
        mapping.setUser(user);
        mapping.setAddress(address);
        mapping.setDeleted(false);
        mapping.setReceiverName(request.getAddress().getReceiverName());
        mapping.setReceiverPhone(request.getAddress().getReceiverPhone());
        mapping.setIsDefault(request.getAddress().getIsDefault() != null ? request.getAddress().getIsDefault() : true);
        userAddressMappingRepository.save(mapping);

        return mapToResponse(user);
    }

    // Cập nhật khách hàng
    @Transactional
    @Override
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Kiểm tra định dạng email và số điện thoại
        validateEmailAndPhone(request.getEmail(), request.getPhoneNumber());

        // Kiểm tra trùng email với user khác
        if (request.getEmail() != null && !user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        // Kiểm tra trùng sđt với user khác
        if (request.getPhoneNumber() != null
                && !request.getPhoneNumber().equals(user.getPhoneNumber())
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại");
        }

        modelMapper.map(request, user);
        userRepository.save(user);

        return mapToResponse(user);
    }

    //them dia chi moi
    @Override
    @Transactional
    public AddressResponse addAddressForUser(Long userId, AddressRequest addressRequest) {
        return addressService.addAddressToCustomer(userId, addressRequest);
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
        // Map danh sách địa chỉ
        if (user.getUserAddressMappings() != null && !user.getUserAddressMappings().isEmpty()) {
            List<AddressResponse> addresses = user.getUserAddressMappings().stream()
                    .filter(mapping -> Boolean.FALSE.equals(mapping.getDeleted()))
                    .map(mapping -> {
                        Address address = mapping.getAddress();
                        AddressResponse addrRes = new AddressResponse();
                        addrRes.setId(address.getId());
                        addrRes.setStreet(address.getStreet());
                        addrRes.setWard(address.getWard());
                        addrRes.setDistrict(address.getDistrict());
                        addrRes.setProvince(address.getProvince());
                        addrRes.setCity(address.getCity());
                        addrRes.setState(address.getState());
                        addrRes.setCountry(address.getCountry());
                        addrRes.setZipcode(address.getZipcode());
                        addrRes.setIsDefault(mapping.getIsDefault());
                        addrRes.setReceiverName(mapping.getReceiverName());
                        addrRes.setReceiverPhone(mapping.getReceiverPhone());
                        return addrRes;
                    })
                    .collect(Collectors.toList());
            response.setAddresses(addresses);
        }
        return response;
    }
    // mat khau ngau nhien
    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$!";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }


}
