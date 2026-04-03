package org.pulsedesk.comment;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentSourceChannel sourceChannel;

    public Comment() {
    }

    public Comment(String comment, CommentSourceChannel sourceChannel) {
        this.comment = comment;
        this.sourceChannel = sourceChannel;
    }

    public Comment(Long id, String comment, CommentSourceChannel sourceChannel) {
        this.id = id;
        this.comment = comment;
        this.sourceChannel = sourceChannel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public CommentSourceChannel getSourceChannel() {
        return sourceChannel;
    }

    public void setSourceChannel(CommentSourceChannel sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment1 = (Comment) o;
        return Objects.equals(id, comment1.id) && Objects.equals(comment, comment1.comment) && sourceChannel == comment1.sourceChannel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, comment, sourceChannel);
    }
}
