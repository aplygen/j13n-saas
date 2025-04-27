package io.j13n.search.model.brave;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class LocationResult extends Result {

    private String type;

    private String id;

    private String providerUrl;

    private List<Float> coordinates;

    private Integer zoomLevel;

    private Thumbnail thumbnail;

    private PostalAddress postalAddress;

    private OpeningHours openingHours;

    private Contact contact;

    private String priceRange;

    private Rating rating;

    private Unit distance;

    private List<DataProvider> profiles;

    private Reviews reviews;

    private PictureResults pictures;

    private Action action;

    private List<String> servesCuisine;

    private List<String> categories;

    private String iconCategory;

    private LocationWebResult results;

    private String timezone;

    private String timezoneOffset;
}
