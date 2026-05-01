package com.markdown.editor.service;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class MarkdownService {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownService() {
        List<Extension> extensions = Arrays.asList(
            TablesExtension.create(),
            HeadingAnchorExtension.create()
        );
        
        this.parser = Parser.builder()
            .extensions(extensions)
            .build();
        
        this.renderer = HtmlRenderer.builder()
            .extensions(extensions)
            .build();
    }

    public String toHtml(String markdown) {
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
