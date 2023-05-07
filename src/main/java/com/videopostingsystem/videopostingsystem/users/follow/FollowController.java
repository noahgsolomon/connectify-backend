package com.videopostingsystem.videopostingsystem.users.follow;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FollowController {

    FollowService followService;

    public FollowController(FollowService followService){
        this.followService = followService;
    }

    @GetMapping("/{user}/follow-count")
    public ResponseEntity<?> getFollowCount(@PathVariable("user") String user, HttpSession session){
        return followService.getFollowCount(user, session);
    }

    @GetMapping("/{user}/followed")
    public ResponseEntity<?> isUserFollowed(@PathVariable("user") String user, HttpSession session){
        return followService.isUserFollowed(user, session);
    }

    @PostMapping("/{user}/follow")
    public ResponseEntity<?> followEvent(@PathVariable("user") String user, HttpSession session){
        return followService.followEvent(user, session);
    }

    @PostMapping("/{user}/unfollow")
    public ResponseEntity<?> unfollowEvent(@PathVariable("user") String user, HttpSession session){
        return followService.unfollowEvent(user, session);
    }

}
