package fxtasks.control;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import com.google.common.collect.ImmutableList;

import fxtasks.model.*;

public class TaskPaneBuilder {

    private static class TaskPaneKeyEventHandler implements EventHandler<KeyEvent> {
        @Override
        public void handle(KeyEvent event) {
            TitledPane pane = (TitledPane) event.getSource();
            TaskPaneController.of(pane).handle(event);
        }
    }

    private static final DropShadowBuilder<?> SHADOW = DropShadowBuilder.create().offsetX(5).offsetY(5).blurType(
            BlurType.THREE_PASS_BOX).color(Color.color(0, 0, 0, .3));

    public static TaskPaneBuilder create() {
        return new TaskPaneBuilder();
    }

    private Task task;
    private TaskStore store;

    public TaskPaneBuilder taskStore(TaskStore taskStore) {
        if (store != null)
            throw new IllegalStateException("task store already set");
        if (taskStore == null)
            throw new NullPointerException();
        this.store = taskStore;
        return this;
    }

    public TaskPaneBuilder task(Task newTask) {
        if (task != null)
            throw new IllegalStateException("task already set");
        if (newTask == null)
            throw new NullPointerException();
        this.task = newTask;
        return this;
    }

    public TitledPane build() {
        if (task == null)
            throw new IllegalStateException("set task before calling build()");
        if (store == null)
            throw new IllegalStateException("set task store before calling build()");
        TitledPane taskPane = TitledPaneBuilder.create().animated(true).effect(SHADOW.build()) //
        .content(buildContent()).graphic(buildChildren()) //
        .onKeyReleased(new TaskPaneKeyEventHandler()).build();
        taskPane.setUserData(new TaskPaneController(store, task, taskPane));
        return taskPane;
    }

    private AnchorPane buildContent() {
        return AnchorPaneBuilder.create().prefHeight(100).minWidth(200).children().build();
    }

    private Node buildChildren() {
        return HBoxBuilder.create().children(ImmutableList.of(buildTitle(), buildDone())).build();
    }

    private TextField buildTitle() {
        TextField title = TextFieldBuilder.create().build();
        title.textProperty().bindBidirectional(task.titleProperty());
        return title;
    }

    private CheckBox buildDone() {
        CheckBox done = CheckBoxBuilder.create().build();
        HBox.setMargin(done, new Insets(3));
        done.selectedProperty().bindBidirectional(task.doneProperty());
        return done;
    }
}
