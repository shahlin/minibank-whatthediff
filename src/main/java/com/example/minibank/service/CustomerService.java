package com.example.minibank.service;

import com.example.minibank.model.Account;
import com.example.minibank.model.Customer;
import com.example.minibank.repository.CustomerRepository;
import com.example.minibank.exception.CustomerNotFoundException;
import com.example.minibank.validator.CustomerValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AccountService accountService;

    public static final int CUSTOMER_MIN_AGE_REQUIRED = 18;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, AccountService accountService) {
        this.customerRepository = customerRepository;
        this.accountService = accountService;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomer(String code) {
        return customerRepository.findCustomerByCode(code)
                .orElseThrow(CustomerNotFoundException::new);
    }

    @Transactional(rollbackFor = Exception.class)
    public Customer createCustomer(Customer customer) {
        CustomerValidator.validateAge(customer);
        checkCustomerExistsWithEmail(customer, "");

        customer.setCode(generateCustomerCode());

        return customerRepository.save(customer);
    }

    @Transactional(rollbackFor = Exception.class)
    public Customer updateCustomer(String code, Customer customer) {
        Optional<Customer> customerOptional = customerRepository.findCustomerByCode(code);

        if (customerOptional.isEmpty()) {
            throw new CustomerNotFoundException();
        }

        Customer existingCustomer = customerOptional.get();

        CustomerValidator.validateAge(customer);
        checkCustomerExistsWithEmail(customer, existingCustomer.getEmail());

        existingCustomer.setName(customer.getName());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setUpdatedAt(LocalDateTime.now());

        return existingCustomer;
    }

    public Account openNewAccount(String code) {
        Optional<Customer> customerOptional = customerRepository.findCustomerByCode(code);

        if (customerOptional.isEmpty()) {
            throw new CustomerNotFoundException();
        }

        return accountService.openNewAccountForCustomer(customerOptional.get());
    }

    private void checkCustomerExistsWithEmail(Customer customer, String excludeEmail) {
        Optional<Customer> customerOptional = customerRepository.findCustomerByEmailWithExcludeList(
                customer.getEmail(),
                List.of(excludeEmail)
        );

        if (customerOptional.isPresent()) {
            throw new RuntimeException("Customer email is already taken");
        }
    }

    private String generateCustomerCode() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
