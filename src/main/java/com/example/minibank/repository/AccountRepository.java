package com.example.minibank.repository;

import com.example.minibank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findAccountByCode(String code);
    Optional<Account> findAccountByCustomerId(Integer id);

}
