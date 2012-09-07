package fxtasks.model;

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.beans.value.*;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.ImmutableList;

@Slf4j
@EqualsAndHashCode
abstract class AbstractTask implements Task {

    private final class LogChangeListener implements ChangeListener<Object> {
        private final String format;

        public LogChangeListener(String propertyName) {
            format = propertyName + ": {} -> {}";
        }

        @Override
        public void changed(ObservableValue<?> property, Object oldValue, Object newValue) {
            log.debug(format, oldValue, newValue);
        }
    }

    private final StringProperty title = new SimpleStringProperty();
    {
        title.addListener(new LogChangeListener("title"));
    }
    private final BooleanProperty done = new SimpleBooleanProperty();
    {
        done.addListener(new LogChangeListener("done"));
    }

    @Override
    public StringProperty titleProperty() {
        return title;
    }

    @Override
    public String title() {
        return titleProperty().get();
    }

    @Override
    public AbstractTask title(String newTitle) {
        titleProperty().set(newTitle);
        return this;
    }

    @Override
    public BooleanProperty doneProperty() {
        return done;
    }

    @Override
    public boolean done() {
        return done.get();
    }

    @Override
    public Task done(boolean newDone) {
        done.set(newDone);
        return this;
    }

    @Override
    public ImmutableList<Property<?>> getProperties() {
        return ImmutableList.<Property<?>> of(titleProperty(), done);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        for (Property<?> property : getProperties()) {
            property.removeListener(invalidationListener);
        }
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        for (Property<?> property : getProperties()) {
            property.addListener(invalidationListener);
        }
    }
}
