package io.github.damian1000.stocks.analysis.us.export.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailExportTest {

    @Test
    void disabledExportIsNoOp() {
        // No SMTP needed; the export just logs and returns.
        EmailExport export = new EmailExport();
        ReflectionTestUtils.setField(export, "enabled", false);
        export.emailExport(LocalDate.now(), "name", "name.xls", "/tmp/name.xls");
    }

    @Test
    void enabledButIncompleteConfigThrows() {
        EmailExport export = new EmailExport();
        ReflectionTestUtils.setField(export, "enabled", true);
        // host/username/password/from/to all blank — must reject.
        ReflectionTestUtils.setField(export, "host", "");
        ReflectionTestUtils.setField(export, "username", "");
        ReflectionTestUtils.setField(export, "password", "");
        ReflectionTestUtils.setField(export, "from", "");
        ReflectionTestUtils.setField(export, "to", "");

        assertThrows(IllegalStateException.class,
                () -> export.emailExport(LocalDate.now(), "name", "name.xls", "/tmp/name.xls"));
    }

    @Test
    void enabledWithMissingSingleFieldStillThrows() {
        EmailExport export = new EmailExport();
        ReflectionTestUtils.setField(export, "enabled", true);
        ReflectionTestUtils.setField(export, "host", "smtp.example.com");
        ReflectionTestUtils.setField(export, "username", "user");
        ReflectionTestUtils.setField(export, "password", ""); // missing
        ReflectionTestUtils.setField(export, "from", "a@b.com");
        ReflectionTestUtils.setField(export, "to", "c@d.com");

        assertThrows(IllegalStateException.class,
                () -> export.emailExport(LocalDate.now(), "n", "n.xls", "/tmp/n.xls"));
    }
}
