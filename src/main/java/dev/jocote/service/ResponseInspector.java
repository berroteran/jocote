package dev.jocote.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jocote.model.ResponseData;
import dev.jocote.model.ResponsePreview;
import dev.jocote.model.ResponsePreview.Format;
import dev.jocote.model.ResponsePreview.ImageInfo;
import dev.jocote.model.ResponsePreview.QueryResult;
import dev.jocote.model.ResponsePreview.ValueNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathEvaluationResult;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathNodes;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** CPU work for passive response previews. Call outside the JavaFX thread. */
public final class ResponseInspector {
    public static final int TEXT_LIMIT = 300_000;
    private static final int NODE_LIMIT = 10_000;
    private static final ObjectMapper JSON = new ObjectMapper(JsonFactory.builder()
            .streamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(128).maxStringLength(TEXT_LIMIT).build())
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build())
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);

    public ResponsePreview inspect(ResponseData response) {
        String mediaType = response.contentType().split(";", 2)[0].strip().toLowerCase(Locale.ROOT);
        if (mediaType.startsWith("image/") && !mediaType.equals("image/svg+xml")) {
            try {
                return new ResponsePreview(Format.IMAGE, "", null, List.of(), imageInfo(response.body()),
                        "Vista estática; GIF muestra el primer fotograma. Guardar conserva el archivo original.");
            } catch (Exception e) { return plain("", "No se pudo previsualizar la imagen: formato inválido, no soportado o demasiado grande."); }
        }
        String text = response.text();
        if (text.length() > TEXT_LIMIT) return plain("", "Vista limitada a 300 000 caracteres. Guarda la respuesta para obtener el contenido completo.");
        String trimmed = text.stripLeading();
        try {
            if (mediaType.equals("text/html") || mediaType.equals("application/xhtml+xml")) {
                return new ResponsePreview(Format.HTML, text, null, HtmlPreviewParser.parse(text), null,
                        "HTML estático: sin JavaScript, CSS, navegación, formularios ni carga de recursos externos.");
            }
            if (mediaType.contains("json") || trimmed.startsWith("{") || trimmed.startsWith("[")) {
                JsonNode root = json(text);
                return new ResponsePreview(Format.JSON, writeJson(root), tree("$", root, new int[]{0}), List.of(), null, "");
            }
            if (mediaType.contains("xml") || trimmed.startsWith("<")) {
                Document document = xml(text);
                return new ResponsePreview(Format.XML, writeXml(document), null, List.of(), null,
                        "XML sin DTD ni entidades externas. Los cambios de formato solo afectan la vista.");
            }
        } catch (Exception e) {
            return plain(text, "No se generó la vista: documento inválido o límite de complejidad excedido. Se conserva el texto original.");
        }
        return plain(text, text.isEmpty() ? "Respuesta sin contenido." : "Sin vista estructurada para este contenido; consulta Body.");
    }

    public QueryResult query(Format format, String source, String expression) {
        if (source.length() > TEXT_LIMIT) throw new IllegalArgumentException("La consulta requiere una respuesta de hasta 300 000 caracteres.");
        if (expression == null || expression.isBlank() || expression.length() > 1_024) {
            throw new IllegalArgumentException("Escribe una consulta de 1 a 1 024 caracteres.");
        }
        try {
            if (format == Format.JSON) {
                JsonNode root = json(source);
                var matches = JsonPathQuery.evaluate(root, expression.strip());
                return new QueryResult(writeJson(matches), matches.size());
            }
            if (format != Format.XML) throw new IllegalArgumentException("Las consultas requieren JSON o XML válido.");
            Document document = xml(source);
            var factory = XPathFactory.newDefaultInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            var xpath = factory.newXPath();
            xpath.setNamespaceContext(new NamespaceContext() {
                @Override public String getNamespaceURI(String prefix) {
                    if (prefix == null) throw new IllegalArgumentException("Prefijo nulo.");
                    if (prefix.equals(XMLConstants.XML_NS_PREFIX)) return XMLConstants.XML_NS_URI;
                    if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
                    if (prefix.isEmpty()) return XMLConstants.NULL_NS_URI;
                    String uri = document.getDocumentElement().lookupNamespaceURI(prefix);
                    return uri == null ? XMLConstants.NULL_NS_URI : uri;
                }
                @Override public String getPrefix(String uri) { return document.getDocumentElement().lookupPrefix(uri); }
                @Override public Iterator<String> getPrefixes(String uri) {
                    String prefix = getPrefix(uri);
                    return prefix == null ? List.<String>of().iterator() : List.of(prefix).iterator();
                }
            });
            var result = xpath.compile(expression).evaluateExpression(document);
            var output = new BoundedWriter();
            int matches = 1;
            if (result.type() == XPathEvaluationResult.XPathResultType.NODESET) {
                var nodes = (XPathNodes) result.value(); matches = nodes.size();
                if (matches > 1_000) throw new IllegalArgumentException("La consulta supera 1 000 resultados; usa una ruta más específica.");
                for (Node node : nodes) {
                    output.write(node.getNodeType() == Node.ATTRIBUTE_NODE || node.getNodeType() == Node.TEXT_NODE
                            ? node.getNodeValue() : writeXml(node));
                    output.write("\n");
                }
            } else output.write(String.valueOf(result.value()));
            return new QueryResult(output.toString(), matches);
        } catch (IllegalArgumentException e) { throw e; }
        catch (Exception e) { throw new IllegalArgumentException("Consulta inválida, documento no admitido o resultado demasiado grande."); }
    }

    private static ResponsePreview plain(String text, String notice) {
        return new ResponsePreview(Format.TEXT, text, null, List.of(), null, notice);
    }

    private static JsonNode json(String text) throws IOException {
        JsonNode node = JSON.readTree(text);
        if (node == null || node.isMissingNode()) throw new IOException("JSON vacío.");
        return node;
    }

    private static String writeJson(Object value) throws IOException {
        var writer = new BoundedWriter();
        JSON.writerWithDefaultPrettyPrinter().writeValue(writer, value);
        return writer.toString();
    }

    private static ValueNode tree(String name, JsonNode node, int[] count) {
        if (++count[0] > NODE_LIMIT || Thread.currentThread().isInterrupted()) throw new IllegalArgumentException("Árbol demasiado grande.");
        var children = new ArrayList<ValueNode>();
        String type = node.isObject() ? "object" : node.isArray() ? "array" : node.isTextual() ? "string"
                : node.isNumber() ? "number" : node.isBoolean() ? "boolean" : "null";
        if (node.isObject()) node.fields().forEachRemaining(entry -> children.add(tree(entry.getKey(), entry.getValue(), count)));
        else if (node.isArray()) for (int i = 0; i < node.size(); i++) children.add(tree("[" + i + "]", node.get(i), count));
        String value = node.isContainerNode() ? (node.isArray() ? "[" : "{") + node.size() + (node.isArray() ? "]" : "}") : node.toString();
        return new ValueNode(name, value, type, children);
    }

    private static Document xml(String source) throws Exception {
        var factory = DocumentBuilderFactory.newDefaultNSInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setAttribute("http://www.oracle.com/xml/jaxp/properties/maxElementDepth", 128);
        factory.setXIncludeAware(false); factory.setExpandEntityReferences(false);
        var builder = factory.newDocumentBuilder();
        builder.setEntityResolver((publicId, systemId) -> { throw new SAXException("Recurso externo bloqueado."); });
        builder.setErrorHandler(new DefaultHandler() {
            @Override public void error(SAXParseException e) throws SAXException { throw e; }
            @Override public void fatalError(SAXParseException e) throws SAXException { throw e; }
        });
        Document document = builder.parse(new InputSource(new StringReader(source)));
        checkNodes(document, new int[]{0});
        return document;
    }

    private static void checkNodes(Node node, int[] count) {
        if (++count[0] > NODE_LIMIT || Thread.currentThread().isInterrupted()) throw new IllegalArgumentException("XML demasiado complejo.");
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) checkNodes(child, count);
    }

    private static String writeXml(Node node) throws Exception {
        var factory = TransformerFactory.newDefaultInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        var transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        var writer = new BoundedWriter();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }

    private static ImageInfo imageInfo(byte[] bytes) throws IOException {
        if (bytes.length > HttpRequestService.MAX_RESPONSE_BYTES) throw new IOException("Imagen demasiado grande.");
        try (var input = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException("Imagen no admitida.");
            var reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName().toUpperCase(Locale.ROOT);
                if (!Set.of("PNG", "JPEG", "JPG", "GIF", "BMP").contains(format)) throw new IOException("Formato no admitido.");
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > 8_192 || height > 8_192 || (long) width * height > 16_000_000) {
                    throw new IOException("Dimensiones demasiado grandes.");
                }
                return new ImageInfo(width, height, format);
            } finally { reader.dispose(); }
        }
    }

    private static final class BoundedWriter extends Writer {
        private final StringBuilder value = new StringBuilder();
        @Override public void write(char[] chars, int offset, int length) throws IOException {
            if ((long) value.length() + length > TEXT_LIMIT || Thread.currentThread().isInterrupted()) throw new IOException("Salida demasiado grande o cancelada.");
            value.append(chars, offset, length);
        }
        @Override public void flush() { }
        @Override public void close() { }
        @Override public String toString() { return value.toString(); }
    }
}
