package fxtasks.model;

import java.util.UUID;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class TaskId {
    public static TaskId of(String id) {
        return new TaskId(id);
    }

    public static TaskId of(byte[] bytes) {
        return TaskId.of(new String(bytes));
    }

    public static TaskId random() {
        return TaskId.of(UUID.randomUUID().toString());
    }

    final String id;

    private TaskId(String id) {
        if (id == null || id.length() == 0)
            throw new IllegalArgumentException("invalid task id [" + id + "]");
        this.id = id;
    }

    public String asString() {
        return id;
    }
}
