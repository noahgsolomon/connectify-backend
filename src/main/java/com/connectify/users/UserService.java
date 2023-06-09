package com.connectify.users;

import com.connectify.inbox.Inbox;
import com.connectify.inbox.InboxRepository;
import com.connectify.inbox.messagelog.MessageLogRepository;
import com.connectify.posts.Post;
import com.connectify.posts.PostRepository;
import com.connectify.posts.comments.Comment;
import com.connectify.posts.comments.CommentRepository;
import com.connectify.posts.interaction.PostInteractionRepository;
import com.connectify.posts.interaction.PostInteractions;
import com.connectify.users.config.JwtService;
import com.connectify.users.follow.Follow;
import com.connectify.users.follow.FollowRepository;
import com.connectify.users.notification.Notification;
import com.connectify.users.notification.NotificationRepository;
import com.connectify.users.token.ConfirmationToken;
import com.connectify.users.token.ConfirmationTokenRepository;
import com.connectify.users.token.ValidateToken;
import com.connectify.users.token.ValidateTokenRepository;
import com.google.gson.Gson;
import com.connectify.inbox.messagelog.MessageLog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

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
    private final FollowRepository followRepository;
    private final ValidateTokenRepository validateTokenRepository;
    private final NotificationRepository notificationRepository;
    private final JwtService jwtService;

    @Transactional
    public ResponseEntity<?> deleteAccount(HttpServletRequest request){
        String username = jwtService.getUsername(request);
        Users user = userRepository.findById(username).get();

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

        List<ValidateToken> validateTokens = validateTokenRepository.findAllByUsers(user);
        for (ValidateToken token : validateTokens){
            validateTokenRepository.deleteByToken(token.getToken());
        }

        List<Follow> follows = new ArrayList<>();
        follows.addAll(followRepository.findAllByFollower(user));
        follows.addAll(followRepository.findAllByFollowing(user));

        for (Follow follow : follows){
            followRepository.deleteById(follow.getId());
        }

        List<Notification> notifications = new ArrayList<>();
        notifications.addAll(notificationRepository.findAllByUsers(user));
        notifications.addAll(notificationRepository.findAllBySender(user));
        for (Notification notification : notifications){
            notificationRepository.deleteById(notification.getId());
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

        userRepository.deleteById(username);
        return ResponseEntity.ok("Successfully deleted account. We're sad to see you go, " + username + "!");

    }

    @Transactional
    public ResponseEntity<?> deleteAccountAdmin(String deletedUser, HttpServletRequest request) {
        String username = jwtService.getUsername(request);
        if (userRepository.findById(deletedUser).isEmpty()) {
            return ResponseEntity.badRequest().body("user does not exist");
        }
        Users user = userRepository.findById(deletedUser).get();
        if (!userRepository.findById(username).get().getType().equals(UserType.ADMIN)) {
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

    public ResponseEntity<?> getUsername(HttpServletRequest request){
        return ResponseEntity.ok(jwtService.getUsername(request));

    }

    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        String username = jwtService.getUsername(request);
        Gson gson = new Gson();
        Users user = userRepository.findById(username).get();
        List<Follow> followerList = followRepository.findAllByFollowing(user);
        List<Follow> followingList = followRepository.findAllByFollower(user);
        AllUserCredentialsResponseModel allUserCredentialsResponseModel = new AllUserCredentialsResponseModel(user, followerList.size(), followingList.size());
        String json = gson.toJson(allUserCredentialsResponseModel);
        return ResponseEntity.ok(json);
    }

    public ResponseEntity<?> updateProfile(UpdateProfileModel updateProfileModel, HttpServletRequest request){
        String username = jwtService.getUsername(request);
        Gson gson = new Gson();
        Users users = userRepository.findById(username).get();
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

    public ResponseEntity<?> updateProfileSettings(UpdateProfileSettingsModel updateProfileSettingsModel, HttpServletRequest request) {
        String username = jwtService.getUsername(request);
        Gson gson = new Gson();
        Users users = userRepository.findById(username).get();

        users.setFirstName(updateProfileSettingsModel.firstName());
        users.setLastName(updateProfileSettingsModel.lastName());
        users.setProfilePic(updateProfileSettingsModel.profilePic());

        userRepository.save(users);
        String json = gson.toJson(users);
        return ResponseEntity.ok(json);
    }

    public ResponseEntity<?> getUserProfile(String user, HttpServletRequest request) {

        if (user == null || userRepository.findById(user).isEmpty()){
            return ResponseEntity.badRequest().body("User does not exist");
        }

        Gson gson = new Gson();

        String username = jwtService.getUsername(request);
        Users selfObj = userRepository.findById(username).get();

        Users userObj = userRepository.findById(user).get();

        boolean follows = followRepository.existsByFollowerAndFollowing(selfObj, userObj);

        List<Follow> followerList = followRepository.findAllByFollowing(userObj);
        List<Follow> followingList = followRepository.findAllByFollower(userObj);

        UserProfileModel userProfileModel = new UserProfileModel(userObj.getUsername(), userObj.getCountry(), userObj.getBio(), userObj.getTopCategory(), userObj.getCardColor(), userObj.getBackgroundColor(), userObj.getProfilePic(), String.valueOf(userObj.isOnline()), followerList.size(), followingList.size(), follows);
        String json = gson.toJson(userProfileModel);
        System.out.println(json);
        return ResponseEntity.ok(json);
    }

    public ResponseEntity<?> getUsers() {
        List<Users> users = userRepository.findAll();
        List<UserProfileModel> userProfileList = new ArrayList<>();
        for (Users user : users){

            List<Follow> followerList = followRepository.findAllByFollowing(user);
            List<Follow> followingList = followRepository.findAllByFollower(user);

            userProfileList.add(new UserProfileModel(user.getUsername(), user.getCountry(), user.getBio(), user.getTopCategory(), user.getCardColor(), user.getBackgroundColor(), user.getProfilePic(), String.valueOf(user.isOnline()), followerList.size(), followingList.size(), false));
        }
        Gson gson = new Gson();
        String json = gson.toJson(userProfileList);
        return ResponseEntity.ok(json);
    }

    public ResponseEntity<?> updateTheme(String theme, HttpServletRequest request){
        String username = jwtService.getUsername(request);
        if (userRepository.findById(username).isEmpty()) {
            return ResponseEntity.badRequest().body("user does not exist");
        }
        Users user = userRepository.findById(username).get();
        user.setTheme(theme);

        userRepository.save(user);
        return ResponseEntity.ok("updated theme");
    }

    public ResponseEntity<?> getTheme(HttpServletRequest request){
        String username = jwtService.getUsername(request);

        Optional<Users> user = userRepository.findById(username);
        return user.map(users -> ResponseEntity.ok(users.getTheme())).orElseGet(() -> ResponseEntity.badRequest().body("user does not exist"));

    }


    public ResponseEntity<?> onlineHeartBeat(HttpServletRequest request) {
        String username = jwtService.getUsername(request);
        Optional<Users> optionalUser = userRepository.findById(username);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User does not exist");
        }

        Users user = optionalUser.get();

        if (!user.isOnline()){
            user.setOnline(true);
        }

        user.setLastHeartbeat(new Date());
        userRepository.save(user);

        return ResponseEntity.ok("heartbeat");
    }

    public void cleanUpOnlineList() {

        List<Users> onlineUsers = userRepository.findAllByOnline(true);

        for (Users onlineUser : onlineUsers){

            Date lastHeartbeat = onlineUser.getLastHeartbeat();
            Date currentTime = new Date();
            long durationInMillis = currentTime.getTime() - lastHeartbeat.getTime();
            long minutes = durationInMillis / (60 * 1000);

            if (minutes > 3){
                onlineUser.setOnline(false);
                userRepository.save(onlineUser);
            }
        }
    }
}
