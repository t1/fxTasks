package fxtasks.control;

import static fxtasks.control.KeyModifier.*;

import java.util.EnumSet;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import fxtasks.model.Task;

public class TaskPaneController {
    public static TaskPaneController of(TitledPane pane) {
        return (TaskPaneController) pane.getUserData();
    }

    private enum KeyBinding {
        LEFT(KeyCode.LEFT) {
            @Override
            public void execute(TaskPaneController controller) {
                if (controller.pane.isExpanded())
                    controller.pane.setExpanded(false);
                // else con
            }
        },
        RIGHT(KeyCode.RIGHT) {
            @Override
            public void execute(TaskPaneController controller) {
                controller.pane.setExpanded(true);
            }
        },
        ALT_LEFT(KeyCode.LEFT, ALT) {
            @Override
            public void execute(TaskPaneController controller) {
                if (controller.pane.isExpanded())
                    controller.pane.setExpanded(false);
                // else
            }
        },
        ALT_RIGHT(KeyCode.RIGHT, ALT) {
            @Override
            public void execute(TaskPaneController controller) {
                controller.pane.setExpanded(true);
            }
        },

        UP(KeyCode.UP) {
            @Override
            public void execute(TaskPaneController controller) {
                int index = controller.getIndex();
                if (index > 0) {
                    controller.getSiblings().get(index - 1).requestFocus();
                }
            }
        },
        DOWN(KeyCode.DOWN) {
            @Override
            public void execute(TaskPaneController controller) {
                int index = controller.getIndex();
                if (index < controller.getSiblingCount() - 1) {
                    controller.getSiblings().get(index + 1).requestFocus();
                }
            }
        },

        SHIFT_UP(KeyCode.UP, SHIFT) {
            @Override
            public void execute(TaskPaneController controller) {
                int index = controller.getIndex();
                if (index > 0) {
                    controller.getSiblings().get(index - 1).requestFocus();
                }
            }
        },
        SHIFT_DOWN(KeyCode.DOWN, SHIFT) {
            @Override
            public void execute(TaskPaneController controller) {
                int index = controller.getIndex();
                if (index < controller.getSiblingCount() - 1) {
                    controller.getSiblings().get(index + 1).requestFocus();
                }
            }
        },

        ALT_UP(KeyCode.UP, ALT) {
            @Override
            public void execute(TaskPaneController controller) {
                controller.moveSibling(-1);
            }
        },
        ALT_DOWN(KeyCode.DOWN, ALT) {
            @Override
            public void execute(TaskPaneController controller) {
                controller.moveSibling(+1);
            }
        },
        ;

        public final KeyCode code;
        public final EnumSet<KeyModifier> keyModifiers;

        private KeyBinding(KeyCode code, KeyModifier... keyModifiers) {
            this.code = code;
            this.keyModifiers = KeyModifier.setOf(keyModifiers);
        }

        public boolean matches(KeyEvent event) {
            return code.equals(event.getCode()) && KeyModifier.of(event).equals(keyModifiers);
        }

        public abstract void execute(TaskPaneController controller);
    }

    private final Task task;
    private final TitledPane pane;

    public TaskPaneController(Task task, TitledPane pane) {
        this.task = task;
        this.pane = pane;
    }

    public void handle(KeyEvent event) {
        for (KeyBinding binding : KeyBinding.values()) {
            if ("KEY_RELEASED".equals(event.getEventType().getName()) && binding.matches(event)) {
                binding.execute(this);
                return;
            }
        }
    }

    protected int getIndex() {
        ObservableList<Node> siblings = getSiblings();
        for (int i = 0; i < siblings.size(); i++) {
            if (pane == siblings.get(i)) {
                return i;
            }
        }
        throw new IllegalStateException();
    }

    protected int getSiblingCount() {
        return getSiblings().size();
    }

    protected ObservableList<Node> getSiblings() {
        Pane parent = (Pane) pane.getParent();
        return parent.getChildren();
    }

    protected void moveSibling(int offset) {
        ObservableList<Node> siblings = getSiblings();
        int index = getIndex();
        siblings.remove(index);
        siblings.add(index + offset, pane);
    }

    public TextField getTitleField() {
        Pane graphic = (Pane) pane.getGraphic();
        return (TextField) graphic.getChildren().get(0);
    }
}
