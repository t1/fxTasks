package fxtasks.model;

import java.nio.file.*;

import javafx.beans.*;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.*;
import lombok.Delegate;

public class FileBasedTaskStore implements TaskStore {

    @Delegate
    private final ObservableList<Task> taskList = FXCollections.observableArrayList();

    private class SaveListener implements InvalidationListener {
        @Override
        public void invalidated(Observable observable) {
            ReadOnlyProperty<?> property = (ReadOnlyProperty<?>) observable;
            System.out.println("save: " + property.getName() + "@" + FileBasedTaskStore.this.rootPath.toAbsolutePath());
        }
    }

    private final Path rootPath = Paths.get("store");

    @Override
    public Task create() {
        FileBasedTask task = new FileBasedTask();
        task.addListener(new SaveListener());
        return task;
    }
}
