package fxtasks.model;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.*;

public class FileBasedTaskStoreTest {

    // TODO this is getting ugly... use real IO instead (or maybe mock the FileSystem?)
    private class TestFileBasedTaskStore extends FileBasedTaskStore {
        public TestFileBasedTaskStore() {
            super();
        }

        public TestFileBasedTaskStore(Path path) {
            super(path);
        }

        @Override
        protected void save(LinkedTask task) {
            if (!saved.contains(task)) {
                saved.add(task);
            }
        }

        @Override
        protected void saveFirst() {
            firstSaved = true;
        }

        @Override
        protected void removeFirst() {
            firstRemoved = true;
        }

        @Override
        FileBasedTaskStore createChildStore(Path childPath) {
            return new TestFileBasedTaskStore(childPath);
        }

        @Override
        void deleteTaskFile(Task task) {
            deleted.add((LinkedTask) task);
        }
    }

    private final List<LinkedTask> saved = Lists.newArrayList();
    private boolean firstSaved = false;

    private final List<LinkedTask> deleted = Lists.newArrayList();
    private boolean firstRemoved = false;

    private final FileBasedTaskStore store = new TestFileBasedTaskStore();

    @Test
    public void shouldCreate() throws Exception {
        Task created = store.create().title("title");

        assertEquals("title", created.<String> getProperty("title").getValue());
        assertEquals(created, saved.iterator().next());
        assertTrue(firstSaved);
    }

    @Test
    public void shouldRemoveOne() throws Exception {
        Task created = store.create().title("title");
        resetSaved();

        store.remove(created);

        assertEquals(0, store.taskList.size());
        assertTrue(firstRemoved);
    }

    @Test
    public void shouldCreate4() throws Exception {
        Task one = store.create().title("one");
        Task two = store.create().title("two");
        Task three = store.create().title("three");
        Task four = store.create().title("four");

        assertTrue(firstSaved);
        assertSaved(one, two, three, four);
        assertEquals(ImmutableList.of(one, two, three, four), store.taskList);
    }

    private void assertSaved(Task... tasks) {
        assertEquals(tasks.length, saved.size());
        int i = 0;
        for (Task task : tasks) {
            assertEquals(task, saved.get(i++));
        }
    }

    private void resetSaved() {
        saved.clear();
        firstSaved = false;
    }

    @Test
    public void shouldMoveDown() throws Exception {
        LinkedTask one = store.create().title("one");
        LinkedTask two = store.create().title("two");
        LinkedTask three = store.create().title("three");
        LinkedTask four = store.create().title("four");
        resetSaved();

        store.moveDown(two);

        assertFalse(firstSaved);
        assertSaved(one, two, three, four);
        assertEquals(ImmutableList.of(one, three, two, four), store.taskList);

        assertEquals(three, one.next());
        assertEquals(four, two.next());
        assertEquals(two, three.next());
        assertEquals(null, four.next());
    }

    @Test
    public void shouldMoveDownToLast() throws Exception {
        LinkedTask one = store.create().title("one");
        LinkedTask two = store.create().title("two");
        LinkedTask three = store.create().title("three");
        resetSaved();

        store.moveDown(two);

        assertFalse(firstSaved);
        assertSaved(one, two, three);
        assertEquals(ImmutableList.of(one, three, two), store.taskList);

        assertEquals(three, one.next());
        assertEquals(null, two.next());
        assertEquals(two, three.next());
    }

    @Test
    public void shouldMoveDownFromFirst() throws Exception {
        LinkedTask one = store.create().title("one");
        LinkedTask two = store.create().title("two");
        LinkedTask three = store.create().title("three");
        resetSaved();

        store.moveDown(one);

        assertTrue(firstSaved);
        assertSaved(one, two, three);
        assertEquals(ImmutableList.of(two, one, three), store.taskList);

        assertEquals(three, one.next());
        assertEquals(one, two.next());
        assertEquals(null, three.next());
    }

    @Test
    public void shouldNotMoveLastTaskDown() throws Exception {
        Task one = store.create().title("one");
        Task two = store.create().title("two");
        Task three = store.create().title("three");
        Task four = store.create().title("four");
        resetSaved();

        store.moveDown(four);

        assertEquals(0, saved.size());
        assertEquals(ImmutableList.of(one, two, three, four), store.taskList);
        assertFalse(firstSaved);
    }

    @Test
    public void shouldMoveUp() throws Exception {
        LinkedTask one = store.create().title("one");
        LinkedTask two = store.create().title("two");
        LinkedTask three = store.create().title("three");
        LinkedTask four = store.create().title("four");
        resetSaved();

        store.moveUp(three);

        assertFalse(firstSaved);
        assertSaved(one, two, three, four);
        assertEquals(ImmutableList.of(one, three, two, four), store.taskList);

        assertEquals(three, one.next());
        assertEquals(four, two.next());
        assertEquals(two, three.next());
        assertEquals(null, four.next());
    }

    @Test
    public void shouldMoveUpToFirst() throws Exception {
        LinkedTask one = store.create().title("one");
        LinkedTask two = store.create().title("two");
        LinkedTask three = store.create().title("three");
        resetSaved();

        store.moveUp(two);

        assertTrue(firstSaved);
        assertSaved(one, two, three);
        assertEquals(ImmutableList.of(two, one, three), store.taskList);

        assertEquals(three, one.next());
        assertEquals(one, two.next());
        assertEquals(null, three.next());
    }

    @Test
    public void shouldMoveUpFromLast() throws Exception {
        LinkedTask one = store.create().title("one");
        LinkedTask two = store.create().title("two");
        LinkedTask three = store.create().title("three");
        resetSaved();

        store.moveUp(three);

        assertFalse(firstSaved);
        assertSaved(one, two, three);
        assertEquals(ImmutableList.of(one, three, two), store.taskList);

        assertEquals(three, one.next());
        assertEquals(null, two.next());
        assertEquals(two, three.next());
    }

    @Test
    public void shouldNotMoveFirstTaskUp() throws Exception {
        Task one = store.create().title("one");
        Task two = store.create().title("two");
        Task three = store.create().title("three");
        Task four = store.create().title("four");
        resetSaved();

        store.moveUp(one);

        assertEquals(0, saved.size());
        assertEquals(ImmutableList.of(one, two, three, four), store.taskList);
    }

    @Test
    public void shouldAddSubtask() throws Exception {
        LinkedTask one = store.create().title("one");
        resetSaved();

        Task sub1 = store.createChildOf(one);
        sub1.<String> getProperty("title").setValue("sub1");

        assertEquals(ImmutableList.of(sub1), saved);
        assertEquals(ImmutableList.of(one), store.taskList);
        assertEquals(ImmutableList.of(sub1), ((FileBasedTaskStore) store.childStores.get(one.id())).taskList);
    }

    @Test
    public void shouldRemoveSubtask() throws Exception {
        LinkedTask one = store.create().title("one");
        Task sub1 = store.createChildOf(one);
        sub1.<String> getProperty("title").setValue("sub1");
        resetSaved();

        store.removeChildOf(one, sub1);

        assertEquals(0, saved.size());
        assertEquals(ImmutableList.of(one), store.taskList);
        assertEquals(ImmutableList.of(), ((FileBasedTaskStore) store.childStores.get(one.id())).taskList);
    }
}
