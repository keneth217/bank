package com.banking.app.service.serviceImpl;

import com.banking.app.dto.EmailDetails;
import com.banking.app.entity.Transactions;
import com.banking.app.entity.User;
import com.banking.app.repository.TransactionRepository;
import com.banking.app.repository.UserRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;
    /**
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return  userRepository.findByEmail( username).orElseThrow(()->new UsernameNotFoundException("invalid user details"));
    }
}

