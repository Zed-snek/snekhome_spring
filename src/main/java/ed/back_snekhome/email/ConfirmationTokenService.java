package ed.back_snekhome.email;

import ed.back_snekhome.exceptionHandler.exceptions.TokenExpiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfirmationTokenService { //For now, is used only to make confirmation code for verification by email

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public ConfirmationToken save(ConfirmationToken confirmationToken){
        return confirmationTokenRepository.save(confirmationToken);
    }


    public ConfirmationToken findByToken(String token){
        return confirmationTokenRepository.findConfirmationTokenByToken(token);
    }


    public void activateToken(ConfirmationToken token) {
        token.setActivated(true);
        save(token);
    }


    public void throwErrIfTokenExpiredOrActivated(ConfirmationToken token) {
        throwErrIfTokenIsActivated(token);
        if (token.isExpired())
            throw new TokenExpiredException("Confirmation token is expired");
    }

    public void throwErrIfTokenIsActivated(ConfirmationToken token) {
        if (token.isActivated())
            throw new TokenExpiredException("Confirmation token has been already activated");
    }

}
