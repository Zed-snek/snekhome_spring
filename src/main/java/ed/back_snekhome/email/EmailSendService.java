package ed.back_snekhome.email;

import ed.back_snekhome.exceptionHandler.exceptions.SendEmailErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


@Service
@RequiredArgsConstructor
public class EmailSendService {


    @Value("${spring.mail.username}")
    private String serverEmail;
    @Value("${domain.address}")
    private String domain;

    private final JavaMailSender mailSender;

    @Async
    public void sendEmail(String destinationAddress, String title, String body) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setText(body, true);
            helper.setTo(destinationAddress);
            helper.setSubject(title);
            helper.setFrom(serverEmail);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new SendEmailErrorException();
        }

    }

    public void sendResetPasswordMail(String email, String name, String tokenValue) {
        String body = EmailTemplates.resetPasswordTemplate(name, domain + "/password_reset/" + tokenValue);
        sendEmail(email, "Reset password confirmation", body);
    }


    public void sendVerificationMail(String email, String name, String tokenValue)  {
        String body = EmailTemplates.verificationTemplate(name, domain + "/confirmation/" + tokenValue);
        sendEmail(email, "Verify your registration", body);
    }


    public void sendChangeEmailMail(String oldEmail, String newEmail, String name, String tokenValue) {
        String body = EmailTemplates.changeEmailTemplate(name, domain + "/reset_mail/" + tokenValue, newEmail);
        sendEmail(oldEmail, "Change e-mail on new one", body);
    }


    public void sendNewEmailConfirmationMail(String email, String name, String codeValue) {
        String body = EmailTemplates.changeEmailConfirmationNewTemplate(name, domain + "/new_mail/" + codeValue );
        sendEmail(email, "Confirm e-mail changing", body);
    }


}


