package io.github.damian1000.stocks.analysis.us._6export.service;

import io.github.damian1000.stocks.analysis.us._5analysis.domain.AnalysisStock;
import lombok.extern.slf4j.Slf4j;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@Component
@Slf4j
public class ExcelExport {

    @Value("${stocks.analysis.us.template}")
    private String excelTemplate;

    public void generateExcel(List<AnalysisStock> stocks, String excelFileName) {
        log.info(String.format("Generating excel %s using template %s", excelFileName, excelTemplate));
        try {
            try (InputStream is = new ClassPathResource(excelTemplate).getInputStream()) {
                try (OutputStream os = new FileOutputStream(excelFileName)) {
                    Context context = new Context();
                    context.putVar("stocks", stocks);
                    log.info("Calling jxls to create excel worksheet {}", excelFileName);
                    JxlsHelper.getInstance().processTemplate(is, os, context);
                    log.info("Completed calling jxls to create excel worksheet {}", excelFileName);
                }
            }
        } catch(IOException e) {
            log.error("An exception has occurred while attempting to generate excel file", e);
        }
    }

}
