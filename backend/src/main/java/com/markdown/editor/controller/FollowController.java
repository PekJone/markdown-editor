package com.markdown.editor.controller;

import com.markdown.editor.dto.ApiResponse;
import com.markdown.editor.dto.FollowStatsVO;
import com.markdown.editor.dto.UserInfoVO;
import com.markdown.editor.dto.WeeklySummaryVO;
import com.markdown.editor.entity.Follow;
import com.markdown.editor.entity.User;
import com.markdown.editor.entity.Message;
import com.markdown.editor.mapper.UserMapper;
import com.markdown.editor.security.UserDetailsImpl;
import com.markdown.editor.service.FollowService;
import com.markdown.editor.service.MessageService;
import com.markdown.editor.service.BlacklistService;
import com.markdown.editor.utils.LogUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping({"/api/users", "/api"})
@Api(tags = "关注管理", description = "关注/取消关注、粉丝/关注列表、一周小结等接口")
public class FollowController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FollowController.class);

    @Autowired
    private FollowService followService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageService messageService;

    @PostMapping("/{userId}/follow")
    public ResponseEntity<?> followUser(@PathVariable Long userId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]关注用户[{}]", userDetails.getUsername(), userId);

        try {
            if (userDetails.getId().equals(userId)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("不能关注自己"));
            }

            User targetUser = userMapper.selectById(userId);
            if (targetUser == null) {
                return ResponseEntity.notFound().build();
            }

            if (followService.isFollowing(userDetails.getId(), userId)) {
                return ResponseEntity.ok(ApiResponse.success("已经关注了该用户", null));
            }

            followService.follow(userDetails.getId(), userId);
            
            // 发送关注消息
            User currentUser = userMapper.selectById(userDetails.getId());
            Message message = new Message();
            message.setSenderId(userDetails.getId());
            message.setReceiverId(userId);
            message.setType("follow");
            message.setContent("用户" + (currentUser.getNickname() != null ? currentUser.getNickname() : "") + "关注了您");
            message.setRelatedId(userDetails.getId());
            message.setIsRead(false);
            message.setCreatedAt(new Date());
            message.setUpdatedAt(new Date());
            
            messageService.sendMessage(message);
            
            LogUtils.info(logger, "用户[{}]关注用户[{}]成功", userDetails.getUsername(), userId);
            return ResponseEntity.ok(ApiResponse.success("关注成功", null));
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]关注用户[{}]失败", e, userDetails.getUsername(), userId);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<?> unfollowUser(@PathVariable Long userId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]取消关注用户[{}]", userDetails.getUsername(), userId);

        try {
            followService.unfollow(userDetails.getId(), userId);
            LogUtils.info(logger, "用户[{}]取消关注用户[{}]成功", userDetails.getUsername(), userId);
            return ResponseEntity.ok(ApiResponse.success("取消关注成功", null));
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]取消关注用户[{}]失败", e, userDetails.getUsername(), userId);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{userId}/follow/status")
    public ResponseEntity<?> getFollowStatus(@PathVariable Long userId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        boolean isFollowing = followService.isFollowing(userDetails.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("获取关注状态成功", isFollowing));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<?> getFollowers(@PathVariable Long userId) {
        LogUtils.info(logger, "获取用户[{}]的粉丝列表", userId);

        List<Long> followerIds = followService.getFollowerIds(userId);
        List<UserInfoVO> followers = new ArrayList<>();

        for (Long followerId : followerIds) {
            User user = userMapper.selectById(followerId);
            if (user != null) {
                UserInfoVO userInfo = new UserInfoVO();
                userInfo.setId(user.getId());
                userInfo.setUsername(user.getUsername());
                userInfo.setNickname(user.getNickname());
                userInfo.setAvatar(user.getAvatar());
                followers.add(userInfo);
            }
        }

        LogUtils.info(logger, "获取用户[{}]的粉丝列表成功，共{}个粉丝", userId, followers.size());
        return ResponseEntity.ok(ApiResponse.success("获取粉丝列表成功", followers));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<?> getFollowing(@PathVariable Long userId) {
        LogUtils.info(logger, "获取用户[{}]的关注列表", userId);

        List<Long> followingIds = followService.getFollowingIds(userId);
        List<UserInfoVO> following = new ArrayList<>();

        for (Long followingId : followingIds) {
            User user = userMapper.selectById(followingId);
            if (user != null) {
                UserInfoVO userInfo = new UserInfoVO();
                userInfo.setId(user.getId());
                userInfo.setUsername(user.getUsername());
                userInfo.setNickname(user.getNickname());
                userInfo.setAvatar(user.getAvatar());
                following.add(userInfo);
            }
        }

        LogUtils.info(logger, "获取用户[{}]的关注列表成功，共关注{}个用户", userId, following.size());
        return ResponseEntity.ok(ApiResponse.success("获取关注列表成功", following));
    }

    @GetMapping("/{userId}/follow/count")
    public ResponseEntity<?> getFollowCounts(@PathVariable Long userId) {
        FollowStatsVO counts = new FollowStatsVO();
        counts.setTotalFollowers(followService.countFollowers(userId));
        counts.setDailyGrowth(followService.countFollowing(userId));
        return ResponseEntity.ok(ApiResponse.success("获取关注统计成功", counts));
    }

    @GetMapping("/follow/stats")
    public ResponseEntity<FollowStatsVO> getFollowerStats(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        LogUtils.info(logger, "获取用户[{}]的粉丝统计数据", userId);

        FollowStatsVO stats = new FollowStatsVO();
        
        stats.setTotalFollowers(followService.countFollowers(userId));

        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate monthStart = now.withDayOfMonth(1);
        stats.setMonthlyGrowth(followService.countNewFollowersByDateRange(userId, monthStart, now));
        stats.setDailyGrowth(followService.countNewFollowersByDateRange(userId, now, now));

        LogUtils.info(logger, "获取用户[{}]的粉丝统计数据成功 - 总粉丝数: {}, 本月增长: {}, 今日增长: {}", 
                userId, stats.getTotalFollowers(), stats.getMonthlyGrowth(), stats.getDailyGrowth());
        return ResponseEntity.ok(stats);
    }

    @Autowired
    private com.markdown.editor.mapper.DocumentMapper documentMapper;

    @Autowired
    private com.markdown.editor.mapper.DocumentViewStatMapper documentViewStatMapper;

    @Autowired
    private com.markdown.editor.mapper.TrendMapper trendMapper;

    @Autowired
    private com.markdown.editor.mapper.DocumentCollectionMapper documentCollectionMapper;

    @GetMapping("/summary")
    public ResponseEntity<WeeklySummaryVO> getWeeklySummary(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        LogUtils.info(logger, "获取用户[{}]的一周小结数据", userId);

        java.time.LocalDate now = java.time.LocalDate.now();
        java.time.LocalDate weekStart = now.minusDays(6);
        
        WeeklySummaryVO summary = new WeeklySummaryVO();

        try {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.markdown.editor.entity.Document> documentWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            documentWrapper.eq("user_id", userId);
            documentWrapper.between("created_at", weekStart.atStartOfDay(), now.atTime(java.time.LocalTime.MAX));
            summary.setWeekArticles(documentMapper.selectCount(documentWrapper).intValue());
        } catch (Exception e) {
            LogUtils.error(logger, "获取本周发文数失败", e);
            summary.setWeekArticles(0);
        }

        try {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.markdown.editor.entity.DocumentViewStat> viewWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            viewWrapper.between("view_date", weekStart, now);
            java.util.List<com.markdown.editor.entity.DocumentViewStat> viewStats = documentViewStatMapper.selectList(viewWrapper);
            int weekViews = 0;
            for (com.markdown.editor.entity.DocumentViewStat stat : viewStats) {
                weekViews += stat.getViewCount();
            }
            summary.setWeekViews(weekViews);
        } catch (Exception e) {
            LogUtils.error(logger, "获取本周浏览数失败", e);
            summary.setWeekViews(0);
        }

        try {
            java.util.List<java.util.Map<String, Object>> likeStats = trendMapper.countByUserIdAndDateRange(
                userId, 
                java.sql.Timestamp.valueOf(weekStart.atStartOfDay()), 
                java.sql.Timestamp.valueOf(now.atTime(java.time.LocalTime.MAX))
            );
            int weekLikes = 0;
            for (java.util.Map<String, Object> stat : likeStats) {
                Object count = stat.get("count");
                if (count != null) {
                    weekLikes += Integer.parseInt(count.toString());
                }
            }
            summary.setWeekLikes(weekLikes);
        } catch (Exception e) {
            LogUtils.error(logger, "获取本周点赞数失败", e);
            summary.setWeekLikes(0);
        }

        try {
            summary.setWeekFollowers(followService.countNewFollowersByDateRange(userId, weekStart, now));
        } catch (Exception e) {
            LogUtils.error(logger, "获取本周新增粉丝数失败", e);
            summary.setWeekFollowers(0);
        }

        LogUtils.info(logger, "获取用户[{}]的一周小结数据成功 - 本周发文: {}, 本周浏览: {}, 本周点赞: {}, 本周新增粉丝: {}", 
                userId, summary.getWeekArticles(), summary.getWeekViews(), summary.getWeekLikes(), summary.getWeekFollowers());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/followers")
    public ResponseEntity<List<UserInfoVO>> getCurrentUserFollowers(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        LogUtils.info(logger, "获取当前用户[{}]的粉丝列表", userId);

        List<Long> followerIds = followService.getFollowerIds(userId);
        List<UserInfoVO> followers = new ArrayList<>();

        for (Long followerId : followerIds) {
            User user = userMapper.selectById(followerId);
            if (user != null) {
                UserInfoVO userInfo = new UserInfoVO();
                userInfo.setId(user.getId());
                userInfo.setUsername(user.getUsername());
                userInfo.setNickname(user.getNickname());
                userInfo.setAvatar(user.getAvatar());
                
                Follow follow = followService.getFollowByUserAndFollower(userId, followerId);
                if (follow != null) {
                    userInfo.setCreatedAt(follow.getCreatedAt());
                }
                followers.add(userInfo);
            }
        }

        LogUtils.info(logger, "获取当前用户[{}]的粉丝列表成功，共{}个粉丝", userId, followers.size());
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/following")
    public ResponseEntity<List<UserInfoVO>> getCurrentUserFollowing(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        LogUtils.info(logger, "获取当前用户[{}]的关注列表", userId);

        List<Long> followingIds = followService.getFollowingIds(userId);
        List<UserInfoVO> following = new ArrayList<>();

        for (Long followingId : followingIds) {
            User user = userMapper.selectById(followingId);
            if (user != null) {
                UserInfoVO userInfo = new UserInfoVO();
                userInfo.setId(user.getId());
                userInfo.setUsername(user.getUsername());
                userInfo.setNickname(user.getNickname());
                userInfo.setAvatar(user.getAvatar());
                
                Follow follow = followService.getFollowByUserAndFollower(followingId, userId);
                if (follow != null) {
                    userInfo.setCreatedAt(follow.getCreatedAt());
                }
                following.add(userInfo);
            }
        }

        LogUtils.info(logger, "获取当前用户[{}]的关注列表成功，共关注{}个用户", userId, following.size());
        return ResponseEntity.ok(following);
    }

    @DeleteMapping("/followers/{followerId}")
    public ResponseEntity<?> removeFollower(@PathVariable Long followerId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        LogUtils.info(logger, "用户[{}]移除粉丝[{}]", userId, followerId);

        try {
            followService.unfollow(followerId, userId);
            LogUtils.info(logger, "用户[{}]移除粉丝[{}]成功", userId, followerId);
            return ResponseEntity.ok(ApiResponse.success("移除粉丝成功", null));
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]移除粉丝[{}]失败", e, userId, followerId);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/friends")
    public ResponseEntity<List<UserInfoVO>> getCurrentUserFriends(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        LogUtils.info(logger, "获取当前用户[{}]的好友列表", userId);

        List<Long> followerIds = followService.getFollowerIds(userId);
        List<Long> followingIds = followService.getFollowingIds(userId);
        List<UserInfoVO> friends = new ArrayList<>();

        LogUtils.info(logger, "当前用户[{}]的粉丝数: {}, 关注数: {}", userId, followerIds.size(), followingIds.size());

        for (Long followerId : followerIds) {
            LogUtils.info(logger, "检查粉丝[{}]是否在关注列表中", followerId);
            if (followingIds.contains(followerId)) {
                LogUtils.info(logger, "粉丝[{}]在关注列表中，是好友", followerId);
                User user = userMapper.selectById(followerId);
                if (user != null) {
                    UserInfoVO userInfo = new UserInfoVO();
                    userInfo.setId(user.getId());
                    userInfo.setUsername(user.getUsername());
                    userInfo.setNickname(user.getNickname());
                    userInfo.setAvatar(user.getAvatar());
                    
                    Follow follow1 = followService.getFollowByUserAndFollower(userId, followerId);
                    Follow follow2 = followService.getFollowByUserAndFollower(followerId, userId);
                    Date createdAt = null;
                    if (follow1 != null && follow2 != null) {
                        createdAt = follow1.getCreatedAt().before(follow2.getCreatedAt()) ? follow1.getCreatedAt() : follow2.getCreatedAt();
                    } else if (follow1 != null) {
                        createdAt = follow1.getCreatedAt();
                    } else if (follow2 != null) {
                        createdAt = follow2.getCreatedAt();
                    }
                    userInfo.setCreatedAt(createdAt);
                    
                    friends.add(userInfo);
                    LogUtils.info(logger, "添加好友[{}]到列表", userInfo.getUsername());
                }
            }
        }

        LogUtils.info(logger, "获取当前用户[{}]的好友列表成功，共{}个好友", userId, friends.size());
        return ResponseEntity.ok(friends);
    }

    @Autowired
    private BlacklistService blacklistService;

    @PostMapping("/users/{userId}/block")
    public ResponseEntity<?> blockUser(@PathVariable Long userId, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        LogUtils.info(logger, "用户[{}]拉黑用户[{}]", currentUserId, userId);

        try {
            if (currentUserId.equals(userId)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("不能拉黑自己"));
            }

            User targetUser = userMapper.selectById(userId);
            if (targetUser == null) {
                return ResponseEntity.notFound().build();
            }

            blacklistService.addToBlacklist(currentUserId, userId);
            LogUtils.info(logger, "用户[{}]拉黑用户[{}]成功", currentUserId, userId);
            return ResponseEntity.ok(ApiResponse.success("拉黑成功", null));
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]拉黑用户[{}]失败", e, currentUserId, userId);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
