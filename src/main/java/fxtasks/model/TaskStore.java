package fxtasks.model;

import javafx.collections.ListChangeListener;

public interface TaskStore {
    public Task create();

    public void delete(Task task);

    public void addListener(ListChangeListener<Task> listener);

    public void load();

    public void moveUp(Task task);

    public void moveDown(Task task);

    public void moveIn(Task task);

    public void moveOut(Task task);

    public Task createChildOf(Task parent);

    public void removeChildOf(Task parent, Task child);
}
