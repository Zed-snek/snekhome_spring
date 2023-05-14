package ed.back_snekhome.response;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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