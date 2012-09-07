package fxtasks.model;

import javax.xml.bind.annotation.*;

import lombok.EqualsAndHashCode;

@XmlRootElement
@EqualsAndHashCode(callSuper = true)
class FileBasedTask extends AbstractTask {

    @XmlTransient
    private String id;

    @XmlAttribute
    private String nextId;

    @XmlAttribute
    private String previousId;

    public String id() {
        return id;
    }

    public FileBasedTask id(String newId) {
        this.id = newId;
        return this;
    }

    public String nextId() {
        return nextId;
    }

    public FileBasedTask nextId(String newNextId) {
        this.nextId = newNextId;
        return this;
    }

    public String previousId() {
        return previousId;
    }

    public FileBasedTask previousId(String newPreviousId) {
        this.previousId = newPreviousId;
        return this;
    }
}
