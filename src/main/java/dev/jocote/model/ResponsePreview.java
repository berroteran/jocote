package dev.jocote.model;

import java.util.List;

/** Bounded, passive presentation data. Never contains executable HTML or external resources. */
public record ResponsePreview(Format format, String formattedText, ValueNode tree,
                              List<HtmlSpan> html, ImageInfo image, String notice) {
    public enum Format { JSON, XML, IMAGE, HTML, TEXT }

    public ResponsePreview { html = List.copyOf(html); }

    public record ValueNode(String name, String value, String type, List<ValueNode> children) {
        public ValueNode { children = List.copyOf(children); }
    }

    public record HtmlSpan(String text, boolean bold, boolean italic, boolean code, int heading) { }
    public record ImageInfo(int width, int height, String format) { }
    public record QueryResult(String text, int matches) { }
}
