package nro.models.power;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Anwin
 */

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Caption {

    @lombok.Getter @lombok.Setter private int id;
    private String earth, saiya, namek;
    @lombok.Getter @lombok.Setter private long power;

    public String getCaption(int planet) {
        String caption = earth;
        if (planet == 1) {
            caption = namek;
        } else if (planet == 2) {
            caption = saiya;
        }
        return caption;
    }
}






