Modelo corregido según el diagrama del parcial. PLAYER_CLUB se identifica con jugador + club + fecha de inicio; no tiene un id numérico independiente. MATCH se mapea en Java a la tabla match_game.

```mermaid
erDiagram

    MATCH {
        int id PK
        date match_date
        int home_country_id FK
        int away_country_id FK
        string stadium
    }

    COUNTRY {
        int id PK
        string name
        string code
        string confederation
    }

    PLAYER {
        int id PK
        string name
        date birth_date
        string position
        int fifaScore
        int country_id FK
    }

    CLUB {
        int id PK
        string name
        string city
        date founded
        int country_id FK
    }

    PLAYER_CLUB {
        int player_id PK, FK
        int club_id PK, FK
        date start_date PK
        date end_date
    }

    %% Relationships

    COUNTRY ||--o{ MATCH : "home team"
    COUNTRY ||--o{ MATCH : "away team"

    COUNTRY ||--o{ PLAYER : has
    COUNTRY ||--o{ CLUB : has

    PLAYER ||--o{ PLAYER_CLUB : history
    CLUB ||--o{ PLAYER_CLUB : employs
```
