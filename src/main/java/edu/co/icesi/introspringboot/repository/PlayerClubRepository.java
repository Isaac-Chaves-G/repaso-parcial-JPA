package edu.co.icesi.introspringboot.repository;

import edu.co.icesi.introspringboot.entity.PlayerClub;
import edu.co.icesi.introspringboot.entity.PlayerClubId;
import org.springframework.data.jpa.repository.JpaRepository;

// La entidad es PlayerClub y su identificador es un OBJETO PlayerClubId, no un Integer.
// Hereda save, findAll y findById(clave). Las consultas que devuelven jugadores van en PlayerRepository.
public interface PlayerClubRepository extends JpaRepository<PlayerClub, PlayerClubId> {
}
