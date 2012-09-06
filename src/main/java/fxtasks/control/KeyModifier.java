package fxtasks.control;

import java.util.*;

import javafx.scene.input.KeyEvent;

public enum KeyModifier {
    SHIFT {
        @Override
        public boolean matches(KeyEvent event) {
            return event.isShiftDown();
        }
    },
    CONTROL {
        @Override
        public boolean matches(KeyEvent event) {
            return event.isControlDown();
        }
    },
    ALT {
        @Override
        public boolean matches(KeyEvent event) {
            return event.isAltDown();
        }
    },
    META {
        @Override
        public boolean matches(KeyEvent event) {
            return event.isMetaDown();
        }
    },
    SHORTCUT {
        @Override
        public boolean matches(KeyEvent event) {
            return event.isShortcutDown();
        }
    };

    public abstract boolean matches(KeyEvent event);

    public static EnumSet<KeyModifier> setOf(KeyModifier[] keyModifiers) {
        EnumSet<KeyModifier> set = EnumSet.noneOf(KeyModifier.class);
        set.addAll(Arrays.asList(keyModifiers));
        return set;
    }

    public static EnumSet<KeyModifier> of(KeyEvent event) {
        EnumSet<KeyModifier> set = EnumSet.noneOf(KeyModifier.class);
        for (KeyModifier modifier : KeyModifier.values()) {
            if (modifier.matches(event)) {
                set.add(modifier);
            }
        }
        return set;
    }
}
