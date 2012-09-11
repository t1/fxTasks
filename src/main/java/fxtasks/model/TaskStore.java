package fxtasks.model;

import javafx.collections.ListChangeListener;

public interface TaskStore {
    public Task create();

    void addListener(ListChangeListener<Task> listener);

    public void load();

    public void moveUp(Task task);

    public void moveDown(Task task);

    public void moveIn(Task task);

    public void moveOut(Task task);
}
