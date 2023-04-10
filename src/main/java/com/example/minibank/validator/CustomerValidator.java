package com.example.minibank.validator;

import com.example.minibank.exception.CustomerIneligibleException;
import com.example.minibank.model.Customer;
import com.example.minibank.service.CustomerService;
import org.springframework.stereotype.Component;

@Component
public class CustomerValidator {

    public static void validateAge(Customer customer) {
        if (customer.getAge() < CustomerService.CUSTOMER_MIN_AGE_REQUIRED) {
            throw new CustomerIneligibleException("Customer age must be above " + CustomerService.CUSTOMER_MIN_AGE_REQUIRED);
        }
    }
}
