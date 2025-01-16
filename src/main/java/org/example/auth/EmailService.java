package org.example.auth;

import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Properties;

@ApplicationScoped
public class EmailService {
    private static final Config config = ConfigProvider.getConfig();
    String smtpHost = config.getValue("smtp.host", String.class);
    int smtpPort = config.getValue("smtp.port",Integer.class);
    String smtpUser = config.getValue("smtp.username", String.class);
    String smtpPassword = config.getValue("smtp.password", String.class);
    boolean startTlsEnabled = config.getValue("smtp.starttls.enable", Boolean.class);

    public void sendEmail(String from, String to, String subject, String content) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", String.valueOf(smtpPort));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", String.valueOf(startTlsEnabled));
        boolean flag = false;
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPassword);
            }
        });
        try{
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);
            Transport.send(message);
            flag = true;
        } catch (Exception e){
            throw new EJBException(e);
        }
    }

    private <T> T getConfigValue(String propertyName, Class<T> propertyType) {
        return config.getOptionalValue(propertyName, propertyType).orElse(null);
    }
}