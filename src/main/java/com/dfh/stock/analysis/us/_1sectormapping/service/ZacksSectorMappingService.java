package com.dfh.stock.analysis.us._1sectormapping.service;

import com.dfh.exception.DataRetrievalError;
import com.dfh.html.HtmlRetriever;
import com.dfh.stock.analysis.us._1sectormapping.domain.ZacksSectorMapping;
import com.dfh.stock.analysis.us._1sectormapping.event.ZacksSectorMappingCompleteEvent;
import com.dfh.stock.analysis.us._1sectormapping.event.ZacksSectorMappingStartEvent;
import com.dfh.stock.analysis.us._1sectormapping.repository.ZacksSectorMappingRepository;
import com.dfh.util.IdGenerator;
import com.google.gson.stream.JsonReader;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@AllArgsConstructor
public class ZacksSectorMappingService {

    private final HtmlRetriever htmlRetriever;
    private final ZacksSectorMappingRepository zacksSectorMappingRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void onZacksSectorMappingStartEvent(ZacksSectorMappingStartEvent event) {
        log.info("Zacks Sector Mapping deleteByDate {}", event.getDate());
        zacksSectorMappingRepository.deleteByDate(event.getDate());

        try {
            List<ZacksSectorMapping> sectorMapping = downloadSectorMapping(event.getDate());
            log.info("Completed retrieving {} sector mapping from zacks", sectorMapping.size());
            zacksSectorMappingRepository.saveAll(sectorMapping);
        } catch (DataRetrievalError dataRetrievalError) {
            log.error("An error occurred while downloading sector mapping", dataRetrievalError);
            throw new IllegalStateException("Unable to download Zacks sector mapping", dataRetrievalError);
        }

        eventPublisher.publishEvent(new ZacksSectorMappingCompleteEvent(event.getDate()));
    }

    private List<ZacksSectorMapping> downloadSectorMapping(LocalDate date) throws DataRetrievalError {
        List<ZacksSectorMapping> zacksSectorMappingList = new ArrayList<>();

        log.info("Downloading sector mapping...");
        String url = "https://www.zacks.com/zrank/sector-industry-classification.php";
        String industries = htmlRetriever.getHtml(url).rawHtml;
        log.info("Completed downloading sector mapping.");
        String startWord = "window.app_data =";
        int startIndex = industries.indexOf(startWord)+startWord.length();
        if (industries.length() > startIndex) {
            industries = industries.substring(startIndex).trim();
            JsonReader reader = new JsonReader(new StringReader(industries));
            reader.setLenient(true);
            try {
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("data")) {
                        // read array
                        reader.beginArray();
                        while (reader.hasNext()) {
                            reader.beginObject();
                            ZacksSectorMapping zacksSectorMapping = new ZacksSectorMapping();
                            zacksSectorMapping.setId(IdGenerator.generateId());
                            zacksSectorMapping.setDate(date);
                            zacksSectorMappingList.add(zacksSectorMapping);
                            while (reader.hasNext()) {
                                String dataName = reader.nextName();
                                if (dataName.equals("Sector Group")) {
                                    zacksSectorMapping.setSectorGroup(findText(reader.nextString()));
                                } else if (dataName.equals("Medium(M) Industry Group")) {
                                    zacksSectorMapping.setMediumIndustryGroup(findText(reader.nextString()));
                                } else if (dataName.equals("Expanded(X) Industry Group")) {
                                    zacksSectorMapping.setIndustry(findText(reader.nextString()));
                                } else {
                                    reader.skipValue(); //avoid some unhandle events
                                }
                            }
                            reader.endObject();
                        }
                        reader.endArray();
                    } else {
                        reader.skipValue(); //avoid some unhandle events
                    }
                }
                reader.endObject();
                reader.close();
            } catch (IOException e) {
                throw new DataRetrievalError(e);
            }

            // remove the header
            zacksSectorMappingList = zacksSectorMappingList.subList(1, zacksSectorMappingList.size());

            return zacksSectorMappingList;
        }
        return Collections.emptyList();
    }

    private String findText(String rawText) {
        // https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
        // http://stackoverflow.com/questions/16331423/whats-the-java-regular-expression-for-an-only-integer-numbers-string
        String pattern = "<span title=\"(.*?)\"";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(rawText);
        if (m.find()) {
            return m.group(1);
        }
        return "could not found anything for "+rawText;
    }
}
