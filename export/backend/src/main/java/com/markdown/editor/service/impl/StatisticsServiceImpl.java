package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markdown.editor.dto.UserStatsDTO;
import com.markdown.editor.entity.Document;
import com.markdown.editor.entity.User;
import com.markdown.editor.entity.UserStatistics;
import com.markdown.editor.service.*;
import com.markdown.editor.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger logger = LogUtils.getLogger(StatisticsServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserStatisticsService userStatisticsService;

    @Autowired
    private DocumentCollectionService documentCollectionService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private FollowService followService;

    @Override
    public UserStatsDTO calculateUserStats(Long userId) {
        UserStatsDTO stats = new UserStatsDTO();
        stats.setUserId(userId);

        // 获取用户信息
        User user = userService.selectById(userId);
        if (user != null) {
            stats.setUsername(user.getUsername());
            stats.setNickname(user.getNickname());
            stats.setAvatar(user.getAvatar());
        }

        // 从用户统计表中获取统计数据
        UserStatistics userStatistics = userStatisticsService.selectByUserId(userId);
        if (userStatistics != null) {
            stats.setOriginal(userStatistics.getBlogCount() != null ? userStatistics.getBlogCount() : 0);
            stats.setLikes(userStatistics.getLikesCount() != null ? userStatistics.getLikesCount() : 0);
            stats.setCollections(userStatistics.getCollectionsCount() != null ? userStatistics.getCollectionsCount() : 0);
            stats.setComments(userStatistics.getCommentsCount() != null ? userStatistics.getCommentsCount() : 0);
            stats.setFollowers(userStatistics.getFollowersCount() != null ? userStatistics.getFollowersCount() : 0);
            stats.setFollowing(userStatistics.getFollowingCount() != null ? userStatistics.getFollowingCount() : 0);
            
            // 计算总浏览量
            int totalViews = calculateTotalViews(userId);
            stats.setViews(totalViews);
        } else {
            // 如果用户统计表中没有数据，计算并插入
            calculateAndInsertUserStatistics(userId);
            // 再次获取统计数据
            userStatistics = userStatisticsService.selectByUserId(userId);
            if (userStatistics != null) {
                stats.setOriginal(userStatistics.getBlogCount() != null ? userStatistics.getBlogCount() : 0);
                stats.setLikes(userStatistics.getLikesCount() != null ? userStatistics.getLikesCount() : 0);
                stats.setCollections(userStatistics.getCollectionsCount() != null ? userStatistics.getCollectionsCount() : 0);
                stats.setComments(userStatistics.getCommentsCount() != null ? userStatistics.getCommentsCount() : 0);
                stats.setFollowers(userStatistics.getFollowersCount() != null ? userStatistics.getFollowersCount() : 0);
                stats.setFollowing(userStatistics.getFollowingCount() != null ? userStatistics.getFollowingCount() : 0);
                
                // 计算总浏览量
                int totalViews = calculateTotalViews(userId);
                stats.setViews(totalViews);
            }
        }

        // 计算博客数排名
        int blogRank = calculateBlogRank(userId);
        stats.setBlogRank(blogRank);

        // 计算当前连续创作天数
        int continuousDays = calculateContinuousDays(userId);
        
        // 设置连续天数（超过99显示99+）
        stats.setContinuousDays(continuousDays);
        stats.setContinuousDaysDisplay(continuousDays > 99 ? "99+" : String.valueOf(continuousDays));
        LogUtils.info(logger, "连续创作天数计算完成: {}, 显示值: {}", continuousDays, stats.getContinuousDaysDisplay());

        // 打印统计信息
        LogUtils.info(logger, "UserStatsDTO: {}", stats);
        LogUtils.info(logger, "userId: {}", stats.getUserId());
        LogUtils.info(logger, "username: {}", stats.getUsername());
        LogUtils.info(logger, "nickname: {}", stats.getNickname());
        LogUtils.info(logger, "original: {}", stats.getOriginal());
        LogUtils.info(logger, "likes: {}", stats.getLikes());
        LogUtils.info(logger, "collections: {}", stats.getCollections());
        LogUtils.info(logger, "comments: {}", stats.getComments());
        LogUtils.info(logger, "views: {}", stats.getViews());
        LogUtils.info(logger, "blogRank: {}", stats.getBlogRank());
        LogUtils.info(logger, "followers: {}", stats.getFollowers());
        LogUtils.info(logger, "following: {}", stats.getFollowing());
        LogUtils.info(logger, "continuousDays: {}", stats.getContinuousDays());
        LogUtils.info(logger, "continuousDaysDisplay: {}", stats.getContinuousDaysDisplay());

        return stats;
    }

    private int calculateTotalViews(Long userId) {
        int totalViews = 0;
        try {
            List<Document> userDocuments = documentService.selectUserDocumentsWithStats(userId);
            LogUtils.info(logger, "获取到{}篇文档", userDocuments.size());
            for (Document doc : userDocuments) {
                Integer viewCount = doc.getViewCount();
                LogUtils.info(logger, "文档ID: {}, 浏览量: {}", doc.getId(), viewCount);
                totalViews += viewCount != null ? viewCount : 0;
            }
            LogUtils.info(logger, "总浏览量: {}", totalViews);
        } catch (Exception e) {
            LogUtils.error(logger, "计算总浏览量失败", e);
        }
        return totalViews;
    }

    private int calculateBlogRank(Long userId) {
        int blogRank = 1;
        try {
            List<User> allUsers = userService.selectAll();
            Map<Long, Integer> userArticleCounts = new HashMap<>();
            
            for (User u : allUsers) {
                Long uId = u.getId();
                UserStatistics uStats = userStatisticsService.selectByUserId(uId);
                if (uStats != null) {
                    Integer blogCount = uStats.getBlogCount();
                    userArticleCounts.put(uId, blogCount != null ? blogCount : 0);
                } else {
                    Page<Document> userDocsPage = documentService.selectPage(new Page<>(1, 100), uId, null, null, null);
                    userArticleCounts.put(uId, userDocsPage.getRecords().size());
                }
            }
            
            List<Map.Entry<Long, Integer>> sortedUsers = new ArrayList<>(userArticleCounts.entrySet());
            sortedUsers.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            for (int i = 0; i < sortedUsers.size(); i++) {
                if (sortedUsers.get(i).getKey().equals(userId)) {
                    blogRank = i + 1;
                    break;
                }
            }
        } catch (Exception e) {
            LogUtils.error(logger, "计算博客排名失败", e);
        }
        return blogRank;
    }

    private int calculateContinuousDays(Long userId) {
        int continuousDays = 0;
        try {
            LogUtils.info(logger, "开始计算当前连续创作天数，userId: {}", userId);
            
            List<Document> userDocuments = documentService.selectByUserId(userId);
            LogUtils.info(logger, "获取到用户{}的文章数量: {}", userId, userDocuments.size());
            
            Set<LocalDate> articleDates = new HashSet<>();
            for (Document doc : userDocuments) {
                java.util.Date createdAt = doc.getCreatedAt();
                if (createdAt != null) {
                    LocalDate docDate = Instant.ofEpochMilli(createdAt.getTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    articleDates.add(docDate);
                    LogUtils.info(logger, "文章ID: {}, 创建日期: {}", doc.getId(), docDate);
                } else {
                    LogUtils.info(logger, "文章ID: {}, 创建日期为null", doc.getId());
                }
            }
            
            LocalDate today = LocalDate.now();
            LogUtils.info(logger, "今天日期: {}", today);
            LogUtils.info(logger, "文章日期集合: {}", articleDates);
            
            LocalDate checkDate = today;
            while (articleDates.contains(checkDate)) {
                continuousDays++;
                checkDate = checkDate.minusDays(1);
                if (continuousDays > 365) {
                    break;
                }
            }
            
            LogUtils.info(logger, "当前连续创作天数: {}", continuousDays);
        } catch (Exception e) {
            LogUtils.error(logger, "计算连续创作天数失败", e);
        }
        return continuousDays;
    }

    private void calculateAndInsertUserStatistics(Long userId) {
        UserStatistics userStatistics = new UserStatistics();
        
        // 设置用户ID
        userStatistics.setUserId(userId);

        // 一次性获取用户所有文档及其统计数据（评论数、收藏数）
        List<Document> userDocuments = documentService.selectUserDocumentsWithStats(userId);
        LogUtils.info(logger, "获取到{}篇文档", userDocuments.size());
        userStatistics.setBlogCount(userDocuments.size());

        // 计算总点赞数、被收藏数、评论总数、总浏览量
        int totalLikes = 0;
        int totalCollections = 0;
        int totalComments = 0;
        int totalViews = 0;
        
        for (Document doc : userDocuments) {
            Integer likeCount = doc.getLikeCount();
            totalLikes += likeCount != null ? likeCount : 0;
            
            Integer collectionCount = doc.getCollectionCount();
            totalCollections += collectionCount != null ? collectionCount : 0;
            
            Integer commentCount = doc.getCommentCount();
            totalComments += commentCount != null ? commentCount : 0;
            
            Integer viewCount = doc.getViewCount();
            LogUtils.info(logger, "文档ID: {}, 浏览量: {}", doc.getId(), viewCount);
            totalViews += viewCount != null ? viewCount : 0;
        }
        
        userStatistics.setLikesCount(totalLikes);
        userStatistics.setCollectionsCount(totalCollections);
        userStatistics.setCommentsCount(totalComments);
        LogUtils.info(logger, "总浏览量: {}", totalViews);

        // 计算关注用户数
        int totalFollowing = followService.countFollowing(userId);
        userStatistics.setFollowingCount(totalFollowing);

        // 计算粉丝数
        int totalFollowers = followService.countFollowers(userId);
        userStatistics.setFollowersCount(totalFollowers);

        // 设置创建时间和更新时间
        userStatistics.setCreatedAt(LocalDateTime.now());
        userStatistics.setUpdatedAt(LocalDateTime.now());

        // 插入用户统计数据
        userStatisticsService.insert(userStatistics);
    }
}
