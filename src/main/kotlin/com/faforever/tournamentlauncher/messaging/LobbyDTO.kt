package com.faforever.tournamentlauncher.messaging

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID
import com.faforever.tournamentlauncher.domain.Faction as DomainFaction
import com.faforever.tournamentlauncher.domain.MatchParticipant as DomainMatchParticipant

// TODO: Move this to faf-java-commons

enum class Faction {
    @JsonProperty("uef")
    UEF,
    @JsonProperty("cybran")
    CYBRAN,
    @JsonProperty("aeon")
    AEON,
    @JsonProperty("seraphim")
    SERAPHIM;

    fun toDomainFaction() = when (this) {
        UEF -> DomainFaction.UEF
        CYBRAN -> DomainFaction.CYBRAN
        AEON -> DomainFaction.AEON
        SERAPHIM -> DomainFaction.SERAPHIM
    }
}

fun DomainFaction.toLobbyDTOFaction() = when (this) {
    DomainFaction.UEF -> Faction.UEF
    DomainFaction.CYBRAN -> Faction.CYBRAN
    DomainFaction.AEON -> Faction.AEON
    DomainFaction.SERAPHIM -> Faction.SERAPHIM
}

data class MatchParticipant(
    @JsonProperty("player_id")
    val playerId: Int,
    val team: Int,
    val slot: Int,
    val faction: Faction
)

fun DomainMatchParticipant.toLobbyDtoMatchParticipant() = MatchParticipant(
    playerId,
    team,
    slot,
    faction.toLobbyDTOFaction()
)

data class MatchCreateRequest(
    @JsonIgnore
    val requestId: UUID,
    @JsonProperty("game_name")
    val gameName: String,
    @JsonProperty("map_name")
    val mapName: String,
    @JsonProperty("matchmaker_queue")
    val matchmakerQueue: String,
    val participants: List<MatchParticipant>,
)

data class MatchCreateSuccess(
    @JsonProperty("game_id")
    val gameId: Int,
)

data class MatchCreateError(
    @JsonProperty("error_code")
    val errorCode: Int,
    val args: Any,
)

enum class ArmyOutcome {
    VICTORY,
    DEFEAT,
    DRAW,
    UNKNOWN,
    CONFLICTING
}

data class ArmyResult(
    @JsonProperty("player_id")
    val playerId: Int,
    val army: Int,
    @JsonProperty("army_outcome")
    val armyOutcome: ArmyOutcome,
    val metadata: List<Any>,

)

enum class GameOutcome {
    VICTORY,
    DEFEAT,
    DRAW,
    UNKNOWN,
}

data class TeamResult(
    val outcome: GameOutcome,
    @JsonProperty("player_ids")
    val playerIds: List<Int>,
    @JsonProperty("army_results")
    val armyResults: List<ArmyResult>
)

data class MatchResult(
    @JsonProperty("game_id")
    val gameId: Int,
    @JsonProperty("rating_type")
    val ratingType: String,
    @JsonProperty("map_id")
    val mapId: Int,
    @JsonProperty("featured_mod")
    val featuredMod: String, // TODO: Enum
    @JsonProperty("sim_mod_ids")
    val simModIds: List<String>,
    @JsonProperty("commander_kills")
    val commanderKills: Map<String, Int>,
    val validity: String, // TODO: Enum
    val teams: List<TeamResult>
)
