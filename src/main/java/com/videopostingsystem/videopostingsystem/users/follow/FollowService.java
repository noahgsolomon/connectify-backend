package com.videopostingsystem.videopostingsystem.users.follow;

import com.google.gson.Gson;
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

import java.util.List;

@Service
@AllArgsConstructor
public class FollowService {
    UserRepository userRepository;
    FollowRepository followRepository;
    @Autowired
    NotificationService notificationService;

    public ResponseEntity<?> getFollowCount(String user, HttpSession session){
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser == null || userRepository.findById(loggedInUser).isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user not logged in");
        }
        if (userRepository.findById(user).isEmpty() || !userRepository.findById(user).get().getEnabled()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid user");
        }

        Users userObj = userRepository.findById(user).get();

        List<Follow> followerList = followRepository.findAllByFollowing(userObj);
        List<Follow> followingList = followRepository.findAllByFollower(userObj);


        Gson gson = new Gson();
        FollowCountResponseModel followCountResponseModel = new FollowCountResponseModel(followerList.size(), followingList.size());
        String json = gson.toJson(followCountResponseModel);
        return ResponseEntity.ok(json);
    }

    public ResponseEntity<?> isUserFollowed(String user, HttpSession session){
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser == null || userRepository.findById(loggedInUser).isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user not logged in");
        }
        if (userRepository.findById(user).isEmpty() || !userRepository.findById(user).get().getEnabled()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid user");
        }

        Users userObj = userRepository.findById(loggedInUser).get();
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

    public ResponseEntity<?> followEvent(String user, HttpSession session){
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser == null || userRepository.findById(loggedInUser).isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user not logged in");
        }
        if (userRepository.findById(user).isEmpty() || !userRepository.findById(user).get().getEnabled()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid user");
        }

        Users userObj = userRepository.findById(loggedInUser).get();
        Users followingObj = userRepository.findById(user).get();

        if (followRepository.existsByFollowerAndFollowing(userObj, followingObj)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already follows this account");
        }

        Follow follow = new Follow(userObj, followingObj);
        followRepository.save(follow);
        notificationService.setNotification(userObj, followingObj, NotificationType.FOLLOW, follow.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully followed");
    }

    public ResponseEntity<?> unfollowEvent(String user, HttpSession session){
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        if (loggedInUser == null || userRepository.findById(loggedInUser).isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user not logged in");
        }
        if (userRepository.findById(user).isEmpty() || !userRepository.findById(user).get().getEnabled()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid user");
        }

        Users userObj = userRepository.findById(loggedInUser).get();
        Users followingObj = userRepository.findById(user).get();

        if (followRepository.existsByFollowerAndFollowing(userObj, followingObj)){
            Follow follow = followRepository.findByFollowerAndFollowing(userObj, followingObj);
            followRepository.deleteById(follow.getId());
            notificationService.removeNotification(userObj, followingObj, NotificationType.FOLLOW, follow.getId());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("User unfollowed");
        }
        else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not followed");
    }
}
