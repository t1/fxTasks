package fxtasks.model;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.UUID;

import javafx.beans.*;
import javafx.collections.*;

import javax.xml.bind.JAXB;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileBasedTaskStore implements TaskStore {
    private static final Path ROOT_PATH = Paths.get("store");
    private static final Path FIRST_FILE_PATH = ROOT_PATH.resolve(".first");

    private final ObservableList<FileBasedTask> taskList = FXCollections.observableArrayList();

    public FileBasedTaskStore(ListChangeListener<Task> listChangeListener) {
        taskList.addListener(listChangeListener);
        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void load() throws IOException {
        if (!Files.exists(ROOT_PATH))
            Files.createDirectories(ROOT_PATH);
        if (Files.exists(FIRST_FILE_PATH)) {
            String firstId = new String(Files.readAllBytes(FIRST_FILE_PATH));
            load(firstId);
        }
    }

    private void load(String id) throws IOException {
        do {
            Path path = ROOT_PATH.resolve(id);
            try (Reader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
                FileBasedTask task = JAXB.unmarshal(reader, FileBasedTask.class);
                task.id(id);
                add(task);
                id = task.nextId();
            }
        } while (id != null);
    }

    private void add(final FileBasedTask task) {
        taskList.add(task);
        task.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                save(task);
            }
        });
    }

    @Override
    public Task create() {
        FileBasedTask task = new FileBasedTask();
        task.id(UUID.randomUUID().toString());
        if (taskList.isEmpty()) {
            try {
                Files.write(FIRST_FILE_PATH, task.id().getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            FileBasedTask lastTask = lastTask();
            lastTask.nextId(task.id());
            task.previousId(lastTask.id());
            save(lastTask);
        }
        add(task);
        return task;
    }

    private void save(FileBasedTask task) {
        log.debug("save: {} @ {}", task.title(), task.id());
        try (Writer writer = Files.newBufferedWriter(ROOT_PATH.resolve(task.id()), Charset.forName("UTF-8"))) {
            JAXB.marshal(task, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FileBasedTask lastTask() {
        return taskList.isEmpty() ? null : taskList.get(taskList.size() - 1);
    }
}
