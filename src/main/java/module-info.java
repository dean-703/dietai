module com.courtney.dietai {
    requires javafx.controls;
    requires javafx.graphics;
    requires java.prefs;
    requires java.net.http;

    requires com.fasterxml.jackson.datatype.jsr310;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;

    requires org.apache.commons.csv;

    // Allow JavaFX to find your Application class at launch
    opens com.courtney.dietai to javafx.graphics;

    // TableView uses reflection to read getters
    opens com.courtney.dietai.model to javafx.base, com.fasterxml.jackson.databind;

    // Jackson needs reflective access for JSON (profile/targets/summary/etc.)
    opens com.courtney.dietai.profile to com.fasterxml.jackson.databind;
    opens com.courtney.dietai.analysis to com.fasterxml.jackson.databind;

    // Export the main package (optional but common)
    exports com.courtney.dietai;
}
