package com.aprendendotddspring.aprendendo.service;

import java.util.List;

public interface EmailService {
    void sendMails(String mensagem, List<String> emailsList);
}
