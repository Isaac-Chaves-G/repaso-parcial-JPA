package edu.co.icesi.introspringboot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.time.LocalDate;

// Estudio: Agrupa la clave (jugador, club, inicio). Sus columnas están en PLAYER_CLUB; no crea otra tabla.
@Embeddable
public class PlayerClubId implements Serializable {

    // Estudio: Relaciona este atributo con la columna SQL player_id.
    @Column(name = "player_id")
    private Integer playerId;

    // Estudio: Relaciona este atributo con la columna SQL club_id.
    @Column(name = "club_id")
    private Integer clubId;

    // Estudio: Relaciona este atributo con la columna SQL start_date.
    @Column(name = "start_date")
    private LocalDate startDate;

    // Estudio: Constructor vacío: JPA puede crear el objeto y después cargar sus campos.
    public PlayerClubId() {
    }

    // Estudio: Recibe las tres piezas que juntas identifican una etapa; this.campo es el atributo del objeto.
    public PlayerClubId(Integer playerId, Integer clubId,
                    LocalDate startDate) {
    this.playerId = playerId;
    this.clubId = clubId;
    this.startDate = startDate;
}

    // Estudio: Devuelve el ID del jugador dentro de la clave.
    public Integer getPlayerId() {
        return playerId;
    }

    // Estudio: Asigna el ID del jugador dentro de la clave al construir la clave; no modificar componentes de una PK ya persistida.
    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    // Estudio: Devuelve el ID del club dentro de la clave.
    public Integer getClubId() {
        return clubId;
    }

    // Estudio: Asigna el ID del club dentro de la clave al construir la clave; no modificar componentes de una PK ya persistida.
    public void setClubId(Integer clubId) {
        this.clubId = clubId;
    }

    // Estudio: Devuelve la fecha de inicio.
    public LocalDate getStartDate() {
    return startDate;
}

    // Estudio: Asigna la fecha de inicio al construir la clave; no modificar componentes de una PK ya persistida.
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    @Override
    // Estudio: Dos claves son iguales si coinciden jugador, club y fecha de inicio; && significa "y".
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerClubId that = (PlayerClubId) o;
        return Objects.equals(playerId, that.playerId)
    && Objects.equals(clubId, that.clubId)
    && Objects.equals(startDate, that.startDate);
    }

    @Override
    // Estudio: Código auxiliar para mapas y conjuntos; usar los componentes de la clave, incluida startDate.
    public int hashCode() {
        return Objects.hash(playerId, clubId);
    }
}
