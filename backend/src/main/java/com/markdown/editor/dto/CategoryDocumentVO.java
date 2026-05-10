package com.markdown.editor.dto;

import com.markdown.editor.entity.Document;

public class CategoryDocumentVO {
    
    private String category;
    private Document document;

    public CategoryDocumentVO() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
