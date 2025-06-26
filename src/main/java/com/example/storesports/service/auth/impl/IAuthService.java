package com.example.storesports.service.auth.impl;


import com.example.storesports.core.auth.payload.UpdateUserForm;
import com.example.storesports.entity.Address;
import com.example.storesports.entity.UserAddressMapping;
import com.example.storesports.infrastructure.constant.Gender;
import com.example.storesports.infrastructure.constant.Role;
import com.example.storesports.core.auth.payload.LoginInfoDto;
import com.example.storesports.core.auth.payload.RegisterForm;
import com.example.storesports.core.auth.payload.UserResponse;
import com.example.storesports.entity.Token;
import com.example.storesports.entity.User;
import com.example.storesports.repositories.AddressRepository;
import com.example.storesports.repositories.UserAddressMappingRepository;
import com.example.storesports.repositories.UserRepository;
import com.example.storesports.service.auth.AuthService;
import com.example.storesports.service.auth.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.internal.bytebuddy.implementation.bytecode.Throw;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IAuthService implements AuthService {

    private final UserService service;


    private final ModelMapper modelMapper;


    private final UserRepository userRepository;

    private final AddressRepository addressRepository;

    private final UserAddressMappingRepository userAddressMappingRepository;


    private final IJWTTokenService ijwtTokenService;

    private final PasswordEncoder passwordEncoder;
    @Override
    @Transactional
    public LoginInfoDto login(String username) {
        User entity = service.getAccountByUsername(username);

        LoginInfoDto dto = mapToLoginInfoDto(entity);

        dto.setToken(ijwtTokenService.generateJWT(entity.getUsername()));

        Token token = ijwtTokenService.generateRefreshToken(entity);
        dto.setRefreshToken(token.getKey());

        return dto;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterForm registerForm) {
        // Kiểm tra username và email đã tồn tại
        if (userRepository.existsByUsername(registerForm.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại.");
        }
        if (userRepository.existsByEmail(registerForm.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }

        // Tạo và lưu user
        User user = new User();
        user.setUsername(registerForm.getUsername());
        user.setPassword(passwordEncoder.encode(registerForm.getPassword()));
        user.setEmail(registerForm.getEmail());
        if (registerForm.getGender() != null && !registerForm.getGender().isEmpty()) {
            try {
                user.setGender(Gender.valueOf(registerForm.getGender()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Giới tính không hợp lệ.");
            }
        }
        user.setRole(Role.CUSTOMER);
        user.setDeleted(false);
        User savedUser = userRepository.save(user);

        // Ánh xạ sang UserResponse
        UserResponse userResponse = mapToResponse(savedUser);
        userResponse.setMessage("Đăng ký thành công.");
        return userResponse;
    }


    @Override
    @Transactional
    public UserResponse updateAddress(Long userId, UpdateUserForm userForm) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        // Cập nhật các trường phoneNumber và gender nếu được cung cấp
        if (userForm.getPhoneNumber() != null && !userForm.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(userForm.getPhoneNumber());
        }

        if (userForm.getGender() != null && !userForm.getGender().isEmpty()) {
            try {
                user.setGender(Gender.valueOf(userForm.getGender()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Giới tính không hợp lệ.");
            }
        }

        if (userForm.getAddress() != null) {
            Address address = new Address();
            address.setStreet(userForm.getAddress().getAddressStreet());
            address.setCity(userForm.getAddress().getAddressCity());
            address.setCountry(userForm.getAddress().getAddressCountry());
            address.setWard(userForm.getAddress().getAddressWard());
            address.setProvince(userForm.getAddress().getAddressProvince());
            address.setDistrict(userForm.getAddress().getAddressDistrict());
            address.setState(userForm.getAddress().getAddressState());
            address.setZipcode(userForm.getAddress().getAddressZipcode());
            address.setDeleted(false);
            Address savedAddress = addressRepository.save(address);

            // Xóa hẳn các UserAddressMapping cũ (nếu có)
            if (user.getUserAddressMappings() != null) {
                user.getUserAddressMappings().stream()
                        .filter(m -> !m.getDeleted())
                        .forEach(m -> userAddressMappingRepository.delete(m));
            }

            // Tạo mapping mới
            UserAddressMapping newMapping = new UserAddressMapping();
            newMapping.setUser(user);
            newMapping.setAddress(savedAddress);
            newMapping.setDeleted(false);
            userAddressMappingRepository.save(newMapping);
        }

        // Lưu user với các thông tin đã cập nhật
       User savedUser = userRepository.save(user);

        // Trả về thông tin user đã cập nhật
        UserResponse userResponse = mapToResponse(savedUser);
        userResponse.setMessage("Cập nhật thông tin người dùng thành công.");
        return userResponse;
    }

    @Override
    @Transactional
    public UserResponse finById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("không tìm thấy id này"));

        return mapToResponse(user);
    }

    private LoginInfoDto mapToLoginInfoDto(User user) {
        LoginInfoDto dto = new LoginInfoDto();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword()); // Không trả password ra client
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setGender(user.getGender() != null ? user.getGender().name() : null);
        dto.setActive(Boolean.TRUE.equals(user.getIsActive()));

        // Ánh xạ địa chỉ (lấy địa chỉ đầu tiên chưa bị xóa mềm)
        if (user.getUserAddressMappings() != null && !user.getUserAddressMappings().isEmpty()) {
            // Tìm địa chỉ đầu tiên chưa bị xóa mềm
            UserAddressMapping mapping = user.getUserAddressMappings().stream()
                    .filter(m -> !m.getDeleted() && !m.getAddress().getDeleted())
                    .findFirst()
                    .orElse(null);

            if (mapping != null) {
                Address address = mapping.getAddress();
                LoginInfoDto.UserAddress userAddress = new LoginInfoDto.UserAddress();
                userAddress.setId(address.getId());
                userAddress.setAddressStreet(address.getStreet());
                userAddress.setAddressWard(address.getWard());
                userAddress.setAddressCity(address.getCity());
                userAddress.setAddressState(address.getState());
                userAddress.setAddressCountry(address.getCountry());
                userAddress.setAddressZipcode(address.getZipcode());
                userAddress.setAddressDistrict(address.getDistrict());
                userAddress.setAddressProvince(address.getProvince());

                dto.setAddress(userAddress);
            }
        }

        return dto;
    }

    private UserResponse mapToResponse(User user){
        UserResponse dto = new UserResponse();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword()); // Không trả password ra client
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setGender(user.getGender() != null ? user.getGender().name() : null);
        dto.setActive(Boolean.TRUE.equals(user.getIsActive()));

        // Ánh xạ địa chỉ (lấy địa chỉ đầu tiên chưa bị xóa mềm)
        if (user.getUserAddressMappings() != null && !user.getUserAddressMappings().isEmpty()) {
            // Tìm địa chỉ đầu tiên chưa bị xóa mềm
            UserAddressMapping mapping = user.getUserAddressMappings().stream()
                    .filter(m -> !m.getDeleted() && !m.getAddress().getDeleted())
                    .findFirst()
                    .orElse(null);

            if (mapping != null) {
                Address address = mapping.getAddress();
                UserResponse.UserAddress userAddress = new UserResponse.UserAddress();
                userAddress.setId(address.getId());
                userAddress.setAddressStreet(address.getStreet());
                userAddress.setAddressWard(address.getWard());
                userAddress.setAddressCity(address.getCity());
                userAddress.setAddressState(address.getState());
                userAddress.setAddressCountry(address.getCountry());
                userAddress.setAddressZipcode(address.getZipcode());
                userAddress.setAddressDistrict(address.getDistrict());
                userAddress.setAddressProvince(address.getProvince());

                dto.setAddress(userAddress);
            }
        }
        return dto;
    }


}
