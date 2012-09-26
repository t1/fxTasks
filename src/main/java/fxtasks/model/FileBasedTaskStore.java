package fxtasks.model;

import static com.google.common.base.Charsets.*;
import static com.google.common.base.Preconditions.*;

import java.io.*;
import java.nio.file.*;

import javafx.beans.*;
import javafx.collections.*;

import javax.annotation.Nullable;
import javax.xml.bind.JAXB;

import lombok.extern.slf4j.Slf4j;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;

@Slf4j
public class FileBasedTaskStore implements TaskStore {
    private final Function<TaskId, LinkedTask> taskIdResolver = new Function<TaskId, LinkedTask>() {
        @Override
        @Nullable
        public LinkedTask apply(@Nullable TaskId id) {
            return getById(id);
        }
    };

    private static final Path ROOT_PATH;
    static {
        Path basePath = Paths.get(System.getProperty("user.home"));
        if ("Mac OS X".equals(System.getProperty("os.name")))
            basePath = basePath.resolve("Library/Application Support");
        ROOT_PATH = basePath.resolve("fxTasks/store");
    }

    @VisibleForTesting
    final ObservableList<LinkedTask> taskList = FXCollections.observableArrayList();

    @VisibleForTesting
    final ObservableMap<TaskId, TaskStore> childStores = FXCollections.observableHashMap();

    private final Path path;
    private final Path firstFilePath;

    public FileBasedTaskStore() {
        this(ROOT_PATH);
    }

    public FileBasedTaskStore(Path path) {
        this.path = path;
        this.firstFilePath = path.resolve(".first");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + path + "|" + taskList.size() + "|" + childStores.size() + "]";
    }

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
        if (!Files.exists(path))
            Files.createDirectories(path);
        if (Files.exists(firstFilePath)) {
            TaskId firstId = TaskId.of(Files.readAllBytes(firstFilePath));
            load(firstId);
        }
    }

    private void load(TaskId id) throws IOException {
        do {
            Path taskPath = path.resolve(id.asString());
            try (Reader reader = Files.newBufferedReader(taskPath, UTF_8)) {
                LinkedTask task = JAXB.unmarshal(reader, LinkedTask.class);
                task.resolver(taskIdResolver).id(id);
                add(task);
                id = task.nextId();
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
        LinkedTask task = new LinkedTask().resolver(taskIdResolver).id(TaskId.random());
        if (taskList.isEmpty()) {
            add(task);
            saveFirst();
        } else {
            LinkedTask lastTask = lastTask();
            lastTask.next(task);
            save(lastTask);
            add(task);
        }
        return task;
    }

    @VisibleForTesting
    protected void saveFirst() {
        // TODO only if necessary
        try {
            Files.createDirectories(firstFilePath.getParent());
            Files.write(firstFilePath, taskList.get(0).id().asString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    protected void removeFirst() {
        try {
            Files.delete(firstFilePath);
            Files.delete(firstFilePath.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    protected void save(LinkedTask task) {
        log.debug("save: {} @ {}", task.title(), task.id());
        try (Writer writer = Files.newBufferedWriter(getPath(task), UTF_8)) {
            JAXB.marshal(task, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getPath(Task task) {
        return path.resolve(task.id().asString());
    }

    private LinkedTask lastTask() {
        return taskList.isEmpty() ? null : taskList.get(taskList.size() - 1);
    }

    @Override
    public void moveUp(Task task) {
        int index = taskList.indexOf(task);
        if (index == 0) {
            log.debug("{} is already first task", task);
            return;
        }

        LinkedTask moving = (LinkedTask) task;
        LinkedTask swapped = taskList.get(index - 1);
        LinkedTask previous = (index == 1) ? null : taskList.get(index - 2);
        LinkedTask next = moving.next();

        moveByOffset(moving, -1);

        if (previous != null)
            previous.next(moving);
        moving.next(swapped);
        swapped.next(next);
        // next.next remains

        if (previous == null)
            saveFirst();
        else
            save(previous);
        save(swapped);
        save(moving);
        if (next != null) {
            save(next);
        }
    }

    @Override
    public void moveDown(Task task) {
        int index = taskList.indexOf(task);
        if (index + 1 >= taskList.size()) {
            log.debug("{} is already last task", task);
            return;
        }

        LinkedTask moving = (LinkedTask) task;
        LinkedTask previous = (index == 0) ? null : taskList.get(index - 1);
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

        if (previous != null)
            save(previous);
        save(moving);
        save(swapped);
        if (next != null) {
            save(next);
        }
    }

    private void moveByOffset(LinkedTask task, int delta) {
        int index = taskList.indexOf(task);
        log.debug("move from {} to {}", index, index + delta);
        taskList.remove(index);
        taskList.add(index + delta, task);
        log.debug("--> {}", taskList);
    }

    @Override
    public LinkedTask getById(TaskId id) {
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

    @Override
    public Task createChildOf(Task parent) {
        TaskId parentId = ((LinkedTask) parent).id();
        TaskStore childStore = childStores.get(parentId);
        if (childStore == null) {
            Path childPath = path.resolve(parentId.asString() + "@");
            childStore = createChildStore(childPath);
            childStores.put(parentId, childStore);
        }
        return childStore.create();
    }

    @VisibleForTesting
    FileBasedTaskStore createChildStore(Path childPath) {
        return new FileBasedTaskStore(childPath);
    }

    @Override
    public void remove(Task task) {
        checkNotNull(task);

        deleteTaskFile(task);

        boolean removed = taskList.remove(task);
        checkState(removed, "the task store at " + path + " doesn't contain a task " + task.id());
        if (taskList.isEmpty()) {
            removeFirst();
        } else {
            // TODO update first
        }
    }

    @VisibleForTesting
    void deleteTaskFile(Task task) {
        Path taskPath = getPath(task);
        try {
            Files.delete(taskPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeChildOf(Task parent, Task child) {
        TaskStore childStore = childStores.get(parent.id());
        checkNotNull(childStore, "task " + parent.id() + " has no children");
        childStore.remove(child);
    }
}
