package com.connectify.users.follow;

import com.connectify.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    //to retrieve users following list
    List<Follow> findAllByFollower(Users follower);

    //to retrieve users followers list
    List<Follow> findAllByFollowing(Users following);

    boolean existsByFollowerAndFollowing(Users follower, Users following);

    Follow findByFollowerAndFollowing(Users follower, Users following);

}
