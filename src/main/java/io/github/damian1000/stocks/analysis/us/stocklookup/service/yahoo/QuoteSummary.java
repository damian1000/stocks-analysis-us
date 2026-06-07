package io.github.damian1000.stocks.analysis.us.stocklookup.service.yahoo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class QuoteSummary {
    QuoteSummaryStore QuoteSummaryStore;
}

@Data
class QuoteSummaryStore {
    RecommendationTrends recommendationTrend;
    Price price;
    SummaryDetail summaryDetail;
    FinancialData financialData;
    EarningsTrends earningsTrend;
    EarningsHistory earningsHistory;

}

@Data
class EarningsHistory {
    List<History> history;
}

@Data
class History {
    Raw epsDifference;
}

@Data
class EarningsTrends {
    List<EarningTrend> trend;
}

@Data
class EarningTrend {
    Long maxAge;
    String period;
    String endDate;
    EarningsEstimate earningsEstimate;
}

@Data
class EarningsEstimate {
    Raw avg;
    Raw yearAgoEps;
}

@Data
class Raw {
    BigDecimal raw;
}

@Data
class FinancialData {
    Raw targetMeanPrice;
    String financialCurrency;
    Raw recommendationMean;
}

@Data
class SummaryDetail {
    Raw previousClose;
    Raw beta;
    String currency;
    Raw trailingPE;
}

@Data
class Price {
    Raw marketCap;
    String currency;
    String longName;
}

@Data
class RecommendationTrends {
    private List<RecommendationTrend> trend;
}

@Data
class RecommendationTrend {
    String period;
    Long strongBuy;
    Long buy;
    Long hold;
    Long sell;
    Long strongSell;
}
