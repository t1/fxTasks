package fxtasks.model;

import static com.google.common.base.Charsets.*;

import java.io.*;
import java.nio.file.*;
import java.util.UUID;

import javafx.beans.*;
import javafx.collections.*;

import javax.annotation.Nullable;
import javax.xml.bind.JAXB;

import lombok.extern.slf4j.Slf4j;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;

@Slf4j
public class FileBasedTaskStore implements TaskStore {
    private final Function<String, LinkedTask> taskIdResolver = new Function<String, LinkedTask>() {
        @Override
        @Nullable
        public LinkedTask apply(@Nullable String id) {
            return getById(id);
        }
    };

    private static final Path ROOT_PATH = Paths.get("store");
    private static final Path FIRST_FILE_PATH = ROOT_PATH.resolve(".first");

    @VisibleForTesting
    final ObservableList<LinkedTask> taskList = FXCollections.observableArrayList();

    @Override
    public void addListener(ListChangeListener<Task> listChangeListener) {
        taskList.addListener(listChangeListener);
    }

    @Override
    public void load() {
        try {
            doLoad();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void doLoad() throws IOException {
        if (!Files.exists(ROOT_PATH))
            Files.createDirectories(ROOT_PATH);
        if (Files.exists(FIRST_FILE_PATH)) {
            String firstId = new String(Files.readAllBytes(FIRST_FILE_PATH));
            load(firstId);
        }
    }

    private void load(String id) throws IOException {
        String previousId = null;
        do {
            Path path = ROOT_PATH.resolve(id);
            try (Reader reader = Files.newBufferedReader(path, UTF_8)) {
                LinkedTask task = JAXB.unmarshal(reader, LinkedTask.class);
                assert task.previousId == previousId;
                task.resolver(taskIdResolver).id(id);
                add(task);
                previousId = id;
                id = task.nextId;
            }
        } while (id != null);
    }

    private void add(final LinkedTask task) {
        taskList.add(task);
        task.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                save(task);
            }
        });
    }

    @Override
    public LinkedTask create() {
        LinkedTask task = new LinkedTask().resolver(taskIdResolver).id(UUID.randomUUID().toString());
        if (taskList.isEmpty()) {
            add(task);
            saveFirst();
        } else {
            LinkedTask lastTask = lastTask();
            lastTask.next(task);
            task.previous(lastTask);
            save(lastTask);
            add(task);
        }
        return task;
    }

    @VisibleForTesting
    protected void saveFirst() {
        // TODO only if necessary
        try {
            Files.write(FIRST_FILE_PATH, taskList.get(0).id().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    protected void save(LinkedTask task) {
        log.debug("save: {} @ {}", task.title(), task.id());
        try (Writer writer = Files.newBufferedWriter(ROOT_PATH.resolve(task.id()), UTF_8)) {
            JAXB.marshal(task, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private LinkedTask lastTask() {
        return taskList.isEmpty() ? null : taskList.get(taskList.size() - 1);
    }

    @Override
    public void moveUp(Task task) {
        if (taskList.indexOf(task) > 0) {
            LinkedTask moving = (LinkedTask) task;
            LinkedTask swapped = moving.previous();
            LinkedTask previous = swapped.previous();
            LinkedTask next = moving.next();

            moveByOffset(moving, -1);

            if (previous != null)
                previous.next(moving);
            moving.next(swapped);
            swapped.next(next);
            // next.next remains

            // previous.previous remains
            moving.previous(previous);
            swapped.previous(moving);
            if (next != null)
                next.previous(swapped);

            if (previous == null)
                saveFirst();
            else
                save(previous);
            save(swapped);
            save(moving);
            if (next != null) {
                save(next);
            }
        } else {
            log.debug("{} is already first task", task);
        }
    }

    @Override
    public void moveDown(Task task) {
        if (taskList.indexOf(task) + 1 < taskList.size()) {
            LinkedTask moving = (LinkedTask) task;
            LinkedTask previous = moving.previous();
            LinkedTask swapped = moving.next();
            LinkedTask next = swapped.next();

            moveByOffset(moving, 1);

            if (previous == null)
                saveFirst();
            else
                previous.next(swapped);
            moving.next(next);
            swapped.next(moving);
            // next.next remains

            // previous.previous remains
            moving.previous(swapped);
            swapped.previous(previous);
            if (next != null)
                next.previous(moving);

            if (previous != null)
                save(previous);
            save(moving);
            save(swapped);
            if (next != null) {
                save(next);
            }
        } else {
            log.debug("{} is already last task", task);
        }
    }

    private void moveByOffset(LinkedTask task, int delta) {
        int index = taskList.indexOf(task);
        log.debug("move from {} to {}", index, index + delta);
        taskList.remove(index);
        taskList.add(index + delta, task);
        log.debug("--> {}", taskList);
    }

    public LinkedTask getById(String id) {
        if (id == null)
            return null;
        for (LinkedTask task : taskList) {
            if (id.equals(task.id())) {
                return task;
            }
        }
        throw new IllegalArgumentException("no task found with id " + id);
    }

    @Override
    public void moveIn(Task task) {
        log.debug("move in {}", task);
    }

    @Override
    public void moveOut(Task task) {
        log.debug("move out {}", task);
    }
}
