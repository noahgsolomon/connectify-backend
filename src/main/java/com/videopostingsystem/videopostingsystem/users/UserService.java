package com.videopostingsystem.videopostingsystem.users;

import com.google.gson.Gson;
import com.videopostingsystem.videopostingsystem.inbox.Inbox;
import com.videopostingsystem.videopostingsystem.inbox.InboxRepository;
import com.videopostingsystem.videopostingsystem.inbox.messagelog.MessageLog;
import com.videopostingsystem.videopostingsystem.inbox.messagelog.MessageLogRepository;
import com.videopostingsystem.videopostingsystem.posts.Post;
import com.videopostingsystem.videopostingsystem.posts.PostRepository;
import com.videopostingsystem.videopostingsystem.posts.comments.Comment;
import com.videopostingsystem.videopostingsystem.posts.comments.CommentRepository;
import com.videopostingsystem.videopostingsystem.posts.interaction.PostInteractionRepository;
import com.videopostingsystem.videopostingsystem.posts.interaction.PostInteractions;
import com.videopostingsystem.videopostingsystem.users.token.ConfirmationToken;
import com.videopostingsystem.videopostingsystem.users.token.ConfirmationTokenRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostInteractionRepository postInteractionRepository;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final MessageLogRepository messageLogRepository;
    private final InboxRepository inboxRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public ResponseEntity<?> deleteAccount(HttpSession session){
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (session.getAttribute("loggedInUser") == null || userRepository.findById(loggedInUser).isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        Users user = userRepository.findById(loggedInUser).get();

        List<PostInteractions> postInteractions = postInteractionRepository.findAllByUsers(user);
        for (PostInteractions postInteraction : postInteractions){
            Post post = postRepository.findById(postInteraction.getPostID()).get();
            if (postInteraction.isLiked()){
                post.setLikes(post.getLikes()-1);
            }
            if (postInteraction.isBookmark()){
                post.setBookmarks(post.getBookmarks()-1);
            }
            postRepository.save(post);
        }
        postInteractionRepository.deleteAllByUsers(user);

        List<Post> posts = postRepository.findAllByUsers(user);
        for (Post currPost : posts){
            List<Comment> comments = commentRepository.findAllByPost(currPost);

            for (Comment comment : comments){
                commentRepository.deleteById(comment.getId());
            }

            postInteractionRepository.deleteAllByPostID(currPost.getId());
            postRepository.deleteById(currPost.getId());
        }
        List<ConfirmationToken> confirmationTokens = confirmationTokenRepository.findAllByUsers(user);
        for (ConfirmationToken token : confirmationTokens){
            confirmationTokenRepository.deleteByToken(token.getToken());
        }
        List<Inbox> inboxes = new ArrayList<>();
        if (!inboxRepository.findAllByUser1(user).isEmpty()){
            List<Inbox> inboxes1 = inboxRepository.findAllByUser1(user);
            inboxes.addAll(inboxes1);
        }
        if (!inboxRepository.findAllByUser2(user).isEmpty()){
            List<Inbox> inboxes2 = inboxRepository.findAllByUser2(user);
            inboxes.addAll(inboxes2);
        }
        for (Inbox inbox: inboxes){
            List<MessageLog> messageLogs = messageLogRepository.findAllByInbox(inbox);
            for (MessageLog messageLog : messageLogs){
                messageLogRepository.deleteById(messageLog.getMessage_id());
            }
            inboxRepository.deleteById(inbox.getInboxId());
        }

        userRepository.deleteById(loggedInUser);
        return ResponseEntity.ok("Successfully deleted account. We're sad to see you go, " + loggedInUser + "!");

    }

    @Transactional
    public ResponseEntity<?> deleteAccountAdmin(String deletedUser, HttpSession session) {
        String adminAccount = (String) session.getAttribute("loggedInUser");
        if (adminAccount == null || userRepository.findById(adminAccount).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        if (userRepository.findById(deletedUser).isEmpty()) {
            return ResponseEntity.badRequest().body("user does not exist");
        }
        Users user = userRepository.findById(deletedUser).get();
        if (!userRepository.findById(adminAccount).get().getType().equals(UserType.ADMIN)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to delete this users account!");
        }

        List<PostInteractions> postInteractions = postInteractionRepository.findAllByUsers(userRepository.findById(deletedUser).get());
        for (PostInteractions currPostInteraction : postInteractions) {
            postInteractionRepository.deleteById(currPostInteraction.getPostID() + "_" + currPostInteraction.getUsers().getUsername());
        }
        List<Post> posts = postRepository.findAllByUsers(userRepository.findById(deletedUser).get());
        for (Post currPost : posts) {
            postRepository.deleteById(currPost.getId());
        }
        List<ConfirmationToken> confirmationTokens = confirmationTokenRepository.findAllByUsers(userRepository.findById(deletedUser).get());
        for (ConfirmationToken token : confirmationTokens) {
            confirmationTokenRepository.deleteByToken(token.getToken());
        }
        List<Inbox> inboxes = new ArrayList<>();
        if (!inboxRepository.findAllByUser1(user).isEmpty()){
            List<Inbox> inboxes1 = inboxRepository.findAllByUser1(user);
            inboxes.addAll(inboxes1);
        }
        if (!inboxRepository.findAllByUser2(user).isEmpty()){
            List<Inbox> inboxes2 = inboxRepository.findAllByUser2(user);
            inboxes.addAll(inboxes2);
        }
        for (Inbox inbox: inboxes){
            List<MessageLog> messageLogs = messageLogRepository.findAllByInbox(inbox);
            for (MessageLog messageLog : messageLogs){
                messageLogRepository.deleteById(messageLog.getMessage_id());
            }
            inboxRepository.deleteById(inbox.getInboxId());
        }
        userRepository.deleteById(deletedUser);
        return ResponseEntity.ok("Successfully deleted user " + deletedUser);
    }

    public ResponseEntity<?> getUsername(HttpSession session){
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser == null || userRepository.findById(loggedInUser).isEmpty()){
            return ResponseEntity.badRequest().body("User not logged in");
        }
        return ResponseEntity.ok(loggedInUser);

    }

    public ResponseEntity<?> getProfile(HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser == null || userRepository.findById(loggedInUser).isEmpty()){
            return ResponseEntity.badRequest().body("User not logged in");
        }
        Gson gson = new Gson();
        Users user = userRepository.findById(loggedInUser).get();
        String json = gson.toJson(user);
        System.out.println(json);
        return ResponseEntity.ok(json);
    }

    public ResponseEntity<?> updateProfile(UpdateProfileModel updateProfileModel, HttpSession session){
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser == null || userRepository.findById(loggedInUser).isEmpty()){
            return ResponseEntity.badRequest().body("User not logged in");
        }
        Gson gson = new Gson();
        Users users = userRepository.findById(loggedInUser).get();
        if (updateProfileModel.bio() != null && !updateProfileModel.bio().equals("")){
            users.setBio(updateProfileModel.bio());
        }
        if (updateProfileModel.country() != null && !updateProfileModel.country().equals("")){
            users.setCountry(updateProfileModel.country());
        }
        System.out.println(updateProfileModel.profilePic());
        if (updateProfileModel.profilePic() != null && !updateProfileModel.profilePic().equals("")){
            users.setProfilePic(updateProfileModel.profilePic());
        }
        users.setCardColor(updateProfileModel.cardColor());
        users.setBackgroundColor(updateProfileModel.backgroundColor());

        userRepository.save(users);
        String json = gson.toJson(users);
        return ResponseEntity.ok(json);
    }

    public ResponseEntity<?> getUserProfile(String user, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser == null || userRepository.findById(loggedInUser).isEmpty()){
            return ResponseEntity.badRequest().body("User not logged in");
        }
        if (user == null || userRepository.findById(user).isEmpty()){
            return ResponseEntity.badRequest().body("User does not exist");
        }
        Gson gson = new Gson();
        Users userDetails = userRepository.findById(user).get();
        UserProfileModel userProfileModel = new UserProfileModel(userDetails.getUsername(), userDetails.getCountry(), userDetails.getBio(), userDetails.getTopCategory(), userDetails.getCardColor(), userDetails.getBackgroundColor(), userDetails.getProfilePic());
        String json = gson.toJson(userProfileModel);
        System.out.println(json);
        return ResponseEntity.ok(json);
    }

    public ResponseEntity<?> getUsers(HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser == null || userRepository.findById(loggedInUser).isEmpty()){
            return ResponseEntity.badRequest().body("User not logged in");
        }
        List<Users> users = userRepository.findAll();
        List<UserProfileModel> userProfileList = new ArrayList<>();
        for (Users user : users){
            userProfileList.add(new UserProfileModel(user.getUsername(), user.getCountry(), user.getBio(), user.getTopCategory(), user.getCardColor(), user.getBackgroundColor(), user.getProfilePic()));
        }
        Gson gson = new Gson();
        String json = gson.toJson(userProfileList);
        return ResponseEntity.ok(json);
    }


}
