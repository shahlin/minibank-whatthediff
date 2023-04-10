package com.example.minibank.service;

import com.example.minibank.exception.CustomerIneligibleException;
import com.example.minibank.exception.CustomerNotFoundException;
import com.example.minibank.model.Customer;
import com.example.minibank.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private AccountService accountService;
    private CustomerService customerService;

    private int minimumRequiredDateOfBirthYear;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository, accountService);
        minimumRequiredDateOfBirthYear = (LocalDate.now().getYear() - CustomerService.CUSTOMER_MIN_AGE_REQUIRED);
    }

    @Test
    void canGetAllCustomers() {
        // When
        customerService.getAllCustomers();

        // Then
        verify(customerRepository).findAll();
    }

    @Test
    void canGetCustomer() {
        // Given
        String code = UUID.randomUUID().toString();

        Customer customer = new Customer();
        customer.setCode(code);
        customer.setEmail("alex@gmail.com");
        customer.setName("Alex");
        customer.setDateOfBirth(LocalDate.of(minimumRequiredDateOfBirthYear, 1, 1));

        // When
        when(customerRepository.findCustomerByCode(code)).thenReturn(Optional.of(customer));
        customerService.getCustomer(code);

        // Then
        verify(customerRepository).findCustomerByCode(code);
    }

    @Test
    void canCreateNewCustomerWithUniqueEmail() {
        // Given
        Customer customer = new Customer();
        customer.setCode(UUID.randomUUID().toString());
        customer.setEmail("alex@gmail.com");
        customer.setName("Alex");
        customer.setDateOfBirth(LocalDate.of(minimumRequiredDateOfBirthYear, 1, 1));

        // When
        customerService.createCustomer(customer);

        // Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);

        verify(customerRepository).save(customerArgumentCaptor.capture());

        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer).isEqualTo(customer);
    }

    @Test
    void canUpdateExistingCustomerWithNameChanged() {
        Customer customer = new Customer();
        customer.setCode(UUID.randomUUID().toString());
        customer.setEmail("alex@gmail.com");
        customer.setName("Alex");
        customer.setDateOfBirth(LocalDate.of(minimumRequiredDateOfBirthYear, 1, 1));
    }

    @Test
    void canUpdateExistingCustomerWithNewName() {
        String name = "John";
        String code = UUID.randomUUID().toString();

        Customer customer = new Customer();
        customer.setEmail("alex@gmail.com");
        customer.setName(name);
        customer.setDateOfBirth(LocalDate.of(minimumRequiredDateOfBirthYear, 1, 1));

        when(customerRepository.findCustomerByCode(code)).thenReturn(Optional.of(customer));
        Customer updatedCustomer = customerService.updateCustomer(code, customer);

        assertThat(updatedCustomer.getName()).isEqualTo(name);
    }

    @Test
    void canOpenNewAccountForExistingCustomer() {
        String code = UUID.randomUUID().toString();

        Customer customer = new Customer();
        customer.setCode(code);
        customer.setEmail("alex@gmail.com");
        customer.setName("Alex");
        customer.setDateOfBirth(LocalDate.of(minimumRequiredDateOfBirthYear, 1, 1));

        when(customerRepository.findCustomerByCode(code)).thenReturn(Optional.of(customer));
        customerService.openNewAccount(code);

        verify(accountService).openNewAccountForCustomer(customer);
    }

    @Test
    void willThrowWhenCustomerDoesNotExistOnGetSingleCustomer() {
        String code = UUID.randomUUID().toString();

        Customer customer = new Customer();
        customer.setCode(code);
        customer.setEmail("alex@gmail.com");
        customer.setName("Alex");
        customer.setDateOfBirth(LocalDate.of(minimumRequiredDateOfBirthYear, 1, 1));

        assertThrows(CustomerNotFoundException.class, () -> customerService.getCustomer(code));
    }

    @Test
    void willThrowWhenCustomerEmailIsTakenOnCustomerCreate() {
        Customer customer = new Customer();
        customer.setCode(UUID.randomUUID().toString());
        customer.setEmail("alex@gmail.com");
        customer.setName("Alex");
        customer.setDateOfBirth(LocalDate.of(minimumRequiredDateOfBirthYear, 1, 1));

        when(customerRepository.findCustomerByEmailWithExcludeList(customer.getEmail(), List.of("")))
                .thenReturn(Optional.of(customer));

        assertThrows(RuntimeException.class, () -> customerService.createCustomer(customer));

        // TODO: Check exception message
    }

    @Test
    void willThrowWhenCustomerAgeIsLessThanRequiredOnCustomerCreate() {
        Customer customer = new Customer();
        customer.setCode(UUID.randomUUID().toString());
        customer.setEmail("alex@gmail.com");
        customer.setName("Alex");

        customer.setDateOfBirth(LocalDate.of(minimumRequiredDateOfBirthYear + 1, 1, 1));

        assertThrows(CustomerIneligibleException.class, () -> customerService.createCustomer(customer));
    }

    @Test
    void willThrowWhenCustomerDoesNotExistOnUpdate() {
        String code = UUID.randomUUID().toString();
        Customer customer = new Customer();
        customer.setCode(code);
        customer.setEmail("alex@gmail.com");
        customer.setName("Alex");
        customer.setDateOfBirth(LocalDate.of(minimumRequiredDateOfBirthYear, 1, 1));

        when(customerRepository.findCustomerByCode(code)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.updateCustomer(code, customer));
    }

    @Test
    void willThrowWhenCustomerEmailIsTakenOnUpdate() {
        String code = UUID.randomUUID().toString();
        Customer customer = new Customer();
        customer.setCode(code);
        customer.setEmail("alex@gmail.com");
        customer.setName("Alex");
        customer.setDateOfBirth(LocalDate.of(minimumRequiredDateOfBirthYear, 1, 1));

        when(customerRepository.findCustomerByCode(code)).thenReturn(Optional.of(customer));

        when(customerRepository.findCustomerByEmailWithExcludeList(customer.getEmail(), List.of("")))
                .thenReturn(Optional.of(customer));

        assertThrows(RuntimeException.class, () -> customerService.updateCustomer(code, customer));
    }

    @Test
    void willThrowWhenCustomerDoesNotExistOnOpenAccount() {
        String code = UUID.randomUUID().toString();

        when(customerRepository.findCustomerByCode(code)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.openNewAccount(code));
    }

}