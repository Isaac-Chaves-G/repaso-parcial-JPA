package edu.co.icesi.introspringboot.repository;

import edu.co.icesi.introspringboot.entity.Match;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends CrudRepository<Match, Integer> {

    /**
     Solo locales
     */
    List<Match> findDistinctByHomeCountry_Players_PlayerClubs_Club_NameAndHomeCountry_Players_PlayerClubs_EndDateIsNull(String clubName);
}
