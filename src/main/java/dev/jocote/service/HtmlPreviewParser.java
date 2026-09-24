package dev.jocote.service;

import dev.jocote.model.ResponsePreview.HtmlSpan;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Extracts inert text and a small fixed set of styles; all URLs and attributes are discarded. */
final class HtmlPreviewParser {
    private static final Set<String> HIDDEN = Set.of("head", "script", "style", "iframe", "object", "applet");
    private static final Set<String> BLOCKS = Set.of("p", "div", "section", "article", "blockquote", "pre", "tr", "ul", "ol");

    private HtmlPreviewParser() { }

    static List<HtmlSpan> parse(String source) throws IOException {
        var spans = new ArrayList<HtmlSpan>();
        new ParserDelegator().parse(new StringReader(source), new HTMLEditorKit.ParserCallback() {
            private final List<String> hidden = new ArrayList<>();
            private int bold, italic, code, heading;

            private void append(String text) {
                if (!hidden.isEmpty() || text.isEmpty()) return;
                if (Thread.currentThread().isInterrupted()) throw new IllegalArgumentException("Vista cancelada.");
                if (spans.size() >= 2_000) throw new IllegalArgumentException("HTML demasiado complejo: máximo 2 000 fragmentos visibles.");
                spans.add(new HtmlSpan(text, bold > 0, italic > 0, code > 0, heading));
            }

            @Override public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
                String name = tag.toString();
                if (HIDDEN.contains(name)) { hidden.add(name); return; }
                if (!hidden.isEmpty()) return;
                if (BLOCKS.contains(name) || name.matches("h[1-6]")) append("\n");
                if (name.equals("li")) append("\n• ");
                if (name.equals("b") || name.equals("strong")) bold++;
                if (name.equals("i") || name.equals("em")) italic++;
                if (name.equals("code") || name.equals("pre")) code++;
                if (name.matches("h[1-6]")) heading = name.charAt(1) - '0';
            }

            @Override public void handleEndTag(HTML.Tag tag, int position) {
                String name = tag.toString();
                if (!hidden.isEmpty()) { if (hidden.getLast().equals(name)) hidden.removeLast(); return; }
                if (name.equals("b") || name.equals("strong")) bold = Math.max(0, bold - 1);
                if (name.equals("i") || name.equals("em")) italic = Math.max(0, italic - 1);
                if (name.equals("code") || name.equals("pre")) code = Math.max(0, code - 1);
                if (name.matches("h[1-6]")) heading = 0;
                if (BLOCKS.contains(name) || name.matches("h[1-6]")) append("\n");
                if (name.equals("td") || name.equals("th")) append("  |  ");
            }

            @Override public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
                if (tag == HTML.Tag.BR || tag == HTML.Tag.HR) append("\n");
                if (tag == HTML.Tag.IMG) append("[Imagen externa omitida]");
            }

            @Override public void handleText(char[] data, int position) { append(new String(data)); }
        }, true);
        return List.copyOf(spans);
    }
}
