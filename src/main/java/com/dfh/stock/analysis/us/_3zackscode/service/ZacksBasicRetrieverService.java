package com.dfh.stock.analysis.us._3zackscode.service;

import com.dfh.exception.DataRetrievalError;
import com.dfh.html.HtmlParser;
import com.dfh.html.HtmlRetriever;
import com.dfh.stock.analysis.us._2zacksindustry.domain.ZacksList;
import com.dfh.stock.analysis.us._2zacksindustry.repository.ZacksListRepository;
import com.dfh.stock.analysis.us._3zackscode.domain.ZacksCode;
import com.dfh.stock.analysis.us._3zackscode.event.ZacksBasicCompleteEvent;
import com.dfh.stock.analysis.us._3zackscode.event.ZacksBasicStartEvent;
import com.dfh.stock.analysis.us._3zackscode.repository.ZacksBasicRepository;
import com.dfh.util.IdGenerator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@AllArgsConstructor
public class ZacksBasicRetrieverService {

    private final HtmlRetriever htmlRetriever;
    private final HtmlParser htmlParser;
    private final ZacksListRepository zacksIndustryRepository;
    private final ZacksBasicRepository zacksBasicRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void onZacksBasicStartEvent(ZacksBasicStartEvent event) {
        log.info("Zacks Basic deleteByDate {}", event.getDate());
        zacksBasicRepository.deleteByDate(event.getDate());

        Set<ZacksList> zacksIndustries = zacksIndustryRepository.findByDate(event.getDate());
        log.info("Number of zacks industries loaded {} for date {}", zacksIndustries.size(), event.getDate());
        AtomicInteger count = new AtomicInteger(0);

        zacksIndustries.forEach(industry ->  {
            try {
                List<ZacksCode> zacksCodeList = retrieveIndustryDetail(industry, event.getDate());
                zacksBasicRepository.saveAll(zacksCodeList);
                count.addAndGet(zacksCodeList.size());
            } catch(DataRetrievalError e) {
                throw new IllegalStateException("Unable to retrieve Zacks basic details for " + industry.getIndustry(), e);
            }
        });
        log.info("Completed retrieving {} basic details from zacks", count.get());
        eventPublisher.publishEvent(new ZacksBasicCompleteEvent(event.getDate()));
    }

    private List<ZacksCode> retrieveIndustryDetail(ZacksList zacksIndustry, LocalDate date) throws DataRetrievalError {
        List<ZacksCode> zacksIndustryBasicList = new ArrayList<>();
        String index = zacksIndustry.getIndex();
        String url = "https://www.zacks.com/zrank/zacks_industry_rank_data_handler.php?i=";
        log.info("Retrieving: "+zacksIndustry);
        String details = htmlRetriever.getHtml(url+index).parsedHtml;
        String data = htmlParser.extractRow(details, "\"data\"  : [");
        data = data.replace("\\", "");

        // https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
        String pattern = "\\s+\"Company\"\\s+:\\s+\"(.*?)\".*?rel=\"(.*?)\"";

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(data);

        while (m.find()) {
            String company = m.group(1);
            String symbol = m.group(2);
            ZacksCode basic = new ZacksCode();
            basic.setDate(date);
            basic.setId(IdGenerator.generateId());
            basic.setIndustry(zacksIndustry.getIndustry());
            basic.setCompany(WordUtils.capitalizeFully(company));
            basic.setZacksCode(symbol);
            zacksIndustryBasicList.add(basic);
        }
        log.info("Number of details for industry: "+zacksIndustry.getIndustry() +" is "+zacksIndustryBasicList.size());
        return zacksIndustryBasicList;
    }

}
