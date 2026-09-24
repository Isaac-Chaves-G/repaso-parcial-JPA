package edu.co.icesi.introspringboot.controller;

import edu.co.icesi.introspringboot.entity.Club;
import edu.co.icesi.introspringboot.entity.Country;
import edu.co.icesi.introspringboot.entity.Match;
import edu.co.icesi.introspringboot.entity.Player;
import edu.co.icesi.introspringboot.repository.ClubRepository;
import edu.co.icesi.introspringboot.repository.CountryRepository;
import edu.co.icesi.introspringboot.repository.MatchRepository;
import edu.co.icesi.introspringboot.repository.PlayerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/worldcup")
public class WorldCupController {

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final ClubRepository clubRepository;
    private final CountryRepository countryRepository;


    public WorldCupController(PlayerRepository playerRepository,
                              MatchRepository matchRepository,
                              ClubRepository clubRepository,
                              CountryRepository countryRepository
                              ) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.clubRepository = clubRepository;
        this.countryRepository = countryRepository;
    }

    //Jugadores que han jigado en el real
    @GetMapping("/ej1")
    public List<Player> ej1(){
        throw  new UnsupportedOperationException("Not supported yet.");
    }


}
