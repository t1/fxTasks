package fxtasks.control;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.*;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import fxtasks.model.Task;

public class MainPaneController implements Initializable {
    @FXML
    private MenuItem newTaskMenuItem;

    @FXML
    private AnchorPane categories;

    @FXML
    private VBox tasks;

    private final ObservableList<Task> taskList = FXCollections.observableArrayList();

    private final InvalidationListener invalidationListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable arg0) {
            System.out.println("save");
        }
    };

    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        newTaskMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.META_DOWN));

        taskList.addListener(new ListChangeListener<Task>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Task> change) {
                while (change.next()) {
                    if (change.wasPermutated()) {
                        for (int i = change.getFrom(); i < change.getTo(); ++i) {
                            System.out.println("permutate");
                        }
                    } else if (change.wasUpdated()) {
                        System.out.println("update item");
                    } else {
                        for (Task removedTask : change.getRemoved()) {
                            System.out.println("remove: " + removedTask);
                            removedTask.removeListener(invalidationListener);
                        }
                        for (Task addedTask : change.getAddedSubList()) {
                            TitledPane taskPane = TaskPaneBuilder.create().task(addedTask).build();
                            tasks.getChildren().add(taskPane);
                            addedTask.addListener(invalidationListener);
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
        System.out.println("create category");
    }

    @FXML
    public void createTask() {
        collapseAllTasks();
        Task task = Task.create().title("New Task");
        taskList.add(task);
    }

    public void collapseAllTasks() {
        for (Node node : tasks.getChildren()) {
            TitledPane titledPane = (TitledPane) node;
            titledPane.setExpanded(false);
        }
    }

    @FXML
    public void minimize() {
        System.out.println("minimize");
    }

    @FXML
    public void zoom() {
        System.out.println("zoom");
    }

    @FXML
    public void mainSwipeRight() {
        System.out.println("main swipe right");
    }

    public void treeKey(KeyEvent event) {
        System.out.println("you typed " + event);
    }
}
