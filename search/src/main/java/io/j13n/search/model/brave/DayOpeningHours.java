package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class DayOpeningHours implements Serializable {

    private String abbrName;

    private String fullName;

    private String opens;

    private String closes;
}
