package com.faforever.tournamentlauncher.messaging

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID
import com.faforever.tournamentlauncher.domain.ArmyOutcome as DomainArmyOutcome
import com.faforever.tournamentlauncher.domain.ArmyResult as DomainArmyResult
import com.faforever.tournamentlauncher.domain.Faction as DomainFaction
import com.faforever.tournamentlauncher.domain.GameOutcome as DomainGameOutcome
import com.faforever.tournamentlauncher.domain.MatchParticipant as DomainMatchParticipant
import com.faforever.tournamentlauncher.domain.MatchResult as DomainMatchResult
import com.faforever.tournamentlauncher.domain.TeamResult as DomainTeamResult

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
    @JsonProperty("request_id")
    val requestId: UUID,
    @JsonProperty("game_name")
    val gameName: String,
    @JsonProperty("map_name")
    val mapName: String,
    @JsonProperty("featured_mod")
    val featuredMod: String, // TODO:enum
    @JsonProperty("game_options")
    val gameOptions: Map<String, String>,
    val participants: List<MatchParticipant>,
)

data class MatchCreateSuccess(
    @JsonProperty("game_id")
    val gameId: Int,
)
enum class MatchCreateErrorCode {
    PLAYER_NOT_ONLINE,
    PLAYER_NOT_IDLE,
    PLAYER_NOT_CONFIRMING,
    PLAYER_NOT_STARTING,
    PLAYER_NOT_CONNECTING,
    OTHER
}
data class MatchCreateError(
    @JsonProperty("error_code")
    val errorCode: MatchCreateErrorCode,
    @JsonProperty("players_causing_cancel")
    val playerIdsCausingCancel: List<String>?,
)

enum class ArmyOutcome {
    VICTORY,
    DEFEAT,
    DRAW,
    UNKNOWN,
    CONFLICTING;

    fun toDomainArmyOutcome() = when (this) {
        VICTORY -> DomainArmyOutcome.VICTORY
        DEFEAT -> DomainArmyOutcome.DEFEAT
        DRAW -> DomainArmyOutcome.DRAW
        UNKNOWN -> DomainArmyOutcome.UNKNOWN
        CONFLICTING -> DomainArmyOutcome.CONFLICTING
    }
}

data class ArmyResult(
    @JsonProperty("player_id")
    val playerId: Int,
    val army: Int,
    @JsonProperty("army_outcome")
    val armyOutcome: ArmyOutcome,
    val metadata: List<Any>,
) {
    fun toDomainArmyResult() = DomainArmyResult(
        playerId,
        armyOutcome.toDomainArmyOutcome()
    )
}

enum class GameOutcome {
    VICTORY,
    DEFEAT,
    DRAW,
    UNKNOWN;

    fun toDomainGameOutcome() = when (this) {
        VICTORY -> DomainGameOutcome.VICTORY
        DEFEAT -> DomainGameOutcome.DEFEAT
        DRAW -> DomainGameOutcome.DRAW
        UNKNOWN -> DomainGameOutcome.UNKNOWN
    }
}

data class TeamResult(
    val outcome: GameOutcome,
    @JsonProperty("player_ids")
    val playerIds: List<Int>,
    @JsonProperty("army_results")
    val armyResults: List<ArmyResult>
) {
    fun toDomainTeamResult() = DomainTeamResult(
        outcome.toDomainGameOutcome(),
        playerIds,
        armyResults.map { it.toDomainArmyResult() }
    )
}

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
) {
    fun toDomainMatchResult() = DomainMatchResult(
        gameId,
        commanderKills,
        validity,
        teams.map { it.toDomainTeamResult() }
    )
}
