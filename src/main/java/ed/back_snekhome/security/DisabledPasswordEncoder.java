package ed.back_snekhome.security;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class DisabledPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence charSequence) {
        return charSequence.toString();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {

        //byte[] passwordb = rawPassword.toString().getBytes(StandardCharsets.UTF_8);


        return messageDigestIsEqual(rawPassword.toString(), encodedPassword);
    }



    private boolean messageDigestIsEqual(String a, String b) {

        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }

}
