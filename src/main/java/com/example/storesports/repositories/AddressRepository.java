package com.example.storesports.repositories;


import com.example.storesports.entity.Address;
import com.example.storesports.entity.UserAddressMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long>, JpaSpecificationExecutor<Address> {

    //        Address findBy

}
