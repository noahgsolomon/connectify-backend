package com.connectify.users.follow;

import com.connectify.users.UserRepository;
import com.connectify.users.Users;
import com.connectify.users.config.JwtService;
import com.connectify.users.notification.NotificationService;
import com.connectify.users.notification.NotificationType;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class FollowService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    @Autowired
    private final NotificationService notificationService;
    private final JwtService jwtService;

    public ResponseEntity<?> getFollowCount(String user){
        Users userObj = userRepository.findById(user).get();

        List<Follow> followerList = followRepository.findAllByFollowing(userObj);
        List<Follow> followingList = followRepository.findAllByFollower(userObj);


        Gson gson = new Gson();
        FollowCountResponseModel followCountResponseModel = new FollowCountResponseModel(followerList.size(), followingList.size());
        String json = gson.toJson(followCountResponseModel);
        return ResponseEntity.ok(json);
    }

    public ResponseEntity<?> isUserFollowed(String user, HttpServletRequest request){
        String username = jwtService.getUsername(request);
        Users userObj = userRepository.findById(username).get();
        Users followingObj = userRepository.findById(user).get();
        Gson gson = new Gson();
        IsFollowedResponseModel isFollowedResponseModel;
        if (followRepository.existsByFollowerAndFollowing(userObj, followingObj)){
            isFollowedResponseModel = new IsFollowedResponseModel(true);
        }
        else {
            isFollowedResponseModel = new IsFollowedResponseModel(false);
        }
        return ResponseEntity.ok(gson.toJson(isFollowedResponseModel));
    }

    public ResponseEntity<?> followEvent(String user, HttpServletRequest request){
        String username = jwtService.getUsername(request);
        Users userObj = userRepository.findById(username).get();
        Users followingObj = userRepository.findById(user).get();

        if (followRepository.existsByFollowerAndFollowing(userObj, followingObj)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already follows this account");
        }

        Follow follow = new Follow(userObj, followingObj);
        followRepository.save(follow);
        notificationService.setNotification(userObj, followingObj, NotificationType.FOLLOW, follow.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully followed");
    }

    public ResponseEntity<?> unfollowEvent(String user, HttpServletRequest request){
        if (userRepository.findById(user).isEmpty() || !userRepository.findById(user).get().getEnabled()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid user");
        }
        String username = jwtService.getUsername(request);
        Users userObj = userRepository.findById(username).get();
        Users followingObj = userRepository.findById(user).get();

        if (followRepository.existsByFollowerAndFollowing(userObj, followingObj)){
            Follow follow = followRepository.findByFollowerAndFollowing(userObj, followingObj);
            followRepository.deleteById(follow.getId());
            notificationService.removeNotification(userObj, followingObj, NotificationType.FOLLOW, follow.getId());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("User unfollowed");
        }
        else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not followed");
    }

    public ResponseEntity<?>friendsList(HttpServletRequest request){
        String username = jwtService.getUsername(request);
        Users user = userRepository.findById(username).get();
        List<Follow> usersFollowingList = followRepository.findAllByFollower(user);
        List<Follow> usersFollowerList = followRepository.findAllByFollowing(user);
        List<FriendResponseModel> mutualFollowList = new ArrayList<>();
        for (Follow following : usersFollowingList) {
            System.out.println(following.getFollower());
            System.out.println(following.getFollowing());
            for (Follow follower : usersFollowerList) {
                System.out.println(follower.getFollower());
                System.out.println(follower.getFollowing());
                if (following.getFollowing().equals(follower.getFollower()) && following.getFollower().equals(follower.getFollowing())) {
                    Users followerUser = following.getFollower();
                    Users followingUser = following.getFollowing();
                    if (!followerUser.getUsername().equals(username)){
                        System.out.println(followerUser.getUsername() + " "  + followerUser.isOnline());
                        mutualFollowList.add(new FriendResponseModel(followerUser.getUsername(), followerUser.getProfilePic(), String.valueOf(followerUser.isOnline())));
                    }
                    else if (!followingUser.getUsername().equals(username)){
                        System.out.println(followingUser.getUsername() + " "  + followingUser.isOnline());
                        mutualFollowList.add(new FriendResponseModel(followingUser.getUsername(), followingUser.getProfilePic(), String.valueOf(followingUser.isOnline())));
                    }
                    break;
                }
            }
        }
        Gson gson = new Gson();
        System.out.println(gson.toJson(mutualFollowList));
        return ResponseEntity.ok(gson.toJson(mutualFollowList));
    }
}
