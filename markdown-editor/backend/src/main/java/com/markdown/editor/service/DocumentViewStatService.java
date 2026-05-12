package com.markdown.editor.service;

public interface DocumentViewStatService {

    void recordView(Long documentId, Long userId);
}
