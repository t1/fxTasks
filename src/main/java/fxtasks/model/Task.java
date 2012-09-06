package fxtasks.model;

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.beans.value.*;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.ImmutableList;

@Slf4j
@EqualsAndHashCode
public class Task {

    private final class LogChangeListener implements ChangeListener<Object> {
        private final String format;

        public LogChangeListener(String propertyName) {
            this.format = propertyName + " {} -> {}";
        }

        @Override
        public void changed(ObservableValue<?> property, Object oldValue, Object newValue) {
            log.debug(format, oldValue, newValue);
        }
    }

    public static Task create() {
        return new Task();
    }

    private final StringProperty title = new SimpleStringProperty();
    private final BooleanProperty done = new SimpleBooleanProperty();

    public Task() {
        title.addListener(new LogChangeListener("title"));
        done.addListener(new LogChangeListener("done"));
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String title() {
        return title.get();
    }

    public Task title(String newTitle) {
        title.set(newTitle);
        return this;
    }

    public BooleanProperty doneProperty() {
        return done;
    }

    public boolean done() {
        return done.get();
    }

    public Task done(boolean newDone) {
        done.set(newDone);
        return this;
    }

    public ImmutableList<Property<?>> getProperties() {
        return ImmutableList.<Property<?>> of(title, done);
    }

    public void removeListener(InvalidationListener invalidationListener) {
        for (Property<?> property : getProperties()) {
            property.removeListener(invalidationListener);
        }
    }

    public void addListener(InvalidationListener invalidationListener) {
        for (Property<?> property : getProperties()) {
            property.addListener(invalidationListener);
        }
    }
}
