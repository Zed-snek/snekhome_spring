package ed.back_snekhome.response;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OwnSuccessResponse {

    private final int status = 200;
    private String message;
    private long timestamp;

    public OwnSuccessResponse(String message) {
        this.message = message;

        this.timestamp = System.currentTimeMillis();
    }

}
