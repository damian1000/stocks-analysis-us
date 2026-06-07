package io.github.damian1000.stocks.analysis.us.zackscode.repository;

import io.github.damian1000.stocks.analysis.us.zackscode.domain.ZacksCode;
import io.github.damian1000.stocks.analysis.us.zackscode.domain.ZacksCode_;
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
public interface ZacksBasicRepository extends JpaRepository<ZacksCode, String>, JpaSpecificationExecutor {

    @Transactional
    @Modifying
    @Query("delete from ZacksCode where date =:#{#date}")
    void deleteByDate(@Param("date") LocalDate date);

    Set<ZacksCode> findByDate(LocalDate date);

    class ZacksSpecs {
        public static Specification<ZacksCode> matchDate(LocalDate date) {
            return (Specification<ZacksCode>) (root, query, builder) ->
                    date != null ?
                            builder.equal(builder.lower(root.get(ZacksCode_.date)), date) :
                            builder.conjunction();
        }
    }
}
