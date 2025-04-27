package io.j13n.search.model.brave.response;

import io.j13n.search.model.brave.LocationResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * Top level response model for successful Local Search API request to get extra information for locations.
 * The response will include a list of location results corresponding to the ids in the request.
 * The API can also respond back with an error response in cases like too many ids being requested,
 * invalid subscription keys, and rate limit events.
 * Access to Local Search API requires a subscription to a Pro plan.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class LocalPoiSearchApiResponse implements Serializable {

    /**
     * The type of local POI search API result. The value is always local_pois.
     */
    private String type;

    /**
     * Location results matching the ids in the request.
     */
    private List<LocationResult> results;
}
