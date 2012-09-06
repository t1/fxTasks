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
        @Override
        public void changed(ObservableValue<?> property, Object oldValue, Object newValue) {
            // String propertyName = ((ReadOnlyProperty<?>) property).getName();
            // log.debug("{}: {} -> {}", new Object[] { propertyName, oldValue, newValue });
        }
    }

    private StringProperty title;
    private final BooleanProperty done = new SimpleBooleanProperty();

    // done.addListener(new LogChangeListener());

    @Override
    public StringProperty titleProperty() {
        if (title == null) {
            title = new SimpleStringProperty(this, "title");
            title.addListener(new LogChangeListener());
        }
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
