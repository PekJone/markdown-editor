package com.markdown.editor.service;

import com.markdown.editor.entity.Follow;
import java.util.List;

public interface FollowService {
    boolean isFollowing(Long followerId, Long followingId);

    void follow(Long followerId, Long followingId);

    void unfollow(Long followerId, Long followingId);

    List<Long> getFollowingIds(Long userId);

    List<Long> getFollowerIds(Long userId);

    int countFollowing(Long userId);

    int countFollowers(Long userId);

    Follow getFollowByUserAndFollower(Long userId, Long followerId);

    int countNewFollowersByDateRange(Long userId, java.time.LocalDate startDate, java.time.LocalDate endDate);
}
