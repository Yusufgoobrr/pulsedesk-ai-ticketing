package org.pulsedesk.ticket;

import jakarta.persistence.*;
import org.pulsedesk.comment.Comment;

import java.util.Objects;

@Entity
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority;

    @Column(nullable = false)
    private String summary;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "comment_id", referencedColumnName = "id")
    private Comment comment;

    public Ticket() {
    }

    public Ticket(String title, TicketCategory category, TicketPriority priority, String summary) {
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.summary = summary;
    }

    public Ticket(Long id, String title, TicketCategory category, TicketPriority priority, String summary) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.summary = summary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Objects.equals(id, ticket.id) && Objects.equals(title, ticket.title) && category == ticket.category && priority == ticket.priority && Objects.equals(summary, ticket.summary) && Objects.equals(comment, ticket.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, category, priority, summary, comment);
    }
}
