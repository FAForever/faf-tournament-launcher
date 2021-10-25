package com.faforever.tournamentlauncher.domain

import com.faforever.tournamentlauncher.messaging.MatchCreateRequest
import com.faforever.tournamentlauncher.messaging.toLobbyDtoMatchParticipant
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class Match(
    val name: String,
    val mapName: String,
    val matchmakerQueue: String,
    val participants: List<MatchParticipant>
)

data class MatchParticipant(
    val playerId: Int,
    val team: Int,
    val slot: Int,
    val faction: Faction
)

@Service
class MatchService(
    @Qualifier("createGameRequestSink")
    private val createGameRequestSink: (MatchCreateRequest) -> Unit
) {
    private val pendingGames: MutableMap<UUID, Match> = ConcurrentHashMap()
    private val runningGames: MutableMap<Int, Match> = ConcurrentHashMap()

    fun getPendingGames() = pendingGames.toMap()
    fun getRunningGames() = runningGames.toMap()

    fun initiateGame(match: Match) {
        val matchId = UUID.randomUUID()

        val matchCreateRequest = MatchCreateRequest(
            requestId = matchId,
            gameName = match.name,
            mapName = match.mapName,
            matchmakerQueue = match.matchmakerQueue,
            participants = match.participants.map { it.toLobbyDtoMatchParticipant() }
        )

        pendingGames[matchId] = match
        createGameRequestSink(matchCreateRequest)
    }

    fun reportSuccess(requestId: UUID, gameId: Int) {
        val game = pendingGames.remove(requestId) ?: throw IllegalStateException("Unknown request id: $requestId")
        runningGames[gameId] = game
    }

    fun reportError(requestId: UUID, errorCode: Int) {
        pendingGames.remove(requestId) ?: throw IllegalStateException("Unknown request id: $requestId")
    }

    fun reportMatchResult(matchResult: MatchResult) {
        runningGames.remove(matchResult.gameId) ?: throw IllegalStateException("Unknown game id: ${matchResult.gameId}")
    }
}
