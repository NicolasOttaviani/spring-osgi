package spring.osgi.example.item.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by nico.
 */
@Entity
public class Item {

    @Id
    @Column
    private String title;
    @Column
    private String text;

    @JsonCreator
    public Item(@JsonProperty("title") String title, @JsonProperty("text") String text) {
        Assert.notNull(title, "Title could not be null");
        this.title = title;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return String.format("Item[title=%s]",
                title);
    }

    @Deprecated
    protected Item() {
    }
}