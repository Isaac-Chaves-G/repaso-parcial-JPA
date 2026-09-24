package edu.co.icesi.introspringboot.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

// Estudio: JPA gestiona esta clase como entidad; @Table indica la tabla donde se guardan sus datos.
@Entity
@Table(name = "player_club")
public class PlayerClub {

    // Estudio: Este objeto contiene la PK completa: playerId + clubId + startDate, como indica el diagrama.
    @EmbeddedId
    private PlayerClubId id;

    // Estudio: Muchos registros de PlayerClub pueden compartir un mismo player; aquí se guarda un objeto.
    @ManyToOne
    // Estudio: El ID de esta relación corresponde a id.playerId; la misma columna es FK y parte de la PK.
    @MapsId("playerId")
    // Estudio: name es la columna SQL que guarda la FK; no es el nombre del atributo Java.
    @JoinColumn(name = "player_id")
    private Player player;

    // Estudio: Muchos registros de PlayerClub pueden compartir un mismo club; aquí se guarda un objeto.
    @ManyToOne
    // Estudio: El ID de esta relación corresponde a id.clubId; la misma columna es FK y parte de la PK.
    @MapsId("clubId")
    // Estudio: name es la columna SQL que guarda la FK; no es el nombre del atributo Java.
    @JoinColumn(name = "club_id")
    private Club club;


    // Estudio: Relaciona este atributo con la columna SQL end_date.
    @Column(name = "end_date")
    private LocalDate endDate;

    // Estudio: Constructor vacío: JPA puede crear el objeto y después cargar sus campos.
    public PlayerClub() {
    }

    // Estudio: Crea la etapa y construye su clave con los IDs del jugador y club, más la fecha de inicio.
    public PlayerClub(Player player, Club club,
                  LocalDate startDate, LocalDate endDate) {
    this.player = player;
    this.club = club;
    this.endDate = endDate;

    // Estudio: La fecha se guarda dentro de la clave; no necesita otro campo startDate en esta entidad.
    this.id = new PlayerClubId(
        player.getId(),
        club.getId(),
        startDate
    );
}

    

    // Estudio: Devuelve el objeto con los tres componentes de la clave.
    public PlayerClubId getId() {
        return id;
    }

    // Estudio: Asigna la clave completa a un objeto nuevo; no cambies la PK de un registro persistido.
    public void setId(PlayerClubId id) {
        this.id = id;
    }

    // Estudio: Devuelve el jugador relacionado.
    public Player getPlayer() {
        return player;
    }

    // Estudio: Asigna el jugador relacionado al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setPlayer(Player player) {
        this.player = player;
    }

    // Estudio: Devuelve el club relacionado.
    public Club getClub() {
        return club;
    }

    // Estudio: Asigna el club relacionado al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setClub(Club club) {
        this.club = club;
    }

    // Estudio: Lee la fecha desde id; si aún no existe la clave, devuelve null.
    public LocalDate getStartDate() {
    if (id == null) {
        return null;
    }
    return id.getStartDate();
}

    // Estudio: Asigna la fecha dentro de id; crea la clave si falta. No cambiar una PK ya persistida.
    public void setStartDate(LocalDate startDate) {
    if (id == null) {
        id = new PlayerClubId();
    }
    id.setStartDate(startDate);
}

    // Estudio: Devuelve la fecha de fin.
    public LocalDate getEndDate() {
        return endDate;
    }

    // Estudio: Asigna la fecha de fin al objeto; por sí solo no ejecuta un UPDATE inmediato.
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
