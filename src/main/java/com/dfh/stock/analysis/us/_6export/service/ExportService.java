package com.dfh.stock.analysis.us._6export.service;

import com.dfh.stock.analysis.us._5analysis.domain.AnalysisStock;
import com.dfh.stock.analysis.us._5analysis.repository.AnalysisRepository;
import com.dfh.stock.analysis.us._6export.event.ExportCompleteEvent;
import com.dfh.stock.analysis.us._6export.event.ExportStartEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
@Slf4j
public class ExportService {

    private final AnalysisRepository analysisRepository;
    private final ExcelExport excelExport;
    private final EmailExport emailExport;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void onAnalysisServiceEvent(ExportStartEvent event) {
        log.info("Retrieving analysis stock for date {}", event.getDate());
        Set<AnalysisStock> analysisStockSet = analysisRepository.findByDate(event.getDate());

        if (!analysisStockSet.isEmpty()) {
            List<AnalysisStock> analysisStockList = new ArrayList<>(analysisStockSet);
            Collections.sort(analysisStockList);
            log.info("Retrieved a total of analysis stock {} for date {}", analysisStockList.size(), event.getDate());

            String name = event.getDate() + "-stock-analysis";
            String fileName =  name+".xls";
            String fullPath =  "./"+fileName;
            log.info("Writing {} rows to excel {}", analysisStockList.size(), fullPath);
            excelExport.generateExcel(analysisStockList, fullPath);
            log.info("Completed writing {} rows to excel {}", analysisStockList.size(), fullPath);

            emailExport.emailExport(event.getDate(), name, fileName, fullPath);
        } else {
            log.info("Not exporting to excel as analysis stock for date {} is 0", event.getDate());
        }

        eventPublisher.publishEvent(new ExportCompleteEvent(event.getDate()));
    }

}