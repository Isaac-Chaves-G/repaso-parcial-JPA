package edu.co.icesi.introspringboot.repository;

import edu.co.icesi.introspringboot.entity.Club;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubRepository extends CrudRepository<Club, Integer> {

    /**

     */
    List<Club> findDistinctByPlayerClubs_Player_Country_HomeMatches_IdOrPlayerClubs_Player_Country_AwayMatches_Id(Integer matchId, Integer matchIdAgain);
}
