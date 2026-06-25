package io.github.damian1000.stocks.analysis.us.export.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelExportTest {

    @Test
    void generatesWorkbookFromTemplate(@TempDir Path tmp) {
        ExcelExport export = new ExcelExport();
        ReflectionTestUtils.setField(export, "excelTemplate", "template/ZacksPriceDetailsTemplate.xls");
        File out = tmp.resolve("report.xls").toFile();

        export.generateExcel(List.of(), out.getAbsolutePath());

        assertTrue(out.exists(), "excel file should be created from the template");
        assertTrue(out.length() > 0, "excel file should not be empty");
    }

    @Test
    void missingTemplateIsLoggedNotThrown(@TempDir Path tmp) {
        ExcelExport export = new ExcelExport();
        ReflectionTestUtils.setField(export, "excelTemplate", "template/does-not-exist.xls");
        File out = tmp.resolve("report.xls").toFile();

        // The IOException is caught and logged; the method must not propagate it.
        export.generateExcel(List.of(), out.getAbsolutePath());

        assertFalse(out.exists(), "no file should be produced when the template is missing");
    }
}
