package com.dfh.html;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HtmlParserTest {

    private HtmlParser htmlParser = new HtmlParser();

    @Test
    public void testExtractText() {
        String html = "{\"RYN\":{\"exchange\":\"Real Time Quote from BATS\",\"dividend_yield\":\"prev_close_date\":\"02\\/28\\/2018 15:57:17\",\"exchange\":\"NYSE\",\"shares\":\"\",\"volatility\"";
        String exchange = htmlParser.extractText(html, "exchange\":\"", "\",\"");
        assertEquals("Real Time Quote from BATS", exchange);
    }

    @Test
    public void testExtractTextWithStartText() {
        String html = "{\"RYN\":{\"exchange\":\"Real Time Quote from BATS\",\"dividend_yield\":\"prev_close_date\":\"02\\/28\\/2018 15:57:17\",\"exchange\":\"NYSE\",\"shares\":\"\",\"volatility\"";
        String exchange = htmlParser.extractText(html, "dividend_yield", "exchange\":\"", "\",\"");
        assertEquals("NYSE", exchange);
    }

}
