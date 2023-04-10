package com.example.minibank.controller.api;

import com.example.minibank.controller.request.DepositRequest;
import com.example.minibank.controller.request.TransferRequest;
import com.example.minibank.service.AccountService;
import com.example.minibank.model.Account;
import com.example.minibank.model.Transfer;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/accounts")
@Tag(name = "Account API", description = "Account related APIs. Read, add, update, deposit and transfer")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        List<Account> accountsList = accountService.getAllAccounts();

        return new ResponseEntity<>(accountsList, HttpStatus.OK);
    }

    @GetMapping(path = "{code}")
    public ResponseEntity<Account> getAccount(@PathVariable("code") String code) {
        Account account = accountService.getAccount(code);

        return new ResponseEntity<>(account, HttpStatus.OK);
    }

    @GetMapping(path = "{code}/transfers")
    public ResponseEntity<Map<String, List<Transfer>>> getAllTransfers(@PathVariable("code") String code) {
        Map<String, List<Transfer>> transfers = accountService.getAllTransfers(code);

        return new ResponseEntity<>(transfers, HttpStatus.OK);
    }

    @PostMapping(path = "{code}/transfers")
    public ResponseEntity<Void> transferAmount(@PathVariable("code") String code, @RequestBody TransferRequest transferRequest) {
        accountService.transfer(code, transferRequest);

        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "{code}/deposit")
    public ResponseEntity<Account> depositAmount(@PathVariable("code") String code, @RequestBody DepositRequest depositRequest) {
        Account account = accountService.deposit(code, depositRequest);

        return new ResponseEntity<>(account, HttpStatus.OK);
    }

}
