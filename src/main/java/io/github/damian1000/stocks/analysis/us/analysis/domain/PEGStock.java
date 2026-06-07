package io.github.damian1000.stocks.analysis.us.analysis.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Builder
@ToString
public class PEGStock implements Comparable<PEGStock> {

    private String zacksCode;
    private BigDecimal thisYearEstimatePE;
    private BigDecimal nextYearEstimatePE;
    private BigDecimal thisYearEPSGrowth;
    private BigDecimal nextYearEPSGrowth;
    private BigDecimal thisYearPEG;
    private BigDecimal nextYearPEG;
    private String category;

    @Override
    public int compareTo(PEGStock o) {
        int result = category.compareTo(o.category);
        if (result == 0) {
            result = nextYearPEG.compareTo(o.nextYearPEG);
        }
        return result;
    }
}
