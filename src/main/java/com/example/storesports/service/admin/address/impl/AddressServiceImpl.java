package com.example.storesports.service.admin.address.impl;

import com.example.storesports.core.admin.address.payload.AddressRequest;
import com.example.storesports.core.admin.address.payload.AddressResponse;
import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import com.example.storesports.entity.Address;
import com.example.storesports.entity.ProductAttribute;
import com.example.storesports.entity.User;
import com.example.storesports.entity.UserAddressMapping;
import com.example.storesports.repositories.AddressRepository;
import com.example.storesports.repositories.UserAddressMappingRepository;
import com.example.storesports.repositories.UserRepository;
import com.example.storesports.service.admin.address.AddressService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    private final UserRepository userRepository;

    private final UserAddressMappingRepository userAddressMappingRepository;

    private final ModelMapper modelMapper;

    @Override
    public List<AddressResponse> getAll() {
        List<Address> addresses = addressRepository.findAll();
        if (addresses.isEmpty()) {
            throw new IllegalArgumentException("thuộc tính bị trống" + addresses);
        }
        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressResponse.class)).collect(Collectors.toList());
    }

// them dia chi
    @Override
    @Transactional
    public AddressResponse addAddressToCustomer(Long customerId, AddressRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // Nếu là địa chỉ mặc định thì reset các mapping cũ về isDefault = false
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            List<UserAddressMapping> existingMappings = userAddressMappingRepository.findByUserId(customerId);
            for (UserAddressMapping mapping : existingMappings) {
                mapping.setIsDefault(false);
            }
            userAddressMappingRepository.saveAll(existingMappings); // luu
        }

        // tạo  Address
        Address address = modelMapper.map(request, Address.class);
        Address savedAddress = addressRepository.save(address);

        //  mapping dia chi
        UserAddressMapping mapping = new UserAddressMapping();
        mapping.setUser(customer);
        mapping.setAddress(savedAddress);
        mapping.setReceiverName(request.getReceiverName());
        mapping.setReceiverPhone(request.getReceiverPhone());
        mapping.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        mapping.setDeleted(false);

        userAddressMappingRepository.save(mapping);

        // map mappingAddress
        AddressResponse response = modelMapper.map(savedAddress, AddressResponse.class);
        response.setReceiverName(mapping.getReceiverName());
        response.setReceiverPhone(mapping.getReceiverPhone());
        response.setIsDefault(mapping.getIsDefault());

        return response;
    }

    //cap nhat dia chi
    @Override
    @Transactional
    public AddressResponse updateAddressForCustomer(Long customerId, Long addressId, AddressRequest request) {
        // Kiểm tra khách hàng có tồn tại không
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // Kiểm tra địa chỉ có tồn tại không
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));

        // Tìm mapping giữa user và address
        UserAddressMapping mapping = userAddressMappingRepository.findByUserIdAndAddressId(customerId, addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mối quan hệ địa chỉ của khách hàng"));

        // Nếu là địa chỉ mặc định thì cập nhật các địa chỉ khác thành false
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            List<UserAddressMapping> existingMappings = userAddressMappingRepository.findByUserId(customerId);
            for (UserAddressMapping m : existingMappings) {
                m.setIsDefault(false);
            }
            userAddressMappingRepository.saveAll(existingMappings);
        }

        // Cập nhật thông tin địa chỉ
        address.setStreet(request.getStreet());
        address.setWard(request.getWard());
        address.setDistrict(request.getDistrict());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setZipcode(request.getZipcode());

        Address updatedAddress = addressRepository.save(address);

        // Cập nhật thông tin mapping (người nhận, sđt, isDefault)
        mapping.setReceiverName(request.getReceiverName());
        mapping.setReceiverPhone(request.getReceiverPhone());
        mapping.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        userAddressMappingRepository.save(mapping);

        // Tạo response
        AddressResponse response = modelMapper.map(updatedAddress, AddressResponse.class);
        response.setReceiverName(mapping.getReceiverName());
        response.setReceiverPhone(mapping.getReceiverPhone());
        response.setIsDefault(mapping.getIsDefault());

        return response;
    }
    // xoa mem dia chi
    @Override
    @Transactional
    public AddressResponse softDeleteAddressForCustomer(Long customerId, Long addressId) {
        UserAddressMapping mapping = userAddressMappingRepository.findByUserIdAndAddressId(customerId, addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ của khách hàng"));

        if (Boolean.TRUE.equals(mapping.getIsDefault())) {
            throw new RuntimeException("Không thể xoá địa chỉ mặc định");
        }

        mapping.setDeleted(true);
        userAddressMappingRepository.save(mapping);
        // Trả về DTO
        AddressResponse response = modelMapper.map(mapping.getAddress(), AddressResponse.class);
        response.setReceiverName(mapping.getReceiverName());
        response.setReceiverPhone(mapping.getReceiverPhone());
        response.setIsDefault(mapping.getIsDefault());
        return response;
    }


}
