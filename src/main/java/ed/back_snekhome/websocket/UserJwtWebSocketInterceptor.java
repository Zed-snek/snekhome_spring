package ed.back_snekhome.websocket;

import ed.back_snekhome.security.JwtService;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;

import javax.security.sasl.AuthenticationException;

@AllArgsConstructor
public class UserJwtWebSocketInterceptor implements ChannelInterceptor {

    private JwtService jwtService;

    @SneakyThrows
    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {

        //get headers from message
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor.getCommand().equals(StompCommand.CONNECT)) {
            String authHeader = accessor.getNativeHeader(HttpHeaders.AUTHORIZATION).get(0);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new AuthenticationException("Access denied - wrong token");
            }

            Authentication authentication = jwtService.getAuthenticationFromToken(authHeader);

            accessor.setUser(authentication);
        }

        return message;
    }


}
