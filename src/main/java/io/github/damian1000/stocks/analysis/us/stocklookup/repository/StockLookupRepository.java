package io.github.damian1000.stocks.analysis.us.stocklookup.repository;

import io.github.damian1000.stocks.analysis.us.stocklookup.domain.StockLookup;
import io.github.damian1000.stocks.analysis.us.stocklookup.domain.StockLookup_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Component
public interface StockLookupRepository extends JpaRepository<StockLookup, String>, JpaSpecificationExecutor {

    @Transactional
    @Modifying
    @Query("delete from StockLookup where date =:#{#date}")
    void deleteByDate(@Param("date") LocalDate date);

    Set<StockLookup> findByDate(LocalDate date);

    class ZacksSpecs {
        public static Specification<StockLookup> matchDate(LocalDate date) {
            return (Specification<StockLookup>) (root, query, builder) ->
                    date != null ?
                            builder.equal(builder.lower(root.get(StockLookup_.date)), date) :
                            builder.conjunction();
        }
    }
}
