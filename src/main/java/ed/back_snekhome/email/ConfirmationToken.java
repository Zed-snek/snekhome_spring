package ed.back_snekhome.email;

import ed.back_snekhome.utils.GenerationFunctions;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Calendar;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ConfirmationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idToken;

    private Long idUser;
    private String token;
    private Timestamp expiresAt;
    private String message;

    @Column
    @Enumerated(EnumType.STRING)
    private ConfirmationType confirmationType;

    public ConfirmationToken( Long idAccount, ConfirmationType confirmationType, int tokenLength, int expiresAtMinutes ) {
        idUser = idAccount;
        this.confirmationType = confirmationType;
        token = GenerationFunctions.generateCode(tokenLength) + System.currentTimeMillis();
        expiresAt = expiresAt(expiresAtMinutes);
    }

    public ConfirmationToken( Long idAccount, ConfirmationType confirmationType, int tokenLength, int expiresAtMinutes, String message ){
        this(idAccount, confirmationType, tokenLength, expiresAtMinutes);
        this.message = message;
    }


    private Timestamp expiresAt( int minutes ){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, minutes);
        return new Timestamp( calendar.getTime().getTime() );
    }

    public boolean isNotExpired(){
        Timestamp timestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
        return timestamp.before(expiresAt);
    }

}
