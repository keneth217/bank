package com.banking.app.service.serviceImpl;

import com.banking.app.dto.TransactionDto;
import org.springframework.stereotype.Service;

@Service
public interface TransactionService {
    void saveTransaction(TransactionDto transactionDto);
}
