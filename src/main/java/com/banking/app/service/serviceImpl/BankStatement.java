package com.banking.app.service.serviceImpl;

import com.banking.app.dto.AccountInfo;
import com.banking.app.dto.BankResponse;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
//@AllArgsConstructor
@Slf4j
public class BankStatement {
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
 UserRepository userRepository;
    @Autowired
    EmailService emailService;
    private static final String documentUrl= "D:\\excels\\";
    private JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private  String senderEmail;

    private String generateUniqueFileName() {
        String randomString = UUID.randomUUID().toString().replace("-", "");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "Account Statement report_" + randomString + "_" + timestamp;
    }
    private String listOfAccountsName() {
        String randomString = UUID.randomUUID().toString().replace("-", "");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "List of accounts report_" + randomString + "_" + timestamp;
    }

    public List<Transactions> generateStatement(String accountNumber, String startDate, String endDate) throws FileNotFoundException, DocumentException {
        String docName = generateUniqueFileName() + ".pdf";
        String filePath = documentUrl + docName;
        System.out.println("located in:" + filePath);
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE).plusDays(1); // Adding one day to include transactions on the end date

        User user = userRepository.findByAccountNumber(accountNumber);
        String customerName = user.getFirstName() + " " + user.getLastName() + " " + user.getOtherName();
        String address = user.getAddress();

        List<Transactions> transactionsList = transactionRepository.findAll().stream()
                .filter(transaction -> transaction.getAccountNumber().equals(accountNumber))
                .filter(transaction -> !transaction.getCreatedAt().isBefore(start.atStartOfDay()) && !transaction.getCreatedAt().isAfter(end.atStartOfDay()))
                .toList();

        Rectangle statementSize = new Rectangle(PageSize.A4);
        Document document = new Document(statementSize);
        OutputStream outputStream = new FileOutputStream(filePath);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        PdfPTable bankInfoTable = new PdfPTable(1);
        PdfPCell bankName = new PdfPCell(new Phrase("ABC BANK"));
        bankName.setBorder(0);
        bankName.setBackgroundColor(BaseColor.BLUE);
        bankName.setPadding(10F);

        PdfPCell bankAddress = new PdfPCell(new Phrase("20-400, LANET, NAKURU"));
        bankAddress.setBorder(0);
        bankAddress.setBackgroundColor(BaseColor.BLUE);
        bankAddress.setPadding(10F);

        bankInfoTable.addCell(bankName);
        bankInfoTable.addCell(bankAddress);

        PdfPTable statementInfo = new PdfPTable(2);
        PdfPCell customerInfo = new PdfPCell(new Phrase("Start Date: " + startDate));
        PdfPCell statement = new PdfPCell(new Phrase("STATEMENT OF ACCOUNT"));
        PdfPCell stopDate = new PdfPCell(new Phrase("End Date: " + endDate));
        PdfPCell space = new PdfPCell();
        PdfPCell customer = new PdfPCell(new Phrase("Customer Name: " + customerName));
        PdfPCell userAddress = new PdfPCell(new Phrase("Customer Address: " + address));

        customerInfo.setBorder(0);
        statement.setBorder(0);
        stopDate.setBorder(0);
        customer.setBorder(0);
        space.setBorder(0);
        userAddress.setBorder(0);

        statementInfo.addCell(customerInfo);
        statementInfo.addCell(statement);
        statementInfo.addCell(stopDate);
        statementInfo.addCell(customer);
        statementInfo.addCell(space);
        statementInfo.addCell(userAddress);

        PdfPTable transactionTable = new PdfPTable(4);
        PdfPCell date = new PdfPCell(new Phrase("DATE"));
        PdfPCell transactionType = new PdfPCell(new Phrase("TRANSACTION TYPE"));
        PdfPCell amount = new PdfPCell(new Phrase("AMOUNT"));
        PdfPCell status = new PdfPCell(new Phrase("STATUS"));

        date.setBackgroundColor(BaseColor.BLUE);
        transactionType.setBackgroundColor(BaseColor.BLUE);
        amount.setBackgroundColor(BaseColor.BLUE);
        status.setBackgroundColor(BaseColor.BLUE);

        // Define a smaller font size for column headers
        int smallerFontSize = 8;
        Font headerFont = new Font(Font.FontFamily.TIMES_ROMAN, smallerFontSize);

