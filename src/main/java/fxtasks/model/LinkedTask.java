package fxtasks.model;

import javax.xml.bind.annotation.*;

import com.google.common.base.Function;

@XmlRootElement
class LinkedTask extends AbstractTask {

    /** the id is the file name, not /in/ the file */
    @XmlTransient
    private TaskId id;

    @XmlAttribute
    String nextId;

    private transient Function<TaskId, LinkedTask> resolver;

    public LinkedTask resolver(Function<TaskId, LinkedTask> resolver) {
        this.resolver = resolver;
        return this;
    }

    @Override
    public TaskId id() {
        return id;
    }

    LinkedTask id(TaskId id) {
        this.id = id;
        return this;
    }

    public TaskId nextId() {
        return (nextId == null) ? null : TaskId.of(nextId);
    }

    public LinkedTask next() {
        return resolver.apply(nextId());
    }

    public LinkedTask next(LinkedTask next) {
        this.nextId = (next == null) ? null : next.id.asString();
        return this;
    }

    public boolean isLast() {
        return nextId == null;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        LinkedTask that = (LinkedTask) obj;
        return this.id.equals(that.id);
    }

    public String title() {
        return this.<String> getProperty("title").getValue();
    }

    public LinkedTask title(String title) {
        this.<String> getProperty("title").setValue(title);
        return this;
    }
}
