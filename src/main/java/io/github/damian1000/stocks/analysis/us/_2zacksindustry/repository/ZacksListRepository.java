package io.github.damian1000.stocks.analysis.us._2zacksindustry.repository;

import io.github.damian1000.stocks.analysis.us._2zacksindustry.domain.ZacksList;
import io.github.damian1000.stocks.analysis.us._2zacksindustry.domain.ZacksList_;
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
public interface ZacksListRepository extends JpaRepository<ZacksList, String>, JpaSpecificationExecutor {

    @Transactional
    @Modifying
    @Query("delete from ZacksList where date =:#{#date}")
    void deleteByDate(@Param("date") LocalDate date);

    Set<ZacksList> findByDate(LocalDate date);

    class ZacksSpecs {
        public static Specification<ZacksList> matchDate(LocalDate date) {
            return (Specification<ZacksList>) (root, query, builder) ->
                    date != null ?
                            builder.equal(builder.lower(root.get(ZacksList_.date)), date) :
                            builder.conjunction();
        }
    }
}
