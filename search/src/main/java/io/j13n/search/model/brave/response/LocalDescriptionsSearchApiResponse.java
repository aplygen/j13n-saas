package io.j13n.search.model.brave.response;

import io.j13n.search.model.brave.LocationDescription;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * Top level response model for successful Local Search API request to get AI generated description for locations.
 * The response includes a list of generated descriptions corresponding to the ids in the request.
 * The API can also respond back with an error response in cases like too many ids being requested,
 * invalid subscription keys, and rate limit events.
 * Access to Local Search API requires a subscription to a Pro plan.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class LocalDescriptionsSearchApiResponse implements Serializable {

    /**
     * The type of local description search API result. The value is always local_descriptions.
     */
    private String type;

    /**
     * Location descriptions matching the ids in the request.
     */
    private List<LocationDescription> results;
}
