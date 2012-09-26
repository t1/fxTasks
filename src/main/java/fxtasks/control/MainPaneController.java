package fxtasks.control;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.*;
import javafx.collections.ListChangeListener.Change;
import javafx.fxml.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import lombok.extern.slf4j.Slf4j;
import fxtasks.model.*;

@Slf4j
public class MainPaneController implements Initializable {
    @FXML
    private MenuItem newTaskMenuItem;

    @FXML
    private AnchorPane categories;

    @FXML
    private VBox tasks;

    private final TaskStore taskStore = new FileBasedTaskStore();

    public enum ChangeType {
        PERMUTATE {
            @Override
            public boolean isType(Change<?> change) {
                return change.wasPermutated();
            }
        },
        UPDATE {
            @Override
            public boolean isType(Change<?> change) {
                return change.wasUpdated();
            }
        },
        REPLACE {
            @Override
            public boolean isType(Change<?> change) {
                return change.wasReplaced();
            }
        },
        REMOVE {
            @Override
            public boolean isType(Change<?> change) {
                return change.wasRemoved();
            }
        },
        ADD {
            @Override
            public boolean isType(Change<?> change) {
                return change.wasAdded();
            }
        };

        public static ChangeType of(Change<?> change) {
            ChangeType result = null;
            for (ChangeType type : values()) {
                if (type.isType(change)) {
                    if (result != null)
                        throw new IllegalStateException("change is a " + result + " as well as a " + type);
                    result = type;
                }
            }
            if (result == null)
                throw new IllegalStateException("change is no known type");
            return result;
        }

        public abstract boolean isType(Change<?> change);
    }

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        newTaskMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.META_DOWN));

        taskStore.addListener(new ListChangeListener<Task>() {
            @Override
            public void onChanged(Change<? extends Task> change) {
                while (change.next()) {
                    ChangeType type = ChangeType.of(change);
                    log.debug("{} [{} to {})", new Object[] { type, change.getFrom(), change.getTo() });
                    switch (type) {
                    case ADD:
                        addTaskChange(change);
                        break;
                    case REMOVE:
                        removeTaskChange(change);
                        break;
                    default:
                        log.debug("unhandled change type {}", type);
                        break;
                    }
                }
            }
        });
        taskStore.load();
        collapseAllTasks();
    }

    @FXML
    public void createCategory() {
        log.debug("create category");
    }

    @FXML
    public void createTask() {
        collapseAllTasks();
        taskStore.create().<String> getProperty("title").setValue("New Task");
    }

    public void collapseAllTasks() {
        for (Node node : tasks.getChildren()) {
            TitledPane titledPane = (TitledPane) node;
            titledPane.setExpanded(false);
        }
    }

    @FXML
    public void minimize() {
        log.debug("minimize");
    }

    @FXML
    public void zoom() {
        log.debug("zoom");
    }

    private void addTaskChange(Change<? extends Task> change) {
        int offset = change.getFrom();
        for (Task addedTask : change.getAddedSubList()) {
            log.debug("add {} at {}", addedTask, offset);
            TitledPane taskPane = TaskPaneBuilder.create().task(addedTask).taskStore(taskStore).build();
            tasks.getChildren().add(offset++, taskPane);
            taskPane.requestFocus();
            TextField titleField = TaskPaneController.of(taskPane).getTitleField();
            taskPane.setExpanded(true);
            titleField.selectAll();
            titleField.requestFocus();
        }
    }

    private void removeTaskChange(Change<? extends Task> change) {
        for (Task removedTask : change.getRemoved()) {
            log.debug("remove {}", removedTask);
            TaskPaneController pane = TaskPaneController.of(tasks, removedTask);
            if (pane == null)
                throw new IllegalStateException("no pane found for task: " + removedTask);
            pane.remove();
        }
    }
}
