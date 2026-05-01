package com.markdown.editor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.markdown.editor.dto.CommentDTO;
import com.markdown.editor.entity.Comment;
import com.markdown.editor.entity.Document;
import com.markdown.editor.entity.Message;
import com.markdown.editor.entity.User;
import com.markdown.editor.mapper.CommentMapper;
import com.markdown.editor.mapper.UserMapper;
import com.markdown.editor.service.CommentService;
import com.markdown.editor.service.DocumentService;
import com.markdown.editor.service.MessageService;
import com.markdown.editor.service.UserStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private static final ExecutorService DB_QUERY_EXECUTOR = new ThreadPoolExecutor(
            10, 20, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Autowired
    private UserStatisticsService userStatisticsService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<Comment> selectByDocumentId(Long documentId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("document_id", documentId);
        wrapper.orderByDesc("created_at");
        return baseMapper.selectList(wrapper);
    }

    @Override
    public Comment selectById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public int insert(Comment comment) {
        int result = baseMapper.insert(comment);
        if (result > 0 && comment.getDocumentId() != null) {
            Document document = documentService.selectById(comment.getDocumentId());
            if (document != null) {
                document.setCommentCount(document.getCommentCount() + 1);
                documentService.update(document);

                if (document.getUserId() != null) {
                    userStatisticsService.incrementCommentsCount(document.getUserId());
                }
            }
        }
        return result;
    }

    @Override
    public int deleteById(Long id) {
        Comment comment = baseMapper.selectById(id);
        int result = baseMapper.deleteById(id);
        if (result > 0 && comment != null && comment.getDocumentId() != null) {
            Document document = documentService.selectById(comment.getDocumentId());
            if (document != null) {
                if (document.getCommentCount() > 0) {
                    document.setCommentCount(document.getCommentCount() - 1);
                    documentService.update(document);
                }

                if (document.getUserId() != null) {
                    userStatisticsService.decrementCommentsCount(document.getUserId());
                }
            }
        }
        return result;
    }

    @Override
    public int countByDocumentId(Long documentId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("document_id", documentId);
        return baseMapper.selectCount(wrapper).intValue();
    }

    @Override
    public List<Comment> selectByUserId(Long userId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("created_at");
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<Comment> selectByDocumentIds(List<Long> documentIds) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.in("document_id", documentIds);
        wrapper.orderByDesc("created_at");
        return baseMapper.selectList(wrapper);
    }

    @Override
    public CommentListResult getMyCommentsWithDetails(Long userId, int page, int size) {
        long startTime = System.currentTimeMillis();

        List<Comment> comments = selectByUserId(userId);
        long total = comments.size();
        
        if (comments.isEmpty()) {
            return new CommentListResult(Collections.emptyList(), 0, size, page + 1);
        }

        Set<Long> userIds = new HashSet<>();
        Set<Long> documentIds = new HashSet<>();
        Set<Long> parentCommentIds = new HashSet<>();

        for (Comment comment : comments) {
            userIds.add(comment.getUserId());
            documentIds.add(comment.getDocumentId());
            if (comment.getParentId() != null) {
                parentCommentIds.add(comment.getParentId());
            }
        }

        CompletableFuture<Map<Long, User>> userFuture = CompletableFuture.supplyAsync(
                () -> {
                    if (userIds.isEmpty()) {
                        return Collections.emptyMap();
                    }
                    List<User> users = userMapper.selectBatchIds(new ArrayList<>(userIds));
                    Map<Long, User> map = new HashMap<>();
                    for (User user : users) {
                        map.put(user.getId(), user);
                    }
                    return map;
                }, DB_QUERY_EXECUTOR
        );

        CompletableFuture<Map<Long, Document>> documentFuture = CompletableFuture.supplyAsync(
                () -> {
                    if (documentIds.isEmpty()) {
                        return Collections.emptyMap();
                    }
                    List<Document> documents = documentService.selectBatchIds(new ArrayList<>(documentIds));
                    Map<Long, Document> map = new HashMap<>();
                    for (Document doc : documents) {
                        map.put(doc.getId(), doc);
                    }
                    return map;
                }, DB_QUERY_EXECUTOR
        );

        CompletableFuture<Map<Long, Comment>> parentCommentFuture = CompletableFuture.supplyAsync(
                () -> {
                    if (parentCommentIds.isEmpty()) {
                        return Collections.emptyMap();
                    }
                    List<Comment> parentComments = baseMapper.selectBatchIds(new ArrayList<>(parentCommentIds));
                    Map<Long, Comment> map = new HashMap<>();
                    for (Comment pc : parentComments) {
                        map.put(pc.getId(), pc);
                        userIds.add(pc.getUserId());
                    }
                    return map;
                }, DB_QUERY_EXECUTOR
        );

        CompletableFuture.allOf(userFuture, documentFuture, parentCommentFuture).join();

        Map<Long, User> userMap = userFuture.get();
        Map<Long, Document> documentMap = documentFuture.get();
        Map<Long, Comment> parentCommentMap = parentCommentFuture.get();

        if (!parentCommentMap.isEmpty()) {
            Set<Long> existingUserIds = userMap.keySet();
            List<Long> missingUserIds = userIds.stream()
                    .filter(id -> !existingUserIds.contains(id))
                    .collect(Collectors.toList());
            if (!missingUserIds.isEmpty()) {
                List<User> missingUsers = userMapper.selectBatchIds(missingUserIds);
                for (User user : missingUsers) {
                    userMap.put(user.getId(), user);
                }
            }
        }

        List<CommentDTO> dtoList = new ArrayList<>(comments.size());
        for (Comment comment : comments) {
            dtoList.add(convertToDTO(comment, userMap, documentMap, parentCommentMap));
        }

        int start = page * size;
        int end = Math.min(start + size, (int) total);
        if (start >= total) {
            return new CommentListResult(Collections.emptyList(), total, size, page + 1);
        }

        List<CommentDTO> paginatedList = dtoList.subList(start, end);
        
        long endTime = System.currentTimeMillis();
        System.out.println("getMyCommentsWithDetails execution time: " + (endTime - startTime) + "ms");

        return new CommentListResult(paginatedList, total, size, page + 1);
    }

    @Override
    public CommentListResult getReceivedCommentsWithDetails(Long userId, int page, int size) {
        long startTime = System.currentTimeMillis();

        List<Document> documents = documentService.selectByUserId(userId);
        if (documents.isEmpty()) {
            return new CommentListResult(Collections.emptyList(), 0, size, page + 1);
        }

        Map<Long, Document> documentMap = new HashMap<>();
        for (Document doc : documents) {
            documentMap.put(doc.getId(), doc);
        }

        List<Long> documentIds = documents.stream()
                .map(Document::getId)
                .collect(Collectors.toList());

        List<Comment> comments = selectByDocumentIds(documentIds);
        List<Comment> filteredComments = comments.stream()
                .filter(c -> !c.getUserId().equals(userId))
                .collect(Collectors.toList());

        long total = filteredComments.size();
        if (filteredComments.isEmpty()) {
            return new CommentListResult(Collections.emptyList(), 0, size, page + 1);
        }

        Set<Long> userIds = new HashSet<>();
        Set<Long> parentCommentIds = new HashSet<>();

        for (Comment comment : filteredComments) {
            userIds.add(comment.getUserId());
            if (comment.getParentId() != null) {
                parentCommentIds.add(comment.getParentId());
            }
        }

        CompletableFuture<Map<Long, User>> userFuture = CompletableFuture.supplyAsync(
                () -> {
                    if (userIds.isEmpty()) {
                        return Collections.emptyMap();
                    }
                    List<User> users = userMapper.selectBatchIds(new ArrayList<>(userIds));
                    Map<Long, User> map = new HashMap<>();
                    for (User user : users) {
                        map.put(user.getId(), user);
                    }
                    return map;
                }, DB_QUERY_EXECUTOR
        );

        CompletableFuture<Map<Long, Comment>> parentCommentFuture = CompletableFuture.supplyAsync(
                () -> {
                    if (parentCommentIds.isEmpty()) {
                        return Collections.emptyMap();
                    }
                    List<Comment> parentComments = baseMapper.selectBatchIds(new ArrayList<>(parentCommentIds));
                    Map<Long, Comment> map = new HashMap<>();
                    for (Comment pc : parentComments) {
                        map.put(pc.getId(), pc);
                        userIds.add(pc.getUserId());
                    }
                    return map;
                }, DB_QUERY_EXECUTOR
        );

        CompletableFuture.allOf(userFuture, parentCommentFuture).join();

        Map<Long, User> userMap = userFuture.get();
        Map<Long, Comment> parentCommentMap = parentCommentFuture.get();

        if (!parentCommentMap.isEmpty()) {
            Set<Long> existingUserIds = userMap.keySet();
            List<Long> missingUserIds = userIds.stream()
                    .filter(id -> !existingUserIds.contains(id))
                    .collect(Collectors.toList());
            if (!missingUserIds.isEmpty()) {
                List<User> missingUsers = userMapper.selectBatchIds(missingUserIds);
                for (User user : missingUsers) {
                    userMap.put(user.getId(), user);
                }
            }
        }

        List<CommentDTO> dtoList = new ArrayList<>(filteredComments.size());
        for (Comment comment : filteredComments) {
            dtoList.add(convertToDTO(comment, userMap, documentMap, parentCommentMap));
        }

        int start = page * size;
        int end = Math.min(start + size, (int) total);
        if (start >= total) {
            return new CommentListResult(Collections.emptyList(), total, size, page + 1);
        }

        List<CommentDTO> paginatedList = dtoList.subList(start, end);
        
        long endTime = System.currentTimeMillis();
        System.out.println("getReceivedCommentsWithDetails execution time: " + (endTime - startTime) + "ms");

        return new CommentListResult(paginatedList, total, size, page + 1);
    }

    private CommentDTO convertToDTO(Comment comment, Map<Long, User> userMap,
                                    Map<Long, Document> documentMap, Map<Long, Comment> parentCommentMap) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setDocumentId(comment.getDocumentId());
        dto.setUserId(comment.getUserId());
        dto.setParentId(comment.getParentId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());

        User user = userMap.get(comment.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
        }

        Document doc = documentMap.get(comment.getDocumentId());
        if (doc != null) {
            dto.setDocumentTitle(doc.getTitle());
        }

        if (comment.getParentId() != null) {
            Comment parentComment = parentCommentMap.get(comment.getParentId());
            if (parentComment != null) {
                User repliedUser = userMap.get(parentComment.getUserId());
                if (repliedUser != null) {
                    dto.setRepliedUserId(repliedUser.getId());
                    dto.setRepliedUsername(repliedUser.getUsername());
                    dto.setRepliedNickname(repliedUser.getNickname());
                }
            }
        }

        return dto;
    }
}
