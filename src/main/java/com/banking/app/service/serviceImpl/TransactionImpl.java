package com.banking.app.service.serviceImpl;

import com.banking.app.dto.TransactionDto;
import com.banking.app.entity.Transactions;
import com.banking.app.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
@Component
@Service
public class TransactionImpl implements TransactionService{
    @Autowired
    TransactionRepository transactionRepository;

    /**
     * @param transactionDto
     */
    @Override
    public void saveTransaction(TransactionDto transactionDto) {
        Transactions transactions= Transactions.builder()
                .transactionType(transactionDto.getTransactionType())
                .accountNumber(transactionDto.getAccountNumber())
                .amount(transactionDto.getAmount())
                .status("SUCCESS")
                .build();
        transactionRepository.save(transactions);
        System.out.println("transaction saved successfully");

    }
}
