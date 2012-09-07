package fxtasks.control;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.ListChangeListener;
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

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        newTaskMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.META_DOWN));

        taskStore.addListener(new ListChangeListener<Task>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Task> change) {
                while (change.next()) {
                    if (change.wasPermutated()) {
                        for (int i = change.getFrom(); i < change.getTo(); ++i) {
                            log.debug("permutate");
                        }
                    } else if (change.wasUpdated()) {
                        log.debug("update item");
                    } else {
                        for (Task removedTask : change.getRemoved()) {
                            log.debug("remove: {}", removedTask);
                            // removedTask.removeListener(invalidationListener);
                        }
                        for (Task addedTask : change.getAddedSubList()) {
                            TitledPane taskPane = TaskPaneBuilder.create().task(addedTask).build();
                            tasks.getChildren().add(taskPane);
                            // addedTask.addListener(invalidationListener);
                            taskPane.requestFocus();
                        }
                    }
                }
            }
        });

        collapseAllTasks();
    }

    @FXML
    public void createCategory() {
        log.debug("create category");
    }

    @FXML
    public void createTask() {
        collapseAllTasks();
        Task task = taskStore.create().title("New Task");
        taskStore.add(task);
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

    @FXML
    public void mainSwipeRight() {
        log.debug("main swipe right");
    }
}
