package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.markdown.editor.entity.Follow;
import com.markdown.editor.mapper.FollowMapper;
import com.markdown.editor.service.FollowService;
import com.markdown.editor.service.UserStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Date;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Autowired
    private UserStatisticsService userStatisticsService;

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        try {
            QueryWrapper<Follow> wrapper = new QueryWrapper<>();
            wrapper.eq("follower_id", followerId);
            wrapper.eq("following_id", followingId);
            return baseMapper.selectCount(wrapper) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("不能关注自己");
        }
        if (isFollowing(followerId, followingId)) {
            throw new RuntimeException("已经关注了该用户");
        }
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowingId(followingId);
        follow.setCreatedAt(new Date());
        
        int result = baseMapper.insert(follow);
        if (result > 0) {
            userStatisticsService.incrementFollowingCount(followerId);
            userStatisticsService.incrementFollowersCount(followingId);
        }
    }

    @Override
    public void unfollow(Long followerId, Long followingId) {
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", followerId);
        wrapper.eq("following_id", followingId);
        int result = baseMapper.delete(wrapper);
        if (result > 0) {
            userStatisticsService.decrementFollowingCount(followerId);
            userStatisticsService.decrementFollowersCount(followingId);
        }
    }

    @Override
    public List<Long> getFollowingIds(Long userId) {
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", userId);
        List<Follow> follows = baseMapper.selectList(wrapper);
        Set<Long> followingIdSet = new LinkedHashSet<>();
        for (Follow follow : follows) {
            followingIdSet.add(follow.getFollowingId());
        }
        return new ArrayList<>(followingIdSet);
    }

    @Override
    public List<Long> getFollowerIds(Long userId) {
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("following_id", userId);
        List<Follow> follows = baseMapper.selectList(wrapper);
        Set<Long> followerIdSet = new LinkedHashSet<>();
        for (Follow follow : follows) {
            followerIdSet.add(follow.getFollowerId());
        }
        return new ArrayList<>(followerIdSet);
    }

    @Override
    public int countFollowing(Long userId) {
        return getFollowingIds(userId).size();
    }

    @Override
    public int countFollowers(Long userId) {
        return getFollowerIds(userId).size();
    }

    @Override
    public Follow getFollowByUserAndFollower(Long userId, Long followerId) {
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("following_id", userId);
        wrapper.eq("follower_id", followerId);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public int countNewFollowersByDateRange(Long userId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        QueryWrapper<Follow> wrapper = new QueryWrapper<>();
        wrapper.eq("following_id", userId);
        wrapper.between("created_at", startDate.atStartOfDay(), endDate.atTime(java.time.LocalTime.MAX));
        return baseMapper.selectCount(wrapper).intValue();
    }
}
