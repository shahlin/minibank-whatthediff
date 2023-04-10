package com.example.minibank.service;

import com.example.minibank.controller.request.DepositRequest;
import com.example.minibank.controller.request.TransferRequest;
import com.example.minibank.exception.AccountExistsException;
import com.example.minibank.exception.AccountNotFoundException;
import com.example.minibank.exception.AccountTransactionException;
import com.example.minibank.model.Account;
import com.example.minibank.model.Customer;
import com.example.minibank.model.Transfer;
import com.example.minibank.repository.AccountRepository;
import com.example.minibank.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransferRepository transferRepository;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, transferRepository);
    }

    @Test
    void canGetAllAccounts() {
        accountService.getAllAccounts();

        verify(accountRepository).findAll();
    }

    @Test
    void canGetAccount() {
        // Given
        String code = UUID.randomUUID().toString();

        Account account = new Account();
        account.setCode(code);
        account.setBalance(0);

        // When
        when(accountRepository.findAccountByCode(code)).thenReturn(Optional.of(account));
        accountService.getAccount(code);

        // Then
        verify(accountRepository).findAccountByCode(code);
    }

    @Test
    void canGetAllTransfersForNewAccount() {
        String code = anyString();

        Account account = new Account();
        account.setId(1);
        account.setCode(code);
        account.setBalance(0);

        when(accountRepository.findAccountByCode(code)).thenReturn(Optional.of(account));

        Map<String, List<Transfer>> expectedTransfers = new HashMap<>();
        expectedTransfers.put("sent", Collections.emptyList());
        expectedTransfers.put("received", Collections.emptyList());

        assertThat(accountService.getAllTransfers(code)).isEqualTo(expectedTransfers);
    }

    @Test
    void canOpenNewAccountForCustomerWithExistingAccount() {
        String randomCode = UUID.randomUUID().toString();
        Integer id = 1;

        Customer customer = new Customer();
        customer.setId(id);
        customer.setCode(randomCode);
        customer.setEmail("alex@gmail.com");
        customer.setName("Alex");
        customer.setDateOfBirth(LocalDate.of(2000, 1, 1));

        when(accountRepository.findAccountByCustomerId(id)).thenReturn(Optional.empty());

        accountService.openNewAccountForCustomer(customer);

        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);

        verify(accountRepository).save(accountArgumentCaptor.capture());
    }

    @Test
    void canDepositValidAmountIntoAccount() {
        double amountToDeposit = 1000;

        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setAmount(amountToDeposit);

        String code = anyString();
        Account account = new Account();
        account.setId(1);
        account.setCode(code);
        account.setBalance(0);

        when(accountRepository.findAccountByCode(code)).thenReturn(Optional.of(account));

        accountService.deposit(code, depositRequest);

        assertThat(account.getBalance()).isEqualTo(amountToDeposit);
    }

    @Test
    void canTransferValidAmountToAnotherAccount() {
        String senderCode = UUID.randomUUID().toString();
        Account senderAccount = new Account();
        senderAccount.setCode(senderCode);
        senderAccount.setBalance(1000);

        String receiverCode = UUID.randomUUID().toString();
        Account receiverAccount = new Account();
        receiverAccount.setCode(receiverCode);
        receiverAccount.setBalance(0);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setReceiverAccountCode(receiverCode);
        transferRequest.setAmount(500);

        when(accountRepository.findAccountByCode(anyString()))
                .thenReturn(Optional.of(senderAccount))
                .thenReturn(Optional.of(receiverAccount));

        accountService.transfer(senderCode, transferRequest);

        ArgumentCaptor<Transfer> transferArgumentCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(transferArgumentCaptor.capture());

        assertThat(senderAccount.getBalance()).isEqualTo(500);
        assertThat(receiverAccount.getBalance()).isEqualTo(500);
    }

    @Test
    void canHandleMultipleDepositsConcurrently() throws InterruptedException {
        double amountToDeposit = 1000;

        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setAmount(amountToDeposit);

        String code = anyString();
        Account account = new Account();
        account.setId(1);
        account.setCode(code);
        account.setBalance(0);

        when(accountRepository.findAccountByCode(code)).thenReturn(Optional.of(account));

        int numberOfThreads = 50;
        ExecutorService service = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                accountService.deposit(code, depositRequest);
                latch.countDown();
            });
        }

        latch.await();
        assertThat(account.getBalance()).isEqualTo(amountToDeposit * numberOfThreads);
    }

    @Test
    void canHandleSingleAccountTransfersConcurrently() throws InterruptedException {
        String senderCode = UUID.randomUUID().toString();
        Account senderAccount = new Account();
        senderAccount.setCode(senderCode);
        senderAccount.setBalance(1000);

        String receiverCode = UUID.randomUUID().toString();
        Account receiverAccount = new Account();
        receiverAccount.setCode(receiverCode);
        receiverAccount.setBalance(0);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setReceiverAccountCode(receiverCode);
        transferRequest.setAmount(2);

        when(accountRepository.findAccountByCode(senderCode)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findAccountByCode(receiverCode)).thenReturn(Optional.of(receiverAccount));

        int numberOfThreads = 100;
        ExecutorService service = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                accountService.transfer(senderCode, transferRequest);
                latch.countDown();
            });
        }

        latch.await();

        assertThat(senderAccount.getBalance())
                .isEqualTo(1000 - (numberOfThreads * transferRequest.getAmount()));

        assertThat(receiverAccount.getBalance())
                .isEqualTo(0 + (numberOfThreads * transferRequest.getAmount()));
    }

    @Test
    void canHandleDoubleAccountTransfersConcurrently() throws InterruptedException {
        String senderCode1 = UUID.randomUUID().toString();
        Account senderAccount1 = new Account();
        senderAccount1.setCode(senderCode1);
        senderAccount1.setBalance(1000);

        String senderCode2 = UUID.randomUUID().toString();
        Account senderAccount2 = new Account();
        senderAccount2.setCode(senderCode2);
        senderAccount2.setBalance(1000);

        String receiverCode = UUID.randomUUID().toString();
        Account receiverAccount = new Account();
        receiverAccount.setCode(receiverCode);
        receiverAccount.setBalance(0);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setReceiverAccountCode(receiverCode);
        transferRequest.setAmount(2);

        when(accountRepository.findAccountByCode(senderCode1)).thenReturn(Optional.of(senderAccount1));
        when(accountRepository.findAccountByCode(senderCode2)).thenReturn(Optional.of(senderAccount2));
        when(accountRepository.findAccountByCode(receiverCode)).thenReturn(Optional.of(receiverAccount));

        int numberOfThreads = 100;
        ExecutorService service = Executors.newFixedThreadPool(200);
        CountDownLatch latch1 = new CountDownLatch(numberOfThreads);
        CountDownLatch latch2 = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                accountService.transfer(senderCode1, transferRequest);
                latch1.countDown();
            });

            service.execute(() -> {
                accountService.transfer(senderCode2, transferRequest);
                latch2.countDown();
            });
        }

        latch1.await();
        latch2.await();

        assertThat(senderAccount1.getBalance())
                .isEqualTo(1000 - (numberOfThreads * transferRequest.getAmount()));

        assertThat(senderAccount1.getBalance())
                .isEqualTo(1000 - (numberOfThreads * transferRequest.getAmount()));

        assertThat(receiverAccount.getBalance())
                .isEqualTo(2 * (numberOfThreads * transferRequest.getAmount()));
    }

    @Test
    void willThrowWhenAccountDoesNotExistOnGetSingleAccount() {
        when(accountRepository.findAccountByCode(anyString())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(anyString()));
    }

    @Test
    void willThrowWhenAccountDoesNotExistOnGetAllTransfers() {
        when(accountRepository.findAccountByCode(anyString())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAllTransfers(anyString()));
    }

    @Test
    void willThrowWhenCustomerAlreadyHasAnAccount() {
        String randomCode = UUID.randomUUID().toString();
        Integer id = 1;

        Customer customer = new Customer();
        customer.setId(id);
        customer.setCode(randomCode);
        customer.setEmail("alex@gmail.com");
        customer.setName("Alex");
        customer.setDateOfBirth(LocalDate.of(2000, 1, 1));

        Account account = new Account();
        account.setCode(randomCode);
        account.setCustomer(customer);

        when(accountRepository.findAccountByCustomerId(id)).thenReturn(Optional.of(account));

        assertThrows(AccountExistsException.class, () -> accountService.openNewAccountForCustomer(customer));
    }

    @Test
    void willThrowWhenAccountDoesNotExistOnDeposit() {
        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setAmount(1000);

        when(accountRepository.findAccountByCode(anyString())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.deposit(anyString(), depositRequest));
    }

    @Test
    void willThrowWhenDepositAmountIsMoreThanAllowed() {
        double amountToDeposit = 100_000_000;

        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setAmount(amountToDeposit);

        String code = anyString();
        Account account = new Account();

        when(accountRepository.findAccountByCode(code)).thenReturn(Optional.of(account));

        assertThrows(AccountTransactionException.class, () -> accountService.deposit(code, depositRequest));
    }

    @Test
    void willThrowWhenDepositAmountIsLessThanAllowed() {
        double amountToDeposit = -10;

        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setAmount(amountToDeposit);

        String code = anyString();
        Account account = new Account();

        when(accountRepository.findAccountByCode(code)).thenReturn(Optional.of(account));

        assertThrows(AccountTransactionException.class, () -> accountService.deposit(code, depositRequest));
    }

    @Test
    void willThrowWhenSenderAccountDoesNotExistOnTransfer() {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setAmount(1000);

        when(accountRepository.findAccountByCode(anyString())).thenReturn(Optional.empty());

        AccountNotFoundException thrown = assertThrows(
                AccountNotFoundException.class,
                () -> accountService.transfer(anyString(), transferRequest)
        );

        assertEquals("Sender account not found", thrown.getMessage());
    }

    @Test
    void willThrowWhenReceiverAccountDoesNotExistOnTransfer() {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setAmount(1000);

        Account account = new Account();

        when(accountRepository.findAccountByCode(anyString()))
                .thenReturn(Optional.of(account))
                .thenReturn(Optional.empty());

        AccountNotFoundException thrown = assertThrows(
                AccountNotFoundException.class,
                () -> accountService.transfer(anyString(), transferRequest)
        );

        assertEquals("Receiver account not found", thrown.getMessage());
    }

    @Test
    void willThrowWhenSenderDoesNotHaveEnoughFundsOnTransfer() {
        String senderCode = UUID.randomUUID().toString();
        Account senderAccount = new Account();
        senderAccount.setCode(senderCode);
        senderAccount.setBalance(1000);

        String receiverCode = UUID.randomUUID().toString();
        Account receiverAccount = new Account();
        receiverAccount.setCode(receiverCode);
        receiverAccount.setBalance(0);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setReceiverAccountCode(receiverCode);
        transferRequest.setAmount(2000);

        when(accountRepository.findAccountByCode(anyString()))
                .thenReturn(Optional.of(senderAccount))
                .thenReturn(Optional.of(receiverAccount));

        AccountTransactionException thrown = assertThrows(
                AccountTransactionException.class,
                () -> accountService.transfer(senderCode, transferRequest)
        );

        assertEquals("Insufficient funds to make the transfer", thrown.getMessage());
    }

    @Test
    void willThrowWhenTransferAmountIsLessThatMinimumAllowedOnTransfer() {
        String senderCode = UUID.randomUUID().toString();
        Account senderAccount = new Account();
        senderAccount.setCode(senderCode);
        senderAccount.setBalance(1000);

        String receiverCode = UUID.randomUUID().toString();
        Account receiverAccount = new Account();
        receiverAccount.setCode(receiverCode);
        receiverAccount.setBalance(0);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setReceiverAccountCode(receiverCode);
        transferRequest.setAmount(-10);

        when(accountRepository.findAccountByCode(anyString()))
                .thenReturn(Optional.of(senderAccount))
                .thenReturn(Optional.of(receiverAccount));

        AccountTransactionException thrown = assertThrows(
                AccountTransactionException.class,
                () -> accountService.transfer(senderCode, transferRequest)
        );

        assertEquals("Transfer amount cannot be less than 1", thrown.getMessage());
    }
}