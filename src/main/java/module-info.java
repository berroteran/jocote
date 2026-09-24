module dev.jocote {
    requires javafx.controls;
    requires java.net.http;
    requires java.logging;
    requires com.fasterxml.jackson.databind;

    exports dev.jocote to javafx.graphics;
    opens dev.jocote.model to com.fasterxml.jackson.databind;
}
