package io.github.damian1000.stocks.analysis.us.analysis.repository;

import io.github.damian1000.stocks.analysis.us.analysis.domain.AnalysisStock;
import io.github.damian1000.stocks.analysis.us.analysis.domain.AnalysisStock_;
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
public interface AnalysisRepository extends JpaRepository<AnalysisStock, Long>, JpaSpecificationExecutor<AnalysisStock> {

    @Transactional
    @Modifying
    @Query("delete from AnalysisStock where date =:#{#date}")
    void deleteByDate(@Param("date") LocalDate date);

    Set<AnalysisStock> findByDate(LocalDate date);

    class ZacksSpecs {
        public static Specification<AnalysisStock> matchDate(LocalDate date) {
            return (Specification<AnalysisStock>) (root, query, builder) ->
                    date != null ?
                            builder.equal(builder.lower(root.get(AnalysisStock_.date)), date) :
                            builder.conjunction();
        }
    }
}
