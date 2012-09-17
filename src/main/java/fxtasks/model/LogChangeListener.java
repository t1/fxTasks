package fxtasks.model;

import javafx.beans.value.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class LogChangeListener implements ChangeListener<Object> {
    private final String format;

    public LogChangeListener(String propertyName) {
        format = propertyName + ": {} -> {}";
    }

    @Override
    public void changed(ObservableValue<?> property, Object oldValue, Object newValue) {
        log.debug(format, oldValue, newValue);
    }
}