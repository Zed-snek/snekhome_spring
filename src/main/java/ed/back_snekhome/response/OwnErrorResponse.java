package ed.back_snekhome.response;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class OwnErrorResponse {

    private int status;
    private String message;
    private long timestamp;


    public OwnErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;

        this.timestamp = System.currentTimeMillis();
    }

}