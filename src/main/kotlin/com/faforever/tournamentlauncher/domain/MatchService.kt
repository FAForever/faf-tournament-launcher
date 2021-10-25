package com.faforever.tournamentlauncher.domain

import com.faforever.tournamentlauncher.messaging.MatchCreateRequest
import com.faforever.tournamentlauncher.messaging.toLobbyDtoMatchParticipant
import mu.KLogging
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
    companion object : KLogging()

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

        logger.debug { "Requesting match create: $matchCreateRequest" }

        pendingGames[matchId] = match
        createGameRequestSink(matchCreateRequest)
    }

    fun reportSuccess(requestId: UUID, gameId: Int) {
        val game = pendingGames.remove(requestId)

        if (game == null) {
            // This could be a game requested by a different service
            logger.debug { "Game $gameId is unknown, silently ignoring" }
        } else {
            logger.debug { "Game $gameId created successfully for request id $requestId" }
            runningGames[gameId] = game
        }
    }

    fun reportError(requestId: UUID, errorCode: Int) {
        val pendingGame = pendingGames.remove(requestId)

        if (pendingGame == null) {
            // This could be a game requested by a different service
            logger.debug { "Request id $requestId is unknown, silently ignoring" }
        } else {
            logger.error { "Creating game for request id $requestId failed (error code $errorCode)" }
        }
    }

    fun reportMatchResult(matchResult: MatchResult) {
        val runningGame = runningGames.remove(matchResult.gameId)

        if (runningGame == null) {
            // This could be a game requested by a different service
            logger.debug { "Game id ${matchResult.gameId} is unknown, silently ignoring" }
        } else {
            logger.debug { "Receive game results for game id ${matchResult.gameId}: $matchResult" }
        }
    }
}
