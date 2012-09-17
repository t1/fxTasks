package fxtasks.model;

import static lombok.AccessLevel.*;
import lombok.AllArgsConstructor;
import lombok.experimental.*;

@Value
@AllArgsConstructor(access = PRIVATE)
@Accessors(fluent = true)
public class Tag {
    public static Tag withId(String id) {
        return new Tag(id, null);
    }

    String id;
    String description;
}
