package com.dfh.stock.analysis.us._1sectormapping.repository;

import com.dfh.stock.analysis.us._1sectormapping.domain.ZacksSectorMapping;
import com.dfh.stock.analysis.us._1sectormapping.domain.ZacksSectorMapping_;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public interface ZacksSectorMappingRepository extends JpaRepository<ZacksSectorMapping, String>, JpaSpecificationExecutor {

    @Transactional
    @Modifying
    @Query("delete from ZacksSectorMapping where date =:#{#date}")
    void deleteByDate(@Param("date") LocalDate date);

    List<ZacksSectorMapping> findByDate(LocalDate date);

    class ZacksSpecs {
        public static Specification<ZacksSectorMapping> matchDate(LocalDate date) {
            return (Specification<ZacksSectorMapping>) (root, query, builder) ->
                    date != null ?
                            builder.equal(builder.lower(root.get(ZacksSectorMapping_.date)), date) :
                            builder.conjunction();
        }
    }
}
