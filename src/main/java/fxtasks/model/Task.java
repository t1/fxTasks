package fxtasks.model;

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;

import com.google.common.collect.ImmutableList;

public interface Task {

    public StringProperty titleProperty();

    public String title();

    public Task title(String newTitle);

    public BooleanProperty doneProperty();

    public boolean done();

    public Task done(boolean newDone);

    public ImmutableList<Property<?>> getProperties();

    public void addListener(InvalidationListener invalidationListener);

    public void removeListener(InvalidationListener invalidationListener);
}
