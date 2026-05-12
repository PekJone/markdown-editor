package com.markdown.editor.controller;

import com.markdown.editor.constant.SystemConstant;
import com.markdown.editor.dto.ApiResponse;
import com.markdown.editor.dto.CategoryDocumentVO;
import com.markdown.editor.dto.DocumentRequest;
import com.markdown.editor.dto.PageResponseVO;
import com.markdown.editor.entity.Document;
import com.markdown.editor.entity.DocumentCollection;
import com.markdown.editor.entity.DocumentLike;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markdown.editor.security.UserDetailsImpl;
import com.markdown.editor.service.DocumentService;
import com.markdown.editor.service.DocumentCollectionService;
import com.markdown.editor.service.DocumentLikeService;
import com.markdown.editor.service.DictionaryService;
import com.markdown.editor.service.MessageService;
import com.markdown.editor.service.CommentService;
import com.markdown.editor.entity.Message;
import com.markdown.editor.utils.LogUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;

import javax.validation.Valid;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/documents")
@Api(tags = "文档管理", description = "文档的创建、修改、删除、查询、收藏、点赞等接口")
public class DocumentController {

    private static final Logger logger = LogUtils.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentCollectionService documentCollectionService;

    @Autowired
    private DocumentLikeService documentLikeService;

    @Autowired
    private DictionaryService dictionaryService;
    
    @Autowired
    private com.markdown.editor.service.TagService tagService;

    @Autowired
    private MessageService messageService;
    
    @Autowired
    private CommentService commentService;
    
    @Autowired
    private com.markdown.editor.service.DocumentViewStatService documentViewStatService;
    
    private String getCategoryCodeFromDictionary(String category) {
        List<com.markdown.editor.entity.Dictionary> dictionaries = dictionaryService.selectByDictType("article_category");
        for (com.markdown.editor.entity.Dictionary dict : dictionaries) {
            String dictLabel = dict.getDictLabel();
            if (dictLabel != null && dictLabel.equals(category)) {
                return dict.getDictCode();
            }
        }
        return category;
    }

