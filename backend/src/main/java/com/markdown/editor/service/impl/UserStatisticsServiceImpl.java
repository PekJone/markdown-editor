package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.markdown.editor.entity.UserStatistics;
import com.markdown.editor.mapper.UserStatisticsMapper;
import com.markdown.editor.service.UserStatisticsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserStatisticsServiceImpl implements UserStatisticsService {

    @Resource
    private UserStatisticsMapper userStatisticsMapper;

    @Override
    public UserStatistics selectByUserId(Long userId) {
        return userStatisticsMapper.selectById(userId);
    }

    @Override
    public int insert(UserStatistics userStatistics) {
        return userStatisticsMapper.insert(userStatistics);
    }

    @Override
    public int update(UserStatistics userStatistics) {
        return userStatisticsMapper.updateById(userStatistics);
    }

    private void ensureUserStatisticsExists(Long userId) {
        UserStatistics userStatistics = userStatisticsMapper.selectById(userId);
        if (userStatistics == null) {
            userStatistics = new UserStatistics();
            userStatistics.setUserId(userId);
            userStatistics.setLikesCount(0);
            userStatistics.setCollectionsCount(0);
            userStatistics.setCommentsCount(0);
            userStatistics.setFollowingCount(0);
            userStatistics.setFollowersCount(0);
            userStatistics.setBlogCount(0);
            userStatistics.setCreatedAt(java.time.LocalDateTime.now());
            userStatistics.setUpdatedAt(java.time.LocalDateTime.now());
            userStatisticsMapper.insert(userStatistics);
        }
    }

    @Override
    public int incrementLikesCount(Long userId) {
        ensureUserStatisticsExists(userId);
        UpdateWrapper<UserStatistics> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.setSql("likes_count = likes_count + 1, updated_at = now()");
        return userStatisticsMapper.update(null, wrapper);
    }

    @Override
    public int decrementLikesCount(Long userId) {
        ensureUserStatisticsExists(userId);
        UpdateWrapper<UserStatistics> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.setSql("likes_count = likes_count - 1, updated_at = now()");
        return userStatisticsMapper.update(null, wrapper);
    }

    @Override
    public int incrementCollectionsCount(Long userId) {
        ensureUserStatisticsExists(userId);
        UpdateWrapper<UserStatistics> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.setSql("collections_count = collections_count + 1, updated_at = now()");
        return userStatisticsMapper.update(null, wrapper);
    }

    @Override
    public int decrementCollectionsCount(Long userId) {
        ensureUserStatisticsExists(userId);
        UpdateWrapper<UserStatistics> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.setSql("collections_count = collections_count - 1, updated_at = now()");
        return userStatisticsMapper.update(null, wrapper);
    }

    @Override
    public int incrementCommentsCount(Long userId) {
        ensureUserStatisticsExists(userId);
        UpdateWrapper<UserStatistics> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.setSql("comments_count = comments_count + 1, updated_at = now()");
        return userStatisticsMapper.update(null, wrapper);
    }

    @Override
    public int decrementCommentsCount(Long userId) {
        ensureUserStatisticsExists(userId);
        UpdateWrapper<UserStatistics> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.setSql("comments_count = comments_count - 1, updated_at = now()");
        return userStatisticsMapper.update(null, wrapper);
    }

    @Override
    public int incrementFollowingCount(Long userId) {
        ensureUserStatisticsExists(userId);
        UpdateWrapper<UserStatistics> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.setSql("following_count = following_count + 1, updated_at = now()");
        return userStatisticsMapper.update(null, wrapper);
    }

    @Override
    public int decrementFollowingCount(Long userId) {
        ensureUserStatisticsExists(userId);
        UpdateWrapper<UserStatistics> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.setSql("following_count = following_count - 1, updated_at = now()");
        return userStatisticsMapper.update(null, wrapper);
    }

    @Override
    public int incrementFollowersCount(Long userId) {
        ensureUserStatisticsExists(userId);
        UpdateWrapper<UserStatistics> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.setSql("followers_count = followers_count + 1, updated_at = now()");
        return userStatisticsMapper.update(null, wrapper);
    }

    @Override
    public int decrementFollowersCount(Long userId) {
        ensureUserStatisticsExists(userId);
        UpdateWrapper<UserStatistics> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.setSql("followers_count = followers_count - 1, updated_at = now()");
        return userStatisticsMapper.update(null, wrapper);
    }

    @Override
    public int incrementBlogCount(Long userId) {
        ensureUserStatisticsExists(userId);
        UpdateWrapper<UserStatistics> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.setSql("blog_count = blog_count + 1, updated_at = now()");
        return userStatisticsMapper.update(null, wrapper);
    }

    @Override
    public int decrementBlogCount(Long userId) {
        ensureUserStatisticsExists(userId);
        UpdateWrapper<UserStatistics> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.setSql("blog_count = blog_count - 1, updated_at = now()");
        return userStatisticsMapper.update(null, wrapper);
    }
}
