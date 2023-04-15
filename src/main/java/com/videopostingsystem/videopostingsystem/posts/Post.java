package com.videopostingsystem.videopostingsystem.posts;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @SequenceGenerator(
            name = "post_id_sequence",
            sequenceName = "post_id_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "post_id_sequence"
    )
    private Long id;

    private String users;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String title;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String body;

    private Long likes;

    private Long bookmarks;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    private Date creationDate = new Date();

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified_date")
    private Date lastModifiedDate = new Date();

    @Column(nullable = false)
    private String category;


    public Post(String users, String title, String body) {
        this.users = users;
        this.title = title;
        this.body = body;
    }

    public Post(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String user) {
        this.users = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String tag) {
        this.category = tag;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public Long getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(Long bookmarks) {
        this.bookmarks = bookmarks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equals(id, post.id) && Objects.equals(users, post.users) && Objects.equals(title, post.title) && Objects.equals(body, post.body) && Objects.equals(likes, post.likes) && Objects.equals(bookmarks, post.bookmarks) && Objects.equals(creationDate, post.creationDate) && Objects.equals(lastModifiedDate, post.lastModifiedDate) && Objects.equals(category, post.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, users, title, body, likes, bookmarks, creationDate, lastModifiedDate, category);
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", users='" + users + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", likes=" + likes +
                ", bookmarks=" + bookmarks +
                ", creationDate=" + creationDate +
                ", lastModifiedDate=" + lastModifiedDate +
                ", category='" + category + '\'' +
                '}';
    }
}
