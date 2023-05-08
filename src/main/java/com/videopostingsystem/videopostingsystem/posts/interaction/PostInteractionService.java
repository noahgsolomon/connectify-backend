package com.videopostingsystem.videopostingsystem.posts.interaction;

import com.google.gson.Gson;
import com.videopostingsystem.videopostingsystem.posts.Post;
import com.videopostingsystem.videopostingsystem.posts.PostRepository;
import com.videopostingsystem.videopostingsystem.recommendSystem.FeedService;
import com.videopostingsystem.videopostingsystem.users.UserRepository;
import com.videopostingsystem.videopostingsystem.users.Users;
import com.videopostingsystem.videopostingsystem.users.notification.NotificationService;
import com.videopostingsystem.videopostingsystem.users.notification.NotificationType;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@AllArgsConstructor
public class PostInteractionService {
    PostRepository postRepository;
    UserRepository userRepository;
    PostInteractionRepository postInteractionRepository;
    @Autowired
    NotificationService notificationService;



    public ResponseEntity<?> postInteraction(Long postId, PostInteractionModel postInteraction, HttpSession session){
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        PostInteractions newPostInteraction;
        if (loggedInUser == null || userRepository.findById(loggedInUser).isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        Users user = userRepository.findById(loggedInUser).get();
        if (postRepository.findById(postId).isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid post ID provided");
        }
        Post post = postRepository.findById(postId).get();
        if (!postInteraction.liked() && !postInteraction.bookmark()) {
            if (postInteractionRepository.findById(postId + "_" + loggedInUser).isEmpty()){
                return null;
            }
            else {
                if (postInteractionRepository.findById(postId + "_" + loggedInUser).get().isLiked()){
                    notificationService.removeNotification(user, post.getUsers(), NotificationType.LIKE, postId);
                    post.setLikes(post.getLikes()-1);
                }
                if (postInteractionRepository.findById(postId + "_" + loggedInUser).get().isBookmark()){
                    post.setBookmarks(post.getBookmarks()-1);
                }
                postRepository.save(post);
                postInteractionRepository.deleteById(postId + "_" + loggedInUser);
                return ResponseEntity.ok().body("removed post " + postId + " from users interactions");
            }
        }
        if (postInteractionRepository.findById(postId + "_" + loggedInUser).isEmpty()){
            if (postInteraction.liked()){
                notificationService.setNotification(user, post.getUsers(), NotificationType.LIKE, postId);
                post.setLikes(post.getLikes()+1);
            }
            if (postInteraction.bookmark()){
                post.setBookmarks(post.getBookmarks()+1);
            }
            System.out.println(postInteraction.bookmark());
            System.out.println(postInteraction.liked());
        }

        newPostInteraction = new PostInteractions(postId, user, postInteraction.liked(), postInteraction.bookmark());
        postInteractionRepository.save(newPostInteraction);
        postRepository.save(post);
        if (user.getTopCategory() == null){
            user.setTopCategory(post.getCategory());
            userRepository.save(user);
        }
        else {
            if (!user.getTopCategory().equals(post.getCategory())){
                String category = Objects.requireNonNull(FeedService.likedCategories(session, userRepository, postRepository, postInteractionRepository))[0];
                assert category != null;
                if (!category.equals(user.getTopCategory())){
                    user.setTopCategory(category);
                    userRepository.save(user);
                }

            }
        }
        return ResponseEntity.ok(newPostInteraction);


    }

    public ResponseEntity<?> getPostInteraction(Long postId, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser == null || userRepository.findById(loggedInUser).isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        Gson gson = new Gson();
        PostInteractionModel postInteractionModel = new PostInteractionModel(false, false);

        String json = gson.toJson(postInteractionModel);
        if (postInteractionRepository.findById(postId + "_" + loggedInUser).isEmpty()){
            return ResponseEntity.ok().body(json);
        }
            boolean liked = postInteractionRepository.findById(postId + "_" + loggedInUser).get().isLiked();
            boolean bookmarked = postInteractionRepository.findById(postId + "_" + loggedInUser).get().isBookmark();
            postInteractionModel = new PostInteractionModel(liked, bookmarked);
            json = gson.toJson(postInteractionModel);
            return ResponseEntity.ok().body(json);
    }

    public ResponseEntity<?> getPostLikes(Long postId, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser == null || userRepository.findById(loggedInUser).isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        return ResponseEntity.ok(postInteractionRepository.findAllLikedByPostID(postId).size());
    }
}
