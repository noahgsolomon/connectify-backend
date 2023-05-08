package com.videopostingsystem.videopostingsystem.posts.comments;

import com.google.gson.Gson;
import com.videopostingsystem.videopostingsystem.posts.PostRepository;
import com.videopostingsystem.videopostingsystem.users.UserRepository;
import com.videopostingsystem.videopostingsystem.users.Users;
import com.videopostingsystem.videopostingsystem.users.notification.NotificationService;
import com.videopostingsystem.videopostingsystem.users.notification.NotificationType;
import jakarta.servlet.http.HttpSession;
import com.videopostingsystem.videopostingsystem.posts.Post;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CommentService {

    PostRepository postRepository;
    CommentRepository commentRepository;
    UserRepository userRepository;
    @Autowired
    NotificationService notificationService;

    public ResponseEntity<?> createComment(Long postID, String content, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");

        if (loggedInUser == null || userRepository.findById(loggedInUser).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        if (postID == null || postRepository.findById(postID).isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Post does not exist");
        }
        Post post = postRepository.findById(postID).get();
        Users user = userRepository.findById(loggedInUser).get();
        Users postOwner = post.getUsers();
        Comment comment = new Comment(post, user, content);
        commentRepository.save(comment);
        notificationService.setNotification(user, postOwner, NotificationType.COMMENT, comment.getId());
        return ResponseEntity.ok("Created comment!");
    }

    public ResponseEntity<?> getPostComments(Long postID, HttpSession session){
        String loggedInUser = (String) session.getAttribute("loggedInUser");

        if (loggedInUser == null || userRepository.findById(loggedInUser).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        if (postID == null || postRepository.findById(postID).isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Post does not exist");
        }
        Post post = postRepository.findById(postID).get();
        List<Comment> postComments = commentRepository.findAllByPost(post);
        List<CommentResponseModel> formattedComments = new ArrayList<>();
        for (Comment comment : postComments){
            formattedComments.add(new CommentResponseModel(comment.getUser().getUsername(), comment.getContent(), comment.getCreationDate()));
        }
        Gson gson = new Gson();
        String json = gson.toJson(formattedComments);
        return ResponseEntity.ok(json);
    }
}