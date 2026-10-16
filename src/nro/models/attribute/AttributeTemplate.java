package nro.models.attribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author Anwin
 */

@Getter
@Builder
@AllArgsConstructor
public class AttributeTemplate {

    @lombok.Getter @lombok.Setter private int id;
    @lombok.Getter @lombok.Setter private String name;
}






