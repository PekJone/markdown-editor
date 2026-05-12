package com.markdown.editor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.markdown.editor.mapper")
public class MarkdownEditorApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarkdownEditorApplication.class, args);
    }
}

