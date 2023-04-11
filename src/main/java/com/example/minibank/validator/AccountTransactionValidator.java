package com.example.minibank.validator;

import com.example.minibank.controller.request.DepositRequest;
import com.example.minibank.exception.AccountTransactionException;
import com.example.minibank.model.Account;
import com.example.minibank.service.AccountService;

public class AccountTransactionValidator {
    public static void validateDepositAmount(DepositRequest depositRequest) {
        if (depositRequest.getAmount() < AccountService.MINIMUM_DEPOSIT_AMOUNT) {
            throw new AccountTransactionException("Deposit amount cannot be less than " + AccountService.MINIMUM_DEPOSIT_AMOUNT);
        }

        if (depositRequest.getAmount() > AccountService.MAXIMUM_DEPOSIT_AMOUNT) {
            throw new AccountTransactionException("Deposit amount cannot be more than " + AccountService.MAXIMUM_DEPOSIT_AMOUNT);
        }
    }

    public static void validateTransferAmount(Account account, double amount) {
        if (amount > account.getBalance()) {
            throw new AccountTransactionException("Insufficient funds to make the transfer");
        }

        if (amount < AccountService.MINIMUM_TRANSFER_AMOUNT) {
            throw new AccountTransactionException("Transfer amount cannot be less than " + AccountService.MINIMUM_TRANSFER_AMOUNT);
        }
    }

}
