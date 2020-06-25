package com.aprendendotddspring.aprendendo.service.impl;

import com.aprendendotddspring.aprendendo.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Value("{application.mail.remetent}")
    private String remetent;



    private final JavaMailSender javaMailSender;

    @Override
    public void sendMails(String mensagem, List<String> emailsList) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        String[] mails = emailsList.toArray(new String[emailsList.size()]);

        mailMessage.setFrom(remetent);
        mailMessage.setSubject("Livro que vc colotiou!!");
        mailMessage.setText(mensagem);
        mailMessage.setTo(mails);

        javaMailSender.send(mailMessage);
    }
}