    @GetMapping
    public ResponseEntity<Page<Document>> getAllDocumentsExceptSecret(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]获取所有文档列表，页码: {}, 每页大小: {}", 
                userDetails.getUsername(), page, size);
        
        try {
            Page<Document> pageObject = new Page<>(page, size);
            // 获取除了绝密以外的所有文档
            Page<Document> documents = documentService.selectAllExceptSecretPage(pageObject, null, null, null);
            LogUtils.info(logger, "用户[{}]获取所有文档列表成功，共{}个文档", userDetails.getUsername(), documents.getTotal());
            
            List<com.markdown.editor.entity.Dictionary> categoryDicts = dictionaryService.selectByDictType("article_category");
            java.util.Map<String, String> categoryMap = new java.util.HashMap<>();
            for (com.markdown.editor.entity.Dictionary dict : categoryDicts) {
                String dictCode = dict.getDictCode();
                String dictLabel = dict.getDictLabel();
                if (dictCode != null && dictLabel != null) {
                    categoryMap.put(dictCode, dictLabel);
                }
            }
            
            List<Document> records = documents.getRecords();
            for (Document doc : records) {
                String docCategory = doc.getCategory();
                if (docCategory != null && !docCategory.isEmpty()) {
                    String categoryName = categoryMap.get(docCategory);
                    if (categoryName != null) {
                        doc.setCategory(categoryName);
                    }
                }
            }
            
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]获取所有文档列表失败", e, userDetails.getUsername());
            throw e;
        }
    }

    @GetMapping("/my")
    public ResponseEntity<Page<Document>> getMyDocuments(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]获取自己的文档列表，页码: {}, 每页大小: {}", 
                userDetails.getUsername(), page, size);
        
        try {
            Page<Document> pageObject = new Page<>(page, size);
            // 使用优化后的查询，一次性获取文档及其评论数和收藏数
            Page<Document> documents = documentService.selectPageWithCounts(pageObject, userDetails.getId(), null, null, null);
            LogUtils.info(logger, "用户[{}]获取自己的文档列表成功，共{}个文档", userDetails.getUsername(), documents.getTotal());
            
            List<com.markdown.editor.entity.Dictionary> categoryDicts = dictionaryService.selectByDictType("article_category");
            java.util.Map<String, String> categoryMap = new java.util.HashMap<>();
            for (com.markdown.editor.entity.Dictionary dict : categoryDicts) {
                String dictCode = dict.getDictCode();
                String dictLabel = dict.getDictLabel();
                if (dictCode != null && dictLabel != null) {
                    categoryMap.put(dictCode, dictLabel);
                }
            }
            
            List<Document> records = documents.getRecords();
            for (Document doc : records) {
                String docCategory = doc.getCategory();
                if (docCategory != null && !docCategory.isEmpty()) {
                    String categoryName = categoryMap.get(docCategory);
                    if (categoryName != null) {
                        doc.setCategory(categoryName);
                    }
                }
            }
            
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]获取自己的文档列表失败", e, userDetails.getUsername());
            throw e;
        }
    }

    @GetMapping("/public")
    public ResponseEntity<Page<Document>> getPublicDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        LogUtils.info(logger, "获取公开文档列表，页码: {}, 每页大小: {}", page, size);
        
        try {
            // MyBatis-Plus的Page对象的current参数是从1开始的，所以需要将page参数加1
            int currentPage = page + 1;
            LogUtils.info(logger, "调整后的页码: {}", currentPage);
            
            Page<Document> pageObject = new Page<>(currentPage, size);
            LogUtils.info(logger, "创建Page对象，页码: {}, 每页大小: {}", pageObject.getCurrent(), pageObject.getSize());
            
            Page<Document> documents = documentService.selectPublicPage(pageObject, null, null, null);
            LogUtils.info(logger, "获取公开文档列表成功，共{}个文档，当前页码: {}, 每页大小: {}, 总页数: {}", 
                documents.getTotal(), documents.getCurrent(), documents.getSize(), documents.getPages());
            LogUtils.info(logger, "返回的记录数: {}", documents.getRecords().size());
            
            List<com.markdown.editor.entity.Dictionary> categoryDicts = dictionaryService.selectByDictType("article_category");
            java.util.Map<String, String> categoryMap = new java.util.HashMap<>();
            for (com.markdown.editor.entity.Dictionary dict : categoryDicts) {
                String dictCode = dict.getDictCode();
                String dictLabel = dict.getDictLabel();
                if (dictCode != null && dictLabel != null) {
                    categoryMap.put(dictCode, dictLabel);
                }
            }
            
            List<Document> records = documents.getRecords();
            for (Document doc : records) {
                String docCategory = doc.getCategory();
                if (docCategory != null && !docCategory.isEmpty()) {
                    String categoryName = categoryMap.get(docCategory);
                    if (categoryName != null) {
                        doc.setCategory(categoryName);
                    }
                }
            }
            
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            LogUtils.error(logger, "获取公开文档列表失败", e);
            throw e;
        }
    }
    
    @GetMapping("/public/search")
    public ResponseEntity<Page<Document>> searchPublicDocuments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        String trimmedKeyword = keyword.trim();
        LogUtils.info(logger, "搜索公开文档，关键词: {}, 页码: {}, 每页大小: {}", trimmedKeyword, page, size);
        
        try {
            Page<Document> pageObject = new Page<>(page, size);
            Page<Document> documents = documentService.selectPublicPage(pageObject, null, null, trimmedKeyword);
            LogUtils.info(logger, "搜索公开文档成功，找到{}个结果", documents.getTotal());
            
            List<com.markdown.editor.entity.Dictionary> categoryDicts = dictionaryService.selectByDictType("article_category");
            java.util.Map<String, String> categoryMap = new java.util.HashMap<>();
            for (com.markdown.editor.entity.Dictionary dict : categoryDicts) {
                String dictCode = dict.getDictCode();
                String dictLabel = dict.getDictLabel();
                if (dictCode != null && dictLabel != null) {
                    categoryMap.put(dictCode, dictLabel);
                }
            }
            
            List<Document> records = documents.getRecords();
            for (Document doc : records) {
                String docCategory = doc.getCategory();
                if (docCategory != null && !docCategory.isEmpty()) {
                    String categoryName = categoryMap.get(docCategory);
                    if (categoryName != null) {
                        doc.setCategory(categoryName);
                    }
                }
            }
            
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            LogUtils.error(logger, "搜索公开文档失败，关键词: {}", e, trimmedKeyword);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDocumentById(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]获取文档，文档ID: {}", userDetails.getUsername(), id);
        
        try {
            Document document = documentService.selectById(id);
            if (document == null) {
                LogUtils.warn(logger, "用户[{}]尝试获取不存在的文档，文档ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.notFound().build();
            }
            
            String permission = document.getPermission();
            Long userId = document.getUserId();
            
            if (SystemConstant.PERMISSION_SECRET.equals(permission) && !userId.equals(userDetails.getId()) && !SystemConstant.ROLE_ADMIN.equals(userDetails.getRole())) {
                LogUtils.warn(logger, "用户[{}]尝试访问无权查看的绝密文档，文档ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.status(403).body(ApiResponse.error("无权限访问"));
            }
            
            documentService.incrementViewCount(id);
            documentViewStatService.recordView(id, userDetails.getId());
            LogUtils.debug(logger, "文档ID: {} 浏览次数增加", id);
            document = documentService.selectById(id);
            
            LogUtils.info(logger, "用户[{}]获取文档成功，文档ID: {}", userDetails.getUsername(), id);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]获取文档失败，文档ID: {}", e, userDetails.getUsername(), id);
            throw e;
        }
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicDocumentById(@PathVariable Long id) {
        LogUtils.info(logger, "获取公开文档，文档ID: {}", id);
        
        try {
            Document document = documentService.selectById(id);
            if (document == null) {
                LogUtils.warn(logger, "尝试获取不存在的文档，文档ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            String permission = document.getPermission();
            if (!SystemConstant.PERMISSION_PUBLIC.equals(permission)) {
                LogUtils.warn(logger, "尝试访问非公开文档，文档ID: {}", id);
                return ResponseEntity.status(403).body(ApiResponse.error("文档不是公开的"));
            }
            
            documentService.incrementViewCount(id);
            // 记录每日浏览量统计（公开文档没有认证用户，传入null）
            documentViewStatService.recordView(id, null);
            LogUtils.debug(logger, "文档ID: {} 浏览次数增加", id);
            // 重新获取文档以获取最新的浏览次数
            document = documentService.selectById(id);
            
            LogUtils.info(logger, "获取公开文档成功，文档ID: {}", id);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            LogUtils.error(logger, "获取公开文档失败，文档ID: {}", e, id);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<?> createDocument(
            @Valid @RequestBody DocumentRequest documentRequest,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]创建文档，标题: {}", userDetails.getUsername(), documentRequest.getTitle());
        
        try {
            // 验证标签（如果有）
            if (documentRequest.getTags() != null && !documentRequest.getTags().isEmpty()) {
                String category = documentRequest.getCategory();
                if (category == null || category.isEmpty()) {
                    LogUtils.warn(logger, "用户[{}]创建文档失败，标签验证失败：未指定分类", userDetails.getUsername());
                    return ResponseEntity.badRequest().body("标签验证失败：未指定分类");
                }
                
                // 检查标签是否存在于用户的标签列表中
                boolean tagExists = tagService.existsByUserIdAndCategoryAndTagName(
                        userDetails.getId(), category, documentRequest.getTags());
                if (!tagExists) {
                    LogUtils.warn(logger, "用户[{}]创建文档失败，标签验证失败：标签[{}]不存在", 
                            userDetails.getUsername(), documentRequest.getTags());
                    return ResponseEntity.badRequest().body("标签验证失败：只能选择自己创建的标签");
                }
            }
            
            Document document = new Document();
            document.setTitle(documentRequest.getTitle());
            document.setContent(documentRequest.getContent());
            document.setUserId(userDetails.getId());
            document.setCategory(documentRequest.getCategory());
            document.setTags(documentRequest.getTags());
            document.setPermission(documentRequest.getPermission());
            
            documentService.insert(document);
            
            Long documentId = document.getId();
            
            LogUtils.info(logger, "用户[{}]创建文档成功，文档ID: {}", userDetails.getUsername(), documentId);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]创建文档失败，标题: {}", e, userDetails.getUsername(), documentRequest.getTitle());
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequest documentRequest,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]更新文档，文档ID: {}, 新标题: {}", userDetails.getUsername(), id, documentRequest.getTitle());
        
        try {
            Document document = documentService.selectById(id);
            if (document == null) {
                LogUtils.warn(logger, "用户[{}]尝试更新不存在的文档，文档ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.notFound().build();
            }
            
            Long userId = document.getUserId();
            if (!userId.equals(userDetails.getId())) {
                LogUtils.warn(logger, "用户[{}]尝试更新无权修改的文档，文档ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.status(403).body("无权限修改：只有文章的创建者才能编辑文章");
            }
            
            // 验证标签（如果有）
            if (documentRequest.getTags() != null && !documentRequest.getTags().isEmpty()) {
                String category = documentRequest.getCategory();
                if (category == null || category.isEmpty()) {
                    LogUtils.warn(logger, "用户[{}]更新文档失败，标签验证失败：未指定分类", userDetails.getUsername());
                    return ResponseEntity.badRequest().body("标签验证失败：未指定分类");
                }
                
                // 检查标签是否存在于用户的标签列表中
                boolean tagExists = tagService.existsByUserIdAndCategoryAndTagName(
                        userDetails.getId(), category, documentRequest.getTags());
                if (!tagExists) {
                    LogUtils.warn(logger, "用户[{}]更新文档失败，标签验证失败：标签[{}]不存在", 
                            userDetails.getUsername(), documentRequest.getTags());
                    return ResponseEntity.badRequest().body("标签验证失败：只能选择自己创建的标签");
                }
            }
            
            document.setTitle(documentRequest.getTitle());
            document.setContent(documentRequest.getContent());
            document.setCategory(documentRequest.getCategory());
            document.setTags(documentRequest.getTags());
            document.setPermission(documentRequest.getPermission());
            
            documentService.update(document);
            LogUtils.info(logger, "用户[{}]更新文档成功，文档ID: {}", userDetails.getUsername(), id);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]更新文档失败，文档ID: {}", e, userDetails.getUsername(), id);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]删除文档，文档ID: {}", userDetails.getUsername(), id);
        
        try {
            Document document = documentService.selectById(id);
            if (document == null) {
                LogUtils.warn(logger, "用户[{}]尝试删除不存在的文档，文档ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.notFound().build();
            }
            
            Long userId = document.getUserId();
            if (!userId.equals(userDetails.getId())) {
                LogUtils.warn(logger, "用户[{}]尝试删除无权删除的文档，文档ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.status(403).body(ApiResponse.error("无权限删除"));
            }
            
            documentService.deleteById(id);
            LogUtils.info(logger, "用户[{}]删除文档成功，文档ID: {}", userDetails.getUsername(), id);
            return ResponseEntity.ok(ApiResponse.success("文档删除成功", null));
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]删除文档失败，文档ID: {}", e, userDetails.getUsername(), id);
            throw e;
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Document>> searchDocuments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String scope,
            @RequestParam(required = false) String category,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String trimmedKeyword = keyword.trim();
        LogUtils.info(logger, "用户[{}]搜索文档，关键词: {}, 范围: {}, 分类: {}, 页码: {}, 每页大小: {}", 
                userDetails.getUsername(), trimmedKeyword, scope, category, page, size);
        
        try {
            Page<Document> pageObject = new Page<>(page, size);
            Page<Document> documents;
            if ("my".equals(scope)) {
                // 搜索用户自己的文档
                documents = documentService.selectPage(pageObject, userDetails.getId(), category, null, trimmedKeyword);
            } else {
                // 搜索除了绝密以外的所有文档
                documents = documentService.selectAllExceptSecretPage(pageObject, category, null, trimmedKeyword);
            }
            LogUtils.info(logger, "用户[{}]搜索文档成功，找到{}个结果", userDetails.getUsername(), documents.getTotal());
            
            List<com.markdown.editor.entity.Dictionary> categoryDicts = dictionaryService.selectByDictType("article_category");
            java.util.Map<String, String> categoryMap = new java.util.HashMap<>();
            for (com.markdown.editor.entity.Dictionary dict : categoryDicts) {
                String dictCode = dict.getDictCode();
                String dictLabel = dict.getDictLabel();
                if (dictCode != null && dictLabel != null) {
                    categoryMap.put(dictCode, dictLabel);
                }
            }
            
            List<Document> records = documents.getRecords();
            for (Document doc : records) {
                String docCategory = doc.getCategory();
                if (docCategory != null && !docCategory.isEmpty()) {
                    String categoryName = categoryMap.get(docCategory);
                    if (categoryName != null) {
                        doc.setCategory(categoryName);
                    }
                }
            }
            
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]搜索文档失败，关键词: {}", e, userDetails.getUsername(), trimmedKeyword);
            throw e;
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<Document>> getDocumentsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // 从字典表获取分类信息，支持中文名称或拼音code
        String categoryCode = getCategoryCodeFromDictionary(category);
        LogUtils.info(logger, "用户[{}]获取分类文档，分类: {}, 分类code: {}, 页码: {}, 每页大小: {}", 
                userDetails.getUsername(), category, categoryCode, page, size);
        
        try {
            // 获取除了绝密以外的指定分类文档
            Page<Document> pageObject = new Page<>(page, size);
            Page<Document> documentsPage = documentService.selectAllExceptSecretPage(pageObject, categoryCode, null, null);
            
            List<com.markdown.editor.entity.Dictionary> categoryDicts = dictionaryService.selectByDictType("article_category");
            java.util.Map<String, String> categoryMap = new java.util.HashMap<>();
            for (com.markdown.editor.entity.Dictionary dict : categoryDicts) {
                String dictCode = dict.getDictCode();
                String dictLabel = dict.getDictLabel();
                if (dictCode != null && dictLabel != null) {
                    categoryMap.put(dictCode, dictLabel);
                }
            }
            
            List<Document> records = documentsPage.getRecords();
            for (Document doc : records) {
                String docCategory = doc.getCategory();
                if (docCategory != null && !docCategory.isEmpty()) {
                    String categoryName = categoryMap.get(docCategory);
                    if (categoryName != null) {
                        doc.setCategory(categoryName);
                    }
                }
            }
            
            LogUtils.info(logger, "用户[{}]获取分类文档成功，共{}个文档", userDetails.getUsername(), documentsPage.getTotal());
            return ResponseEntity.ok(documentsPage);
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]获取分类文档失败，分类: {}", e, userDetails.getUsername(), category);
            throw e;
        }
    }

    // 收藏文档
    @PostMapping("/{id}/collect")
    public ResponseEntity<?> collectDocument(
            @PathVariable Long id,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]收藏文档，文档ID: {}", userDetails.getUsername(), id);
        
        try {
            // 检查文档是否存在
            if (documentService.selectById(id) == null) {
                LogUtils.warn(logger, "用户[{}]尝试收藏不存在的文档，文档ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.notFound().build();
            }
            
            // 检查是否已经收藏
            if (documentCollectionService.existsByUserIdAndDocumentId(userDetails.getId(), id)) {
                LogUtils.info(logger, "用户[{}]已经收藏过文档，文档ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.ok(ApiResponse.success("已经收藏过该文档", null));
            }
            
            DocumentCollection collection = new DocumentCollection();
            collection.setUserId(userDetails.getId());
            collection.setDocumentId(id);
            documentCollectionService.insert(collection);
            
            // 获取文档信息
            Document document = documentService.selectById(id);
            
            Long documentUserId = document.getUserId();
            
            if (!documentUserId.equals(userDetails.getId())) {
                String title = document.getTitle();
                
                Message message = new Message();
                message.setSenderId(userDetails.getId());
                message.setReceiverId(documentUserId);
                message.setType("collect");
                message.setContent("您的文章《" + title + "》被收藏了");
                message.setRelatedId(id);
                
                messageService.sendMessage(message);
            }
            
            LogUtils.info(logger, "用户[{}]收藏文档成功，文档ID: {}", userDetails.getUsername(), id);
            return ResponseEntity.ok(ApiResponse.success("收藏成功", null));
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]收藏文档失败，文档ID: {}", e, userDetails.getUsername(), id);
            throw e;
        }
    }

    // 取消收藏
    @DeleteMapping("/{id}/collect")
    public ResponseEntity<?> uncollectDocument(
            @PathVariable Long id,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]取消收藏文档，文档ID: {}", userDetails.getUsername(), id);
        
        try {
            // 检查是否已经收藏
            DocumentCollection collection = documentCollectionService.selectByUserIdAndDocumentId(userDetails.getId(), id);
            if (collection == null) {
                LogUtils.info(logger, "用户[{}]尚未收藏文档，文档ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.ok(ApiResponse.success("尚未收藏该文档", null));
            }
            
            documentCollectionService.deleteById(collection.getId());
            
            LogUtils.info(logger, "用户[{}]取消收藏文档成功，文档ID: {}", userDetails.getUsername(), id);
            return ResponseEntity.ok(ApiResponse.success("取消收藏成功", null));
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]取消收藏文档失败，文档ID: {}", e, userDetails.getUsername(), id);
            throw e;
        }
    }

    // 获取收藏状态
    @GetMapping("/{id}/collect/status")
    public ResponseEntity<?> getCollectStatus(
            @PathVariable Long id,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]获取文档收藏状态，文档ID: {}", userDetails.getUsername(), id);
        
        try {
            // 检查是否已经收藏
            boolean isCollected = documentCollectionService.existsByUserIdAndDocumentId(userDetails.getId(), id);
            LogUtils.info(logger, "用户[{}]获取文档收藏状态成功，文档ID: {}, 状态: {}", userDetails.getUsername(), id, isCollected);
            return ResponseEntity.ok(ApiResponse.success("获取收藏状态成功", isCollected));
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]获取文档收藏状态失败，文档ID: {}", e, userDetails.getUsername(), id);
            throw e;
        }
    }

    // 获取点赞状态
    @GetMapping("/{id}/like/status")
    public ResponseEntity<?> getLikeStatus(
            @PathVariable Long id,
            Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]获取文档点赞状态，文档ID: {}", userDetails.getUsername(), id);

        try {
            boolean isLiked = documentLikeService.existsByUserIdAndDocumentId(userDetails.getId(), id);
            LogUtils.info(logger, "用户[{}]获取文档点赞状态成功，文档ID: {}, 状态: {}", userDetails.getUsername(), id, isLiked);
            return ResponseEntity.ok(ApiResponse.success("获取点赞状态成功", isLiked));
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]获取文档点赞状态失败，文档ID: {}", e, userDetails.getUsername(), id);
            throw e;
        }
    }

    // 点赞文档
    @PostMapping("/{id}/like")
    @Transactional
    public ResponseEntity<?> likeDocument(
            @PathVariable Long id,
            Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]点赞文档，文档ID: {}", userDetails.getUsername(), id);

        try {
            // 检查文档是否存在
            if (documentService.selectById(id) == null) {
                LogUtils.warn(logger, "用户[{}]尝试点赞不存在的文档，文档ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.notFound().build();
            }

            // 检查用户是否已经点赞过
            if (documentLikeService.existsByUserIdAndDocumentId(userDetails.getId(), id)) {
                LogUtils.warn(logger, "用户[{}]尝试重复点赞，文档ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.ok(ApiResponse.error("您已经点过赞了"));
            }

            DocumentLike like = new DocumentLike();
            like.setUserId(userDetails.getId());
            like.setDocumentId(id);
            like.setCreatedAt(new Date());
            documentLikeService.insert(like);

            // 增加点赞数
            documentService.incrementLikeCount(id);
            
            // 获取更新后的文档
            Document document = documentService.selectById(id);

            Long documentUserId = document.getUserId();
            
            if (!documentUserId.equals(userDetails.getId())) {
                String title = document.getTitle();
                
                Message message = new Message();
                message.setSenderId(userDetails.getId());
                message.setReceiverId(documentUserId);
                message.setType("like");
                message.setContent("您的文章《" + title + "》收到了新点赞");
                message.setRelatedId(id);
                
                messageService.sendMessage(message);
            }
            
            Integer likeCount = document.getLikeCount();
            
            LogUtils.info(logger, "用户[{}]点赞文档成功，文档ID: {}, 点赞数: {}", userDetails.getUsername(), id, likeCount);
            return ResponseEntity.ok(ApiResponse.success("点赞成功", likeCount));
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]点赞文档失败，文档ID: {}", e, userDetails.getUsername(), id);
            throw e;
        }
    }
    
    // 取消点赞
    @DeleteMapping("/{id}/like")
    @Transactional
    public ResponseEntity<?> unlikeDocument(
            @PathVariable Long id,
            Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        LogUtils.info(logger, "用户[{}]取消点赞文档，文档ID: {}", userDetails.getUsername(), id);

        try {
            // 检查文档是否存在
            if (documentService.selectById(id) == null) {
                LogUtils.warn(logger, "用户[{}]尝试取消点赞不存在的文档，文档ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.notFound().build();
            }

            // 检查用户是否点赞过
            DocumentLike like = documentLikeService.selectByUserIdAndDocumentId(userDetails.getId(), id);
            if (like == null) {
                LogUtils.warn(logger, "用户[{}]尝试取消未点赞的文档，文档ID: {}", userDetails.getUsername(), id);
                return ResponseEntity.ok(ApiResponse.error("您还没有点赞"));
            }

            Long likeId = like.getId();
            if (likeId != null) {
                documentLikeService.deleteById(likeId);
            }

            // 减少点赞数
            documentService.decrementLikeCount(id);
            
            Document document = documentService.selectById(id);

            Integer likeCount = document.getLikeCount();

            LogUtils.info(logger, "用户[{}]取消点赞文档成功，文档ID: {}, 点赞数: {}", userDetails.getUsername(), id, likeCount);
            return ResponseEntity.ok(ApiResponse.success("取消点赞成功", likeCount));
        } catch (Exception e) {
            LogUtils.error(logger, "用户[{}]取消点赞文档失败，文档ID: {}", e, userDetails.getUsername(), id);
            throw e;
        }
    }

    @GetMapping("/author/{userId}")
    public ResponseEntity<List<CategoryDocumentVO>> getAuthorDocuments(
            @PathVariable Long userId,
            @RequestParam(required = false) Long excludeId) {
        
        LogUtils.info(logger, "获取作者的其他文章，作者ID: {}, 排除文档ID: {}", userId, excludeId);
        
        try {
            List<Document> documents = documentService.selectUserDocumentsWithStats(userId);
            
            if (excludeId != null) {
                documents = documents.stream()
                        .filter(doc -> !doc.getId().equals(excludeId))
                        .collect(java.util.stream.Collectors.toList());
            }
            
            Map<String, Document> categoryMap = new LinkedHashMap<>();
            for (Document doc : documents) {
                String category = "other";
                String docCategory = doc.getCategory();
                if (docCategory != null && !docCategory.isEmpty()) {
                    category = docCategory;
                }
                if (!categoryMap.containsKey(category)) {
                    categoryMap.put(category, doc);
                }
            }
            
            List<com.markdown.editor.entity.Dictionary> categoryDicts = dictionaryService.selectByDictType("article_category");
            
            List<CategoryDocumentVO> result = new ArrayList<>();
            int count = 0;
            for (com.markdown.editor.entity.Dictionary dict : categoryDicts) {
                if (count >= 5) {
                    break;
                }
                
                String categoryCode = dict.getDictCode();
                String categoryLabel = dict.getDictLabel();
                
                if (categoryCode != null && categoryLabel != null) {
                    CategoryDocumentVO item = new CategoryDocumentVO();
                    item.setCategory(categoryLabel);
                    item.setDocument(categoryMap.get(categoryCode));
                    result.add(item);
                    count++;
                }
            }
            
            if (count < 5) {
                CategoryDocumentVO item = new CategoryDocumentVO();
                item.setCategory("未分类");
                item.setDocument(categoryMap.get("other"));
                result.add(item);
            }
            
            LogUtils.info(logger, "获取作者的其他文章成功，找到{}个分类", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            LogUtils.error(logger, "获取作者的其他文章失败，作者ID: {}", e, userId);
            throw e;
        }
    }

    @GetMapping("/author/{userId}/all")
    public ResponseEntity<PageResponseVO<Document>> getAuthorAllDocuments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        LogUtils.info(logger, "获取作者的所有文章，作者ID: {}, 页码: {}, 每页大小: {}", userId, page, size);
        
        try {
            List<Document> documents = documentService.selectUserDocumentsWithStats(userId);
            
            int total = documents.size();
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);
            List<Document> pagedDocuments = new ArrayList<>();
            if (start < total) {
                pagedDocuments = documents.subList(start, end);
            }
            
            PageResponseVO<Document> response = new PageResponseVO<>(pagedDocuments, total, page, size);
            
            LogUtils.info(logger, "获取作者的所有文章成功，找到{}个结果，当前页显示{}个", total, pagedDocuments.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LogUtils.error(logger, "获取作者的所有文章失败，作者ID: {}", e, userId);
            throw e;
        }
    }



    @GetMapping("/author/{userId}/category/{category}")
    public ResponseEntity<PageResponseVO<Document>> getAuthorDocumentsByCategory(
            @PathVariable Long userId,
            @PathVariable String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        String categoryCode = getCategoryCodeFromDictionary(category);
        LogUtils.info(logger, "获取作者指定分类的文章，作者ID: {}, 分类: {}, 分类code: {}, 页码: {}, 每页大小: {}", 
                userId, category, categoryCode, page, size);
        
        try {
            List<Document> documents = documentService.selectByUserId(userId);
            
            documents = documents.stream()
                    .filter(doc -> {
                        String docCategory = "other";
                        String docCategoryValue = doc.getCategory();
                        if (docCategoryValue != null && !docCategoryValue.isEmpty()) {
                            docCategory = docCategoryValue;
                        }
                        return docCategory.equals(categoryCode);
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            int total = documents.size();
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);
            List<Document> pagedDocuments = new ArrayList<>();
            if (start < total) {
                pagedDocuments = documents.subList(start, end);
            }
            
            PageResponseVO<Document> response = new PageResponseVO<>(pagedDocuments, total, page, size);
            
            LogUtils.info(logger, "获取作者指定分类的文章成功，找到{}个结果，当前页显示{}个", total, pagedDocuments.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LogUtils.error(logger, "获取作者指定分类的文章失败，作者ID: {}, 分类: {}", e, userId, category);
            throw e;
        }
    }
    
    // 获取文章分类列表
    @GetMapping("/categories/list")
    public ResponseEntity<ApiResponse> getCategoryList() {
        try {
            LogUtils.info(logger, "获取文章分类列表");
            
            // 从字典表获取文章分类
            List<com.markdown.editor.entity.Dictionary> categoryDicts = dictionaryService.selectByDictType("article_category");
            
            LogUtils.info(logger, "获取文章分类列表成功，分类数量: {}", categoryDicts.size());
            return ResponseEntity.ok(new ApiResponse(true, "获取分类列表成功", categoryDicts));
        } catch (Exception e) {
            LogUtils.error(logger, "获取文章分类列表失败", e);
            return ResponseEntity.ok(new ApiResponse(false, "获取分类列表失败", null));
        }
    }
    
    // 获取文章权限列表
    @GetMapping("/permissions/list")
    public ResponseEntity<ApiResponse> getPermissionList(Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            LogUtils.info(logger, "用户[{}]获取文章权限列表", userDetails.getUsername());
            
            // 从字典表获取文章权限
            List<com.markdown.editor.entity.Dictionary> permissionDicts = dictionaryService.selectByDictType("article_permission");
            
            LogUtils.info(logger, "用户[{}]获取文章权限列表成功，权限数量: {}", userDetails.getUsername(), permissionDicts.size());
            return ResponseEntity.ok(new ApiResponse(true, "获取权限列表成功", permissionDicts));
        } catch (Exception e) {
            LogUtils.error(logger, "获取文章权限列表失败", e);
            return ResponseEntity.ok(new ApiResponse(false, "获取权限列表失败", null));
        }
    }
}

