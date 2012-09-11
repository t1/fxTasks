package fxtasks.model;

import javax.xml.bind.annotation.*;

import com.google.common.base.Function;

@XmlRootElement
class LinkedTask extends AbstractTask {

    @XmlTransient
    private String id;

    @XmlAttribute
    String nextId;

    @XmlAttribute
    String previousId;

    private transient Function<String, LinkedTask> resolver;

    public LinkedTask resolver(Function<String, LinkedTask> resolver) {
        this.resolver = resolver;
        return this;
    }

    @Override
    public LinkedTask title(String title) {
        super.title(title);
        return this;
    }

    public String id() {
        return id;
    }

    LinkedTask id(String id) {
        this.id = id;
        return this;
    }

    public LinkedTask next() {
        return resolver.apply(nextId);
    }

    public LinkedTask next(LinkedTask next) {
        this.nextId = (next == null) ? null : next.id;
        return this;
    }

    public LinkedTask previous() {
        return resolver.apply(previousId);
    }

    public LinkedTask previous(LinkedTask previous) {
        this.previousId = (previous == null) ? null : previous.id;
        return this;
    }

    public boolean isFirst() {
        return previousId == null;
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
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LinkedTask that = (LinkedTask) obj;
        return this.id.equals(that.id);
    }
}