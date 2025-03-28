package com.faforever.tournamentlauncher.rest

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import com.faforever.tournamentlauncher.domain.Faction as DomainFaction
import com.faforever.tournamentlauncher.domain.Match as DomainMatch
import com.faforever.tournamentlauncher.domain.MatchParticipant as DomainMatchParticipant

enum class Faction {
    UEF,
    CYBRAN,
    AEON,
    SERAPHIM,
    ;

    fun toDomainFaction() =
        when (this) {
            UEF -> DomainFaction.UEF
            CYBRAN -> DomainFaction.CYBRAN
            AEON -> DomainFaction.AEON
            SERAPHIM -> DomainFaction.SERAPHIM
        }
}

fun DomainFaction.toRestDTOFaction() =
    when (this) {
        DomainFaction.UEF -> Faction.UEF
        DomainFaction.CYBRAN -> Faction.CYBRAN
        DomainFaction.AEON -> Faction.AEON
        DomainFaction.SERAPHIM -> Faction.SERAPHIM
    }

data class Match(
    val name: String,
    val mapName: String,
    val featuredMod: String,
    val gameOptions: Map<String, String>,
    @field:NotEmpty val participants: List<MatchParticipant>,
) {
    fun toDomainMatch() =
        DomainMatch(
            name,
            mapName,
            featuredMod,
            gameOptions,
            participants.map { it.toDomainMatchParticipant() },
        )
}

data class MatchParticipant(
    @field:Min(1) val playerId: Int,
    val team: Int,
    @field:Min(1) val slot: Int,
    val faction: DomainFaction,
) {
    fun toDomainMatchParticipant() =
        DomainMatchParticipant(
            playerId,
            team,
            slot,
            faction,
        )
}
