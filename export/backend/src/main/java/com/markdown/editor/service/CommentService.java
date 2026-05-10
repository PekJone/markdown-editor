package com.markdown.editor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.markdown.editor.dto.CommentDTO;
import com.markdown.editor.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    List<Comment> selectByDocumentId(Long documentId);

    Comment selectById(Long id);

    int insert(Comment comment);

    int deleteById(Long id);

    int countByDocumentId(Long documentId);

    List<Comment> selectByUserId(Long userId);

    List<Comment> selectByDocumentIds(List<Long> documentIds);

    CommentListResult getMyCommentsWithDetails(Long userId, int page, int size);

    CommentListResult getReceivedCommentsWithDetails(Long userId, int page, int size);

    class CommentListResult {
        private List<CommentDTO> records;
        private long total;
        private int size;
        private int current;

        public CommentListResult() {}

        public CommentListResult(List<CommentDTO> records, long total, int size, int current) {
            this.records = records;
            this.total = total;
            this.size = size;
            this.current = current;
        }

        public List<CommentDTO> getRecords() { return records; }
        public void setRecords(List<CommentDTO> records) { this.records = records; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public int getCurrent() { return current; }
        public void setCurrent(int current) { this.current = current; }
    }
}
