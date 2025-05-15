package io.j13n.core.model.scrape;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class FormField {
    private String name;
    private String id;
    private String type;
    private String label;
    private boolean required;
    private String placeholder;
    private String defaultValue;
    private String[] options;  // for select/radio/checkbox fields
    private String selector;   // CSS selector to locate this field
    private Map<String, String> metadata;  // Additional metadata for special field types

    public FormField() {
        this.metadata = new HashMap<>();
    }

    public Map<String, String> getMetadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        return metadata;
    }
}