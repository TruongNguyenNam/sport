package com.example.storesports.repositories;


import com.example.storesports.entity.Token;
import com.example.storesports.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface TokenRepository extends JpaRepository<Token,Long> {

    @Modifying
    void deleteByUser(User user);

    Token findByKeyAndType(String key, Token.Type type);
}