        // Use smaller font size for column headers
        date.setPhrase(new Phrase("DATE", headerFont));
        transactionType.setPhrase(new Phrase("TRANSACTION TYPE", headerFont));
        amount.setPhrase(new Phrase("AMOUNT", headerFont));
        status.setPhrase(new Phrase("STATUS", headerFont));

        transactionTable.addCell(date);
        transactionTable.addCell(transactionType);
        transactionTable.addCell(amount);
        transactionTable.addCell(status);

        transactionsList.forEach(transactions -> {
            transactionTable.addCell(new Phrase(transactions.getCreatedAt().toString()));
            transactionTable.addCell(new Phrase(transactions.getTransactionType()));
            transactionTable.addCell(new Phrase(String.valueOf(transactions.getAmount())));
            transactionTable.addCell(new Phrase(transactions.getStatus()));
        });

        document.add(bankInfoTable);
        document.add(statementInfo);
        document.add(transactionTable);
        document.close();

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(user.getEmail())
                .subject("STATEMENT OF ACCOUNTS")
                .messageBody("Attached find you requested Statements of Accounts from ABC BANK")
                .attachment(filePath)
                .build();

        emailService.sendEmailAlertWithAttachment(emailDetails);

        return transactionsList;
    }

    public List<BankResponse> allAccounts() throws DocumentException, FileNotFoundException {
        List<User> accountList = userRepository.findAll();
        List<BankResponse> responses = new ArrayList<>();
        String docName = listOfAccountsName() + ".pdf";
        String filePath = documentUrl + docName;
        System.out.println("located in:" + filePath);
        int smallerFontSize = 8;
        Font smallerFont = new Font(Font.FontFamily.TIMES_ROMAN, smallerFontSize);

        Rectangle accountListSize = new Rectangle(PageSize.A4);
        Document document = new Document(accountListSize);
        OutputStream outputStream = new FileOutputStream(filePath);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        PdfPTable bankInfoTable = new PdfPTable(1);
        PdfPCell bankName = new PdfPCell(new Phrase("ABC BANK"));
        bankName.setBorder(0);
        bankName.setBackgroundColor(BaseColor.BLUE);
        bankName.setPadding(10F);

        PdfPCell bankAddress = new PdfPCell(new Phrase("20-400, LANET, NAKURU"));
        bankAddress.setBorder(0);
        bankAddress.setBackgroundColor(BaseColor.BLUE);
        bankAddress.setPadding(10F);

        bankInfoTable.addCell(bankName);
        bankInfoTable.addCell(bankAddress);

        PdfPTable statementInfo = new PdfPTable(1);

        PdfPCell statement = new PdfPCell(new Phrase("LIST OF ACCOUNTS"));
        statement.setBorder(0);
        statement.setColspan(1);
        statement.setIndent(20.0F);
// Center cell content vertically
        statement.setVerticalAlignment(Element.ALIGN_MIDDLE);
// Add top and bottom margin space
        statement.setPaddingTop(10f);
        statement.setPaddingBottom(10f);

        statementInfo.addCell(statement);

        PdfPTable transactionTable = new PdfPTable(4);
        PdfPCell name = new PdfPCell(new Phrase("ACCOUNT NAME"));
        PdfPCell email = new PdfPCell(new Phrase("ACCOUNT EMAIL"));
        PdfPCell accNumber = new PdfPCell(new Phrase("ACCOUNT NUMBER"));
        PdfPCell balance = new PdfPCell(new Phrase("BALANCE"));

        name.setBackgroundColor(BaseColor.BLUE);
        accNumber.setBackgroundColor(BaseColor.BLUE);
        balance.setBackgroundColor(BaseColor.BLUE);
        email.setBackgroundColor(BaseColor.BLUE);

        // Set smaller font size for column items
        name.setPhrase(new Phrase("ACCOUNT NAME", smallerFont));
        email.setPhrase(new Phrase("ACCOUNT EMAIL", smallerFont));
        accNumber.setPhrase(new Phrase("ACCOUNT NUMBER", smallerFont));
        balance.setPhrase(new Phrase("BALANCE", smallerFont));

        transactionTable.addCell(name);
        transactionTable.addCell(email);
        transactionTable.addCell(accNumber);
        transactionTable.addCell(balance);

        accountList.forEach(account1 -> {
            String userName = account1.getFirstName() + " " + account1.getLastName() + " " + account1.getOtherName();
            PdfPCell userNameCell = new PdfPCell(new Phrase(userName, smallerFont));
            PdfPCell emailCell = new PdfPCell(new Phrase(account1.getEmail(), smallerFont));
            PdfPCell accNumberCell = new PdfPCell(new Phrase(account1.getAccountNumber(), smallerFont));
            PdfPCell balanceCell = new PdfPCell(new Phrase(String.valueOf(account1.getAccountBalance()), smallerFont));

            transactionTable.addCell(userNameCell);
            transactionTable.addCell(emailCell);
            transactionTable.addCell(accNumberCell);
            transactionTable.addCell(balanceCell);
        });

        document.add(bankInfoTable);
        document.add(statementInfo);
        document.add(transactionTable);
        document.close();

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(senderEmail)
                .subject("LIST OF ACCOUNTS")
                .messageBody("Attached find you requested list of Accounts from ABC BANK")
                .attachment(filePath)
                .build();

        emailService.sendEmailAlertWithAttachment(emailDetails);
        List<BankResponse> responses1 = new ArrayList<>();

        accountList.forEach(account -> {
            String userName = account.getFirstName() + " " + account.getLastName() + " " + account.getOtherName();
            responses.add(BankResponse.builder()
                            .responseCode("200")
                            .responseMessage(userName +" "+"Accounts details")
                    .accountInfo(AccountInfo.builder()
                            .accountNumber(account.getAccountNumber())
                            .accountBalance(account.getAccountBalance())
                            .accountName(userName)
                            .build())
                    .build());
        });
        return responses;

    }
    public List<BankResponse> searchByName(String name) {
        List<User> searchResults = userRepository.findUserByFirstNameContainingIgnoreCase(name);
        List<BankResponse> responses = new ArrayList<>();

        if (searchResults.isEmpty()) {
            // If no results found, return an empty list with a response message
            BankResponse emptyResponse = BankResponse.builder()
                    .responseCode("200")
                    .responseMessage("No search results found for: " + name)
                    .build();
            responses.add(emptyResponse);
        } else {
            // If results found, create response for each user
            for (User user : searchResults) {
                String accountName = user.getFirstName() + " " + user.getLastName() + " " + user.getOtherName();
                BankResponse response = BankResponse.builder()
                        .responseCode("200")
                        .responseMessage("Here are the search results for: " + name)
                        .accountInfo(AccountInfo.builder()
                                .accountName(accountName)
                                .accountBalance(user.getAccountBalance())
                                .accountNumber(user.getAccountNumber())
                                .build())
                        .build();
                responses.add(response);
            }
        }

        return responses;
    }
    @Transactional
    public BankResponse deleteAccount(String accountNumber) {
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByAccountNumber(accountNumber));
        if (optionalUser.isPresent()) {
            User deletedUser = optionalUser.get();
            String deleteAccount = deletedUser.getFirstName() + " " + deletedUser.getLastName() + " " + deletedUser.getOtherName();

            userRepository.deleteByAccountNumber(accountNumber);
            System.out.println("deleting ABC BANK ACCOUNT  for"+" "+deletedUser);
            EmailDetails emailDetails = EmailDetails.builder()
                    .recipient(deletedUser.getEmail())
                    .subject("ACCOUNT DELETION")
                    .messageBody("Hello " + deleteAccount + ",\n\nSad to see you leave ABC BANK! Your Account has been successfully deleted. 😢😢😢\n\nIf you did not initiate this process, please contact ABC BANK for assistance.")
                    .build();
            emailService.sendEmailAlert(emailDetails);

            return BankResponse.builder()
                    .responseCode("200")
                    .responseMessage("Hello " + deleteAccount + ", Sad to see you leave ABC BANK! Your Account has been successfully deleted. 😢😢😢\nIf you did not initiate this process, please contact ABC BANK.")
                    .build();
        } else {
            return BankResponse.builder()
                    .responseCode("400")
                    .responseMessage("Error deleting user with account number " + accountNumber + ". Please contact ABC BANK for assistance.")
                    .build();
        }
    }



}
