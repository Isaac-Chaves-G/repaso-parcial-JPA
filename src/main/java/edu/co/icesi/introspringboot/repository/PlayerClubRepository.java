package edu.co.icesi.introspringboot.repository;

import edu.co.icesi.introspringboot.entity.PlayerClub;
import edu.co.icesi.introspringboot.entity.PlayerClubId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerClubRepository extends JpaRepository<PlayerClub, PlayerClubId> {
}
