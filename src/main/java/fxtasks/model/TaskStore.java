package fxtasks.model;

import javafx.collections.ObservableList;

public interface TaskStore extends ObservableList<Task> {

    public Task create();

}
