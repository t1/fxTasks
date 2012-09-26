package fxtasks.model;

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;

public interface Task {

    public TaskId id();

    public <T> Property<T> getProperty(String name);

    public void addListener(InvalidationListener invalidationListener);

    public void removeListener(InvalidationListener invalidationListener);
}
