package com.viana.agentic.executor;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotifier {

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;
    private final String to;

    @PostConstruct
    void init() {
        System.out.println("EMAIL CONFIG -> enabled=" + enabled + ", from=" + from + ", to=" + to);
    }

    public EmailNotifier(JavaMailSender mailSender,
                         @Value("${agentic.email.enabled:true}") boolean enabled,
                         @Value("${agentic.email.from:agentic@local.dev}") String from,
                         @Value("${agentic.email.to:ops@local.dev}") String to
    ) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
        this.to = to;
    }

    public void send(String subject, String body) {
        if (!enabled) return;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }
}
