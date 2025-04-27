package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Query implements Serializable {

    private String original;

    private Boolean showStrictWarning;

    private String altered;

    private Boolean safesearch;

    private Boolean isNavigational;

    private Boolean isGeolocal;

    private String localDecision;

    private Integer localLocationsIdx;

    private Boolean isTrending;

    private Boolean isNewsBreaking;

    private Boolean askForLocation;

    private Language language;

    private Boolean spellcheckOff;

    private String country;

    private Boolean badResults;

    private Boolean shouldFallback;

    private String lat;

    private String longitude;

    private String postalCode;

    private String city;

    private String state;

    private String headerCountry;

    private Boolean moreResultsAvailable;

    private String customLocationLabel;

    private String redditCluster;
}
