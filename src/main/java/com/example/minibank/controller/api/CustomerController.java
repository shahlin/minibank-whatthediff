package com.example.minibank.controller.api;

import com.example.minibank.service.CustomerService;
import com.example.minibank.model.Account;
import com.example.minibank.model.Customer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/customers")
@Tag(name = "Customer API", description = "Customer related APIs. Read, add, update and open new account for customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customerList = customerService.getAllCustomers();

        return new ResponseEntity<>(customerList, HttpStatus.OK);
    }

    @GetMapping(path = "{code}")
    public ResponseEntity<Customer> getCustomer(@PathVariable("code") String code) {
        Customer customer = customerService.getCustomer(code);

        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        Customer newlyAddedCustomer = customerService.createCustomer(customer);

        return new ResponseEntity<>(newlyAddedCustomer, HttpStatus.OK);
    }

    @PutMapping(path = "{code}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable("code") String code, @Valid @RequestBody Customer customer) {
        Customer updatedCustomer = customerService.updateCustomer(code, customer);

        return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
    }

    @PostMapping(path = "{code}/accounts")
    public ResponseEntity<?> openNewAccount(@PathVariable("code") String code) {
        Account newAccount = customerService.openNewAccount(code);

        return new ResponseEntity<>(newAccount, HttpStatus.OK);
    }
}
