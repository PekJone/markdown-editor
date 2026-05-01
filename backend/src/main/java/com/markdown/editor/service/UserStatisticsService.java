package com.markdown.editor.service;

import com.markdown.editor.entity.UserStatistics;

public interface UserStatisticsService {
    UserStatistics selectByUserId(Long userId);
    int insert(UserStatistics userStatistics);
    int update(UserStatistics userStatistics);
    int incrementLikesCount(Long userId);
    int decrementLikesCount(Long userId);
    int incrementCollectionsCount(Long userId);
    int decrementCollectionsCount(Long userId);
    int incrementCommentsCount(Long userId);
    int decrementCommentsCount(Long userId);
    int incrementFollowingCount(Long userId);
    int decrementFollowingCount(Long userId);
    int incrementFollowersCount(Long userId);
    int decrementFollowersCount(Long userId);
    int incrementBlogCount(Long userId);
    int decrementBlogCount(Long userId);
}
