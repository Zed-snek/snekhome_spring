package ed.back_snekhome.email;


import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.utils.MyFunctions;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.Calendar;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ConfirmationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idToken;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_user")
    private UserEntity user;


    private String token;
    private Timestamp expiresAt;
    private String message;

    @Nullable
    private boolean isActivated;

    @Column
    @Enumerated(EnumType.STRING)
    private ConfirmationType confirmationType;



    public ConfirmationToken(
            UserEntity user,
            ConfirmationType confirmationType,
            int tokenLength,
            int expiresAtMinutes
    ) {
        this.user = user;
        this.confirmationType = confirmationType;
        token = MyFunctions.generateCode(tokenLength) + System.currentTimeMillis();
        expiresAt = expiresAt(expiresAtMinutes);
    }


    public ConfirmationToken(
            UserEntity user,
            ConfirmationType confirmationType,
            int tokenLength,
            int expiresAtMinutes,
            String message
    ){
        this(user, confirmationType, tokenLength, expiresAtMinutes);
        this.message = message;
    }


    private Timestamp expiresAt( int minutes ){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, minutes);
        return new Timestamp( calendar.getTime().getTime() );
    }


    public boolean isExpired() {
        Timestamp timestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        return timestamp.after(expiresAt);
    }

}
