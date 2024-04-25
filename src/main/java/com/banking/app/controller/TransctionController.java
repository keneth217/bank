package com.banking.app.controller;

import com.banking.app.dto.BankResponse;
import com.banking.app.entity.Transactions;
import com.banking.app.service.serviceImpl.BankStatement;
import com.itextpdf.text.DocumentException;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/bankstatement")
@AllArgsConstructor
public class TransctionController {
    private BankStatement bankStatement;
    @GetMapping
    public List<Transactions> generateStatement(@RequestParam String accountNumber,
                                                @RequestParam String startDate,
                                                @RequestParam String endDate) throws DocumentException, FileNotFoundException {
        return bankStatement.generateStatement(accountNumber,startDate,endDate);
    }
    @GetMapping("/all")
    public List<BankResponse> AllAccounts() throws DocumentException, FileNotFoundException {
        System.out.println("list of accounts"+bankStatement.allAccounts());
        return bankStatement.allAccounts();
    }
    @GetMapping("/search/{name}")
    public List<BankResponse> searchAccountByName(@PathVariable String name) {
        System.out.println("list of accounts"+bankStatement.searchByName(name));
        return bankStatement.searchByName(name);
    }
    @DeleteMapping("/account/{accountNumber}")
    public BankResponse deleteAccount(@PathVariable String accountNumber){
        return bankStatement.deleteAccount(accountNumber);
    }


}
