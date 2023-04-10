package com.example.minibank.repository;

import com.example.minibank.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findCustomerByCode(String code);
    Optional<Customer> findCustomerByEmail(String email);

    @Query("SELECT c FROM Customer c WHERE c.email = :email AND c.email NOT IN (:emailsToExclude)")
    Optional<Customer> findCustomerByEmailWithExcludeList(String email, List<String> emailsToExclude);
}
