package com.faforever.tournamentlauncher.rest

import com.faforever.tournamentlauncher.domain.Faction as DomainFaction
import com.faforever.tournamentlauncher.domain.Match as DomainMatch
import com.faforever.tournamentlauncher.domain.MatchParticipant as DomainMatchParticipant

enum class Faction {
    UEF,
    CYBRAN,
    AEON,
    SERAPHIM;

    fun toDomainFaction() = when (this) {
        UEF -> DomainFaction.UEF
        CYBRAN -> DomainFaction.CYBRAN
        AEON -> DomainFaction.AEON
        SERAPHIM -> DomainFaction.SERAPHIM
    }
}

fun DomainFaction.toRestDTOFaction() = when (this) {
    DomainFaction.UEF -> Faction.UEF
    DomainFaction.CYBRAN -> Faction.CYBRAN
    DomainFaction.AEON -> Faction.AEON
    DomainFaction.SERAPHIM -> Faction.SERAPHIM
}

data class Match(
    val name: String,
    val mapName: String,
    val matchmakerQueue: String,
    val participants: List<MatchParticipant>,
) {
    fun toDomainMatch() = DomainMatch(
        name,
        mapName,
        matchmakerQueue,
        participants.map { it.toDomainMatchParticipant() },
    )
}

data class MatchParticipant(
    val playerId: Int,
    val team: Int,
    val slot: Int,
    val faction: DomainFaction,
) {
    fun toDomainMatchParticipant() = DomainMatchParticipant(
        playerId,
        team,
        slot,
        faction,
    )
}
