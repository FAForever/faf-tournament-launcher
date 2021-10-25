package com.faforever.tournamentlauncher.domain

data class MatchResult(
    val gameId: Int,
    val commanderKills: Map<String, Int>,
    val validity: String, // TODO: Enum
    val teams: List<TeamResult>
)

data class TeamResult(
    val outcome: GameOutcome,
    val playerIds: List<Int>,
    val armyResults: List<ArmyResult>
)

data class ArmyResult(
    val playerId: Int,
    val armyOutcome: ArmyOutcome,
)
