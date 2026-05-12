package com.markdown.editor.controller;

import com.markdown.editor.dto.ApiResponse;
import com.markdown.editor.dto.TrendDataDTO;
import com.markdown.editor.service.TrendService;
import com.markdown.editor.utils.LogUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trend")
@Api(tags = "趋势分析", description = "文章浏览、点赞、粉丝等趋势数据查询的接口")
public class TrendController {

    private static final Logger logger = LogUtils.getLogger(TrendController.class);

    @Autowired
    private TrendService trendService;

    @GetMapping("/data")
    public ResponseEntity<ApiResponse<List<TrendDataDTO>>> getTrendData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        try {
            // 获取当前登录用户ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            com.markdown.editor.security.UserDetailsImpl userDetails = 
                    (com.markdown.editor.security.UserDetailsImpl) auth.getPrincipal();
            Long userId = userDetails.getId();
            
            // 解析日期参数，默认最近7天
            LocalDate start = startDate != null && !startDate.isEmpty() 
                    ? LocalDate.parse(startDate) 
                    : LocalDate.now().minusDays(6);
            
            LocalDate end = endDate != null && !endDate.isEmpty() 
                    ? LocalDate.parse(endDate) 
                    : LocalDate.now();
            
            LocalDate today = LocalDate.now();
            LocalDate oneYearAgo = today.minusYears(1);
            
            // 验证日期范围
            if (start.isBefore(oneYearAgo)) {
                LogUtils.warn(logger, "日期范围验证失败 - 开始日期超出一年范围: {}", start);
                return ResponseEntity.ok(ApiResponse.error("只能访问最近一年的数据"));
            }
            
            if (end.isAfter(today)) {
                LogUtils.warn(logger, "日期范围验证失败 - 结束日期不能大于今天: {}", end);
                return ResponseEntity.ok(ApiResponse.error("结束日期不能大于今天"));
            }
            
            if (start.isAfter(end)) {
                LogUtils.warn(logger, "日期范围验证失败 - 开始日期大于结束日期: {} 至 {}", start, end);
                return ResponseEntity.ok(ApiResponse.error("开始日期不能大于结束日期"));
            }
            
            // 验证日期区间最多30天
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end);
            if (daysBetween > 30) {
                LogUtils.warn(logger, "日期范围验证失败 - 日期区间超过30天: {} 天", daysBetween);
                return ResponseEntity.ok(ApiResponse.error("日期区间最多显示一个月（30天）"));
            }
            
            LogUtils.info(logger, "获取趋势数据请求 - 用户ID: {}, 开始日期: {}, 结束日期: {}", userId, start, end);
            
            // 调用Service获取趋势数据
            List<TrendDataDTO> trendData = trendService.getTrendData(userId, start, end);
            
            LogUtils.info(logger, "趋势数据获取成功 - 返回 {} 条记录", trendData.size());
            
            return ResponseEntity.ok(ApiResponse.success(trendData));
            
        } catch (Exception e) {
            LogUtils.error(logger, "获取趋势数据失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取趋势数据失败: " + e.getMessage()));
        }
    }

    @GetMapping("/data/{days}")
    public ResponseEntity<ApiResponse<List<TrendDataDTO>>> getTrendDataByDays(
            @PathVariable int days) {
        
        try {
            // 获取当前登录用户ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            com.markdown.editor.security.UserDetailsImpl userDetails = 
                    (com.markdown.editor.security.UserDetailsImpl) auth.getPrincipal();
            Long userId = userDetails.getId();
            
            // 根据天数计算日期范围
            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(days - 1);
            
            LogUtils.info(logger, "获取趋势数据请求(按天数) - 用户ID: {}, 天数: {}, 日期范围: {} 至 {}", userId, days, start, end);
            
            // 调用Service获取趋势数据
            List<TrendDataDTO> trendData = trendService.getTrendData(userId, start, end);
            
            LogUtils.info(logger, "趋势数据获取成功 - 返回 {} 条记录", trendData.size());
            
            return ResponseEntity.ok(ApiResponse.success(trendData));
            
        } catch (Exception e) {
            LogUtils.error(logger, "获取趋势数据失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取趋势数据失败: " + e.getMessage()));
        }
    }
}