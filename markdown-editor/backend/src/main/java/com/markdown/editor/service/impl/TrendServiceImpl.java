package com.markdown.editor.service.impl;

import com.markdown.editor.dto.TrendDataDTO;
import com.markdown.editor.entity.Document;
import com.markdown.editor.entity.Follow;
import com.markdown.editor.mapper.*;
import com.markdown.editor.service.TrendService;
import com.markdown.editor.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class TrendServiceImpl implements TrendService {

    private static final Logger logger = LogUtils.getLogger(TrendServiceImpl.class);

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private DocumentLikeMapper documentLikeMapper;

    @Autowired
    private DocumentCollectionMapper documentCollectionMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private DocumentViewStatMapper documentViewStatMapper;

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread t = new Thread(r, "trend-data-worker");
                t.setDaemon(true);
                return t;
            }
    );

    @Override
    public List<TrendDataDTO> getTrendData(Long userId, LocalDate startDate, LocalDate endDate) {
        LogUtils.info(logger, "开始获取趋势数据，用户ID: {}, 开始日期: {}, 结束日期: {}", userId, startDate, endDate);
        
        // 生成日期列表
        List<LocalDate> dateList = generateDateRange(startDate, endDate);
        LogUtils.info(logger, "生成日期范围: {} 天", dateList.size());
        
        // 创建初始数据结构
        Map<LocalDate, TrendDataDTO> trendDataMap = initializeTrendData(dateList);
        
        // 使用CompletableFuture并行获取各类数据
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        // 并行获取浏览量数据
        CompletableFuture<Void> viewFuture = CompletableFuture.runAsync(() -> {
            fetchViewCountData(userId, startDate, endDate, trendDataMap);
        }, EXECUTOR);
        futures.add(viewFuture);
        
        // 并行获取评论数数据
        CompletableFuture<Void> commentFuture = CompletableFuture.runAsync(() -> {
            fetchCommentCountData(userId, startDate, endDate, trendDataMap);
        }, EXECUTOR);
        futures.add(commentFuture);
        
        // 并行获取收藏数数据
        CompletableFuture<Void> collectionFuture = CompletableFuture.runAsync(() -> {
            fetchCollectionCountData(userId, startDate, endDate, trendDataMap);
        }, EXECUTOR);
        futures.add(collectionFuture);
        
        // 并行获取粉丝增长数据
        CompletableFuture<Void> followerFuture = CompletableFuture.runAsync(() -> {
            fetchFollowerGrowthData(userId, startDate, endDate, trendDataMap);
        }, EXECUTOR);
        futures.add(followerFuture);
        
        // 等待所有任务完成，设置超时时间
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LogUtils.error(logger, "趋势数据获取被中断", e);
        } catch (ExecutionException e) {
            LogUtils.error(logger, "趋势数据获取执行异常", e.getCause());
        } catch (TimeoutException e) {
            LogUtils.warn(logger, "趋势数据获取超时，部分数据可能未获取完成");
        }
        
        // 将结果按日期排序返回
        List<TrendDataDTO> result = dateList.stream()
                .map(trendDataMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        LogUtils.info(logger, "趋势数据获取完成，共 {} 条记录", result.size());
        return result;
    }

    private List<LocalDate> generateDateRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        return dates;
    }

    private Map<LocalDate, TrendDataDTO> initializeTrendData(List<LocalDate> dateList) {
        Map<LocalDate, TrendDataDTO> map = new ConcurrentHashMap<>();
        for (LocalDate date : dateList) {
            map.put(date, TrendDataDTO.builder()
                    .date(date)
                    .views(0)
                    .comments(0)
                    .followers(0)
                    .collections(0)
                    .build());
        }
        return map;
    }

    private void fetchViewCountData(Long userId, LocalDate startDate, LocalDate endDate, Map<LocalDate, TrendDataDTO> trendDataMap) {
        try {
            LogUtils.info(logger, "开始获取浏览量数据...");
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            List<Map<String, Object>> views = documentViewStatMapper.countByUserIdAndDateRange(
                    userId,
                    java.sql.Timestamp.valueOf(startDateTime),
                    java.sql.Timestamp.valueOf(endDateTime)
            );
            
            for (Map<String, Object> view : views) {
                try {
                    LocalDate date = ((java.sql.Date) view.get("date")).toLocalDate();
                    Integer count = ((Number) view.get("count")).intValue();
                    
                    TrendDataDTO data = trendDataMap.get(date);
                    if (data != null) {
                        data.setViews(count);
                    }
                } catch (Exception e) {
                    LogUtils.warn(logger, "处理浏览量数据时出错", e);
                }
            }
            
            LogUtils.info(logger, "浏览量数据获取完成");
        } catch (Exception e) {
            LogUtils.error(logger, "获取浏览量数据失败", e);
        }
    }

    private void fetchCommentCountData(Long userId, LocalDate startDate, LocalDate endDate, Map<LocalDate, TrendDataDTO> trendDataMap) {
        try {
            LogUtils.info(logger, "开始获取评论数数据...");
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            List<Map<String, Object>> comments = commentMapper.countByUserIdAndDateRange(
                    userId, 
                    java.sql.Timestamp.valueOf(startDateTime),
                    java.sql.Timestamp.valueOf(endDateTime)
            );
            
            for (Map<String, Object> comment : comments) {
                try {
                    LocalDate date = ((java.sql.Date) comment.get("date")).toLocalDate();
                    Integer count = ((Number) comment.get("count")).intValue();
                    
                    TrendDataDTO data = trendDataMap.get(date);
                    if (data != null) {
                        data.setComments(count);
                    }
                } catch (Exception e) {
                    LogUtils.warn(logger, "处理评论数据时出错", e);
                }
            }
            
            LogUtils.info(logger, "评论数数据获取完成");
        } catch (Exception e) {
            LogUtils.error(logger, "获取评论数数据失败", e);
        }
    }

    private void fetchCollectionCountData(Long userId, LocalDate startDate, LocalDate endDate, Map<LocalDate, TrendDataDTO> trendDataMap) {
        try {
            LogUtils.info(logger, "开始获取收藏数数据...");
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            List<Map<String, Object>> collections = documentCollectionMapper.countByUserIdAndDateRange(
                    userId,
                    java.sql.Timestamp.valueOf(startDateTime),
                    java.sql.Timestamp.valueOf(endDateTime)
            );
            
            for (Map<String, Object> collection : collections) {
                try {
                    LocalDate date = ((java.sql.Date) collection.get("date")).toLocalDate();
                    Integer count = ((Number) collection.get("count")).intValue();
                    
                    TrendDataDTO data = trendDataMap.get(date);
                    if (data != null) {
                        data.setCollections(count);
                    }
                } catch (Exception e) {
                    LogUtils.warn(logger, "处理收藏数据时出错", e);
                }
            }
            
            LogUtils.info(logger, "收藏数数据获取完成");
        } catch (Exception e) {
            LogUtils.error(logger, "获取收藏数数据失败", e);
        }
    }

    private void fetchFollowerGrowthData(Long userId, LocalDate startDate, LocalDate endDate, Map<LocalDate, TrendDataDTO> trendDataMap) {
        try {
            LogUtils.info(logger, "开始获取粉丝增长数据...");
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            List<Map<String, Object>> followers = followMapper.countFollowersByDateRange(
                    userId,
                    java.sql.Timestamp.valueOf(startDateTime),
                    java.sql.Timestamp.valueOf(endDateTime)
            );
            
            for (Map<String, Object> follower : followers) {
                try {
                    LocalDate date = ((java.sql.Date) follower.get("date")).toLocalDate();
                    Integer count = ((Number) follower.get("count")).intValue();
                    
                    TrendDataDTO data = trendDataMap.get(date);
                    if (data != null) {
                        data.setFollowers(count);
                    }
                } catch (Exception e) {
                    LogUtils.warn(logger, "处理粉丝数据时出错", e);
                }
            }
            
            LogUtils.info(logger, "粉丝增长数据获取完成");
        } catch (Exception e) {
            LogUtils.error(logger, "获取粉丝增长数据失败", e);
        }
    }
}