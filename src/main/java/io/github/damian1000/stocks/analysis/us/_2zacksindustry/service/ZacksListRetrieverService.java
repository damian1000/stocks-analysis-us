package io.github.damian1000.stocks.analysis.us._2zacksindustry.service;

import io.github.damian1000.stocks.exception.DataRetrievalError;
import io.github.damian1000.stocks.html.HtmlRetriever;
import io.github.damian1000.stocks.analysis.us._2zacksindustry.domain.ZacksList;
import io.github.damian1000.stocks.analysis.us._2zacksindustry.event.ZacksListCompleteEvent;
import io.github.damian1000.stocks.analysis.us._2zacksindustry.event.ZacksListStartEvent;
import io.github.damian1000.stocks.analysis.us._2zacksindustry.repository.ZacksListRepository;
import io.github.damian1000.stocks.util.IdGenerator;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
@Component
@Slf4j
public class ZacksListRetrieverService {

    private final HtmlRetriever htmlRetriever;
    private final ZacksListRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void onZacksListStartEvent(ZacksListStartEvent event) {
        log.info("Zacks Industry start");
        log.info("Zacks Industry deleteByDate {}", event.getDate());
        repository.deleteByDate(event.getDate());
        try {
            log.info("Zacks Industry retrieveIndustries");
            retrieveIndustries(event.getDate()).forEach(repository::save);
            log.info("Zacks Industry retrieveIndustries complete");
        } catch (DataRetrievalError dataRetrievalError) {
            log.error("An error occurred while retrieving Zacks industries", dataRetrievalError);
            throw new IllegalStateException("Unable to retrieve Zacks industries", dataRetrievalError);
        }
        log.info("Completed Persisting Zacks Industry data");
        eventPublisher.publishEvent(new ZacksListCompleteEvent(event.getDate()));
    }

    private List<ZacksList> retrieveIndustries(LocalDate date) throws DataRetrievalError {
        List<ZacksList> zacksIndustryList = new ArrayList<>();

        String url = "https://www.zacks.com/data_handler/industry/z2_industry_data.php?p=0&t=1";
        String industries = htmlRetriever.getHtml(url).parsedHtml;
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
                        ZacksList zacksIndustry = new ZacksList();
                        zacksIndustry.setDate(date);
                        zacksIndustry.setId(IdGenerator.generateId());
                        zacksIndustryList.add(zacksIndustry);
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String dataName = reader.nextName();
                            JsonToken token = reader.peek();
                            if (token == JsonToken.BEGIN_ARRAY) {
                                reader.skipValue();
                            } else if (token == JsonToken.BEGIN_OBJECT) {
                                reader.skipValue();
                            } else {
                                if (dataName.equalsIgnoreCase("industry_name")) {
                                    String value = reader.nextString();
                                    int startIndex = value.indexOf(">") + 1;
                                    int endIndex = value.indexOf("</a>");
                                    zacksIndustry.setIndustry(value.substring(startIndex, endIndex));
                                } else if (dataName.equalsIgnoreCase("industry_id")) {
                                    zacksIndustry.setIndex(reader.nextString());
                                } else if (dataName.equalsIgnoreCase("no_of_stocks")) {
                                    zacksIndustry.setTotal(reader.nextString());
                                } else {
                                    reader.skipValue();
                                }
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
        zacksIndustryList.sort(Comparator.comparingInt(o -> Integer.valueOf(o.getIndex())));
        return zacksIndustryList;
    }

}
