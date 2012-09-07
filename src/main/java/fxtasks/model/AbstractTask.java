package fxtasks.model;

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.beans.value.*;

import javax.xml.bind.annotation.*;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.ImmutableList;

/**
 * Fluent, but "Straight forward" JavaFX style bean, that's JAXB-marshallable, except for the expanded property.
 */
@Slf4j
@XmlRootElement
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
    private final BooleanProperty expanded = new SimpleBooleanProperty();
    {
        expanded.addListener(new LogChangeListener("expanded"));
    }

    @Override
    public StringProperty titleProperty() {
        return title;
    }

    @Override
    public String title() {
        return titleProperty().get();
    }

    @XmlElement
    private String getTitle() {
        return title();
    }

    @SuppressWarnings("unused")
    private void setTitle(String newTitle) {
        title(newTitle);
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

    @XmlElement
    private boolean getDone() {
        return done();
    }

    @SuppressWarnings("unused")
    private void setDone(boolean newDone) {
        done(newDone);
    }

    @Override
    public Task done(boolean newDone) {
        done.set(newDone);
        return this;
    }

    @Override
    public Property<Boolean> expandedProperty() {
        return expanded;
    }

    @Override
    public boolean expanded() {
        return expandedProperty().getValue();
    }

    @Override
    public Task expanded(boolean newExpanded) {
        expandedProperty().setValue(newExpanded);
        return this;
    }

    public ImmutableList<Property<?>> getAllProperties() {
        return ImmutableList.<Property<?>> of(title, done, expanded);
    }

    @Override
    public ImmutableList<Property<?>> getProperties() {
        return ImmutableList.<Property<?>> of(title, done);
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[title=" + title.get() + "; done=" + done.get() + "]";
    }
}
