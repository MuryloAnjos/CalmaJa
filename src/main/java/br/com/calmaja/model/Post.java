package br.com.calmaja.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "POSTS")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private int upvotes;
    private boolean isVerified;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }

    public Post(){

    }

    public Post(Long id,String title, String content, int upvotes, boolean isVerified, User createdBy, List<Comment> comments, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.upvotes = upvotes;
        this.isVerified = isVerified;
        this.createdBy = createdBy;
        this.comments = comments;
        this.createdAt = createdAt;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setIsVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public int commentsCount(){
        return comments.size();
    }
}
