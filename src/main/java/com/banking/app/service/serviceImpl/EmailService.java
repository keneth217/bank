package com.banking.app.service.serviceImpl;

import com.banking.app.dto.EmailDetails;

public interface EmailService {
    void sendEmailAlert(EmailDetails emailDetails);
    void sendEmailAlertWithAttachment(EmailDetails emailDetails);
}
