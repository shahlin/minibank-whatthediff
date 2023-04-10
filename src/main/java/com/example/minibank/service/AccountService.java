package com.example.minibank.service;

import com.example.minibank.controller.request.DepositRequest;
import com.example.minibank.controller.request.TransferRequest;
import com.example.minibank.model.Customer;
import com.example.minibank.exception.AccountExistsException;
import com.example.minibank.exception.AccountNotFoundException;
import com.example.minibank.model.Account;
import com.example.minibank.repository.AccountRepository;
import com.example.minibank.model.Transfer;
import com.example.minibank.repository.TransferRepository;
import com.example.minibank.validator.AccountTransactionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AccountService {

    public static final int MINIMUM_DEPOSIT_AMOUNT = 1;
    public static final int MAXIMUM_DEPOSIT_AMOUNT = 100_000;
    public static final int MINIMUM_TRANSFER_AMOUNT = 1;

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository, TransferRepository transferRepository) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Account getAccount(String code) {
        return accountRepository.findAccountByCode(code)
                .orElseThrow(AccountNotFoundException::new);
    }

    @Transactional(rollbackFor = Exception.class)
    public Account openNewAccountForCustomer(Customer customer) {
        Optional<Account> accountOptional = accountRepository.findAccountByCustomerId(customer.getId());

        if (accountOptional.isPresent()) {
            throw new AccountExistsException();
        }

        Account account = new Account();
        account.setCode(generateAccountCode());
        account.setCustomer(customer);
        account.setBalance(0);

        return accountRepository.save(account);
    }

    public Map<String, List<Transfer>> getAllTransfers(String code) {
        Optional<Account> account = accountRepository.findAccountByCode(code);

        if (account.isEmpty()) {
            throw new AccountNotFoundException();
        }

        Map<String, List<Transfer>> transfers = new HashMap<>();
        transfers.put("sent", account.get().getSentTransfers());
        transfers.put("received", account.get().getReceivedTransfers());

        return transfers;
    }

    @Transactional(rollbackFor = Exception.class)
    public Account deposit(String code, DepositRequest depositRequest) {
        Optional<Account> account = accountRepository.findAccountByCode(code);

        if (account.isEmpty()) {
            throw new AccountNotFoundException();
        }

        AccountTransactionValidator.validateDepositAmount(depositRequest);

        account.get().deposit(depositRequest.getAmount());

        return account.get();
    }

    @Transactional(rollbackFor = Exception.class)
    public void transfer(String code, TransferRequest transferRequest) {
        Optional<Account> senderAccount = accountRepository.findAccountByCode(code);

        if (senderAccount.isEmpty()) {
            throw new AccountNotFoundException("Sender account not found");
        }

        Optional<Account> receiverAccount = accountRepository.findAccountByCode(transferRequest.getReceiverAccountCode());

        if (receiverAccount.isEmpty()) {
            throw new AccountNotFoundException("Receiver account not found");
        }

        AccountTransactionValidator.validateTransferAmount(senderAccount.get(), transferRequest.getAmount());

        Transfer transfer = new Transfer();
        transfer.setAmount(transferRequest.getAmount());
        transfer.setCode(UUID.randomUUID().toString());
        transfer.setRemarks(transferRequest.getRemarks());
        transfer.setSenderAccount(senderAccount.get());
        transfer.setReceiverAccount(receiverAccount.get());
        transferRepository.save(transfer);

        senderAccount.get().withdraw(transferRequest.getAmount());
        receiverAccount.get().deposit(transferRequest.getAmount());
    }

    private String generateAccountCode() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
