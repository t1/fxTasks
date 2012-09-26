package fxtasks.model;

import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;

import javax.xml.bind.annotation.*;

import com.google.common.collect.Maps;

/**
 * Fluent, but "Straight forward" JavaFX style bean, that's JAXB-marshallable.
 */
@XmlRootElement
abstract class AbstractTask implements Task {

    private final Map<String, Property<?>> properties = Maps.newTreeMap();

    public AbstractTask() {
        properties.put("title", new SimpleStringProperty(this, "title"));
        properties.put("done", new SimpleBooleanProperty(this, "done"));

        for (Property<?> property : properties.values()) {
            property.addListener(new LogChangeListener(property.getName()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Property<T> getProperty(String name) {
        return (Property<T>) properties.get(name);
    }

    // TODO generify the JAXB stuff

    @XmlElement
    private String getTitle() {
        return getTitleProperty().getValue();
    }

    // required for JAXB
    @SuppressWarnings("unused")
    private void setTitle(String title) {
        getTitleProperty().setValue(title);
    }

    private Property<String> getTitleProperty() {
        return this.<String> getProperty("title");
    }

    @XmlElement
    private boolean getDone() {
        return getDoneProperty().getValue();
    }

    // required for JAXB
    @SuppressWarnings("unused")
    private void setDone(boolean done) {
        getDoneProperty().setValue(done);
    }

    private Property<Boolean> getDoneProperty() {
        return this.<Boolean> getProperty("done");
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        for (Property<?> property : properties.values()) {
            property.addListener(invalidationListener);
        }
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        for (Property<?> property : properties.values()) {
            property.removeListener(invalidationListener);
        }
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(getClass().getSimpleName());
        out.append('[');
        for (Property<?> property : properties.values()) {
            out.append(property.getName());
            out.append('=');
            out.append(property.getValue());
        }
        out.append(']');
        return out.toString();
    }
}
