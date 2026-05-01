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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

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
    public List<CommentDTO> getMyCommentsWithDetails(Long userId, int page, int size) {
        List<Comment> comments = selectByUserId(userId);
        
        if (comments.isEmpty()) {
            return Collections.emptyList();
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

        Map<Long, User> userMap = new HashMap<>();
        Map<Long, Document> documentMap = new HashMap<>();
        Map<Long, Comment> parentCommentMap = new HashMap<>();

        CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(new ArrayList<>(userIds));
                for (User user : users) {
                    userMap.put(user.getId(), user);
                }
            }
        });

        CompletableFuture<Void> documentFuture = CompletableFuture.runAsync(() -> {
            if (!documentIds.isEmpty()) {
                List<Document> documents = documentService.selectBatchIds(new ArrayList<>(documentIds));
                for (Document doc : documents) {
                    documentMap.put(doc.getId(), doc);
                }
            }
        });

        CompletableFuture<Void> parentCommentFuture = CompletableFuture.runAsync(() -> {
            if (!parentCommentIds.isEmpty()) {
                List<Comment> parentComments = baseMapper.selectBatchIds(parentCommentIds);
                for (Comment pc : parentComments) {
                    parentCommentMap.put(pc.getId(), pc);
                    synchronized (userIds) {
                        userIds.add(pc.getUserId());
                    }
                }
            }
        });

        CompletableFuture.allOf(userFuture, documentFuture, parentCommentFuture).join();

        if (parentCommentMap.size() > 0) {
            List<Long> missingUserIds = userIds.stream()
                    .filter(id -> !userMap.containsKey(id))
                    .collect(Collectors.toList());
            if (!missingUserIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(missingUserIds);
                for (User user : users) {
                    userMap.put(user.getId(), user);
                }
            }
        }

        List<CommentDTO> dtoList = new ArrayList<>();
        for (Comment comment : comments) {
            CommentDTO dto = convertToDTO(comment, userMap, documentMap, parentCommentMap);
            dtoList.add(dto);
        }

        int total = dtoList.size();
        int start = page * size;
        int end = Math.min(start + size, total);
        if (start >= total) {
            return Collections.emptyList();
        }
        return dtoList.subList(start, end);
    }

    @Override
    public List<CommentDTO> getReceivedCommentsWithDetails(Long userId, int page, int size) {
        List<Document> documents = documentService.selectByUserId(userId);
        if (documents.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> documentIds = documents.stream()
                .map(Document::getId)
                .collect(Collectors.toList());

        List<Comment> comments = selectByDocumentIds(documentIds);
        List<Comment> filteredComments = comments.stream()
                .filter(c -> !c.getUserId().equals(userId))
                .collect(Collectors.toList());

        if (filteredComments.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> userIds = new HashSet<>();
        Set<Long> parentCommentIds = new HashSet<>();

        for (Comment comment : filteredComments) {
            userIds.add(comment.getUserId());
            if (comment.getParentId() != null) {
                parentCommentIds.add(comment.getParentId());
            }
        }

        Map<Long, User> userMap = new HashMap<>();
        Map<Long, Comment> parentCommentMap = new HashMap<>();

        CompletableFuture<Void> userFuture = CompletableFuture.runAsync(() -> {
            if (!userIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(new ArrayList<>(userIds));
                for (User user : users) {
                    userMap.put(user.getId(), user);
                }
            }
        });

        CompletableFuture<Void> parentCommentFuture = CompletableFuture.runAsync(() -> {
            if (!parentCommentIds.isEmpty()) {
                List<Comment> parentComments = baseMapper.selectBatchIds(parentCommentIds);
                for (Comment pc : parentComments) {
                    parentCommentMap.put(pc.getId(), pc);
                    synchronized (userIds) {
                        userIds.add(pc.getUserId());
                    }
                }
            }
        });

        CompletableFuture.allOf(userFuture, parentCommentFuture).join();

        if (parentCommentMap.size() > 0) {
            List<Long> missingUserIds = userIds.stream()
                    .filter(id -> !userMap.containsKey(id))
                    .collect(Collectors.toList());
            if (!missingUserIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(missingUserIds);
                for (User user : users) {
                    userMap.put(user.getId(), user);
                }
            }
        }

        Map<Long, Document> documentMap = documents.stream()
                .collect(Collectors.toMap(Document::getId, d -> d));

        List<CommentDTO> dtoList = new ArrayList<>();
        for (Comment comment : filteredComments) {
            CommentDTO dto = convertToDTO(comment, userMap, documentMap, parentCommentMap);
            dtoList.add(dto);
        }

        int total = dtoList.size();
        int start = page * size;
        int end = Math.min(start + size, total);
        if (start >= total) {
            return Collections.emptyList();
        }
        return dtoList.subList(start, end);
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
