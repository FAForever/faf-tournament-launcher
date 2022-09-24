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
    val featuredMod: String,
    val gameOptions: Map<String, String>,
    val participants: List<MatchParticipant>
)
data class MatchError(
    val code: String,
    val playerIdsCausingError: List<String>?
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

    private val erroredGames: MutableMap<UUID, MatchError> = ConcurrentHashMap() // TODO: Clean up after time
    private val pendingGames: MutableMap<UUID, Match> = ConcurrentHashMap()
    private val runningGames: MutableMap<UUID, Int> = ConcurrentHashMap()

    fun getPendingGames() = pendingGames.toMap()
    fun getRunningGames() = runningGames.toMap()
    fun getErroredGames() = erroredGames.toMap()

    fun initiateGame(match: Match): UUID {
        val matchId = UUID.randomUUID()

        val matchCreateRequest = MatchCreateRequest(
            requestId = matchId,
            gameName = match.name,
            mapName = match.mapName,
            featuredMod = match.featuredMod,
            gameOptions = match.gameOptions,
            participants = match.participants.map { it.toLobbyDtoMatchParticipant() }
        )

        logger.debug { "Requesting match create: $matchCreateRequest" }

        pendingGames[matchId] = match
        createGameRequestSink(matchCreateRequest)
        return matchId
    }

    fun reportSuccess(requestId: UUID, gameId: Int) {
        val game = pendingGames.remove(requestId)

        if (game == null) {
            logger.debug { "Game $gameId is unknown, silently ignoring" }
        } else {
            logger.debug { "Game $gameId created successfully for request id $requestId" }
            runningGames[requestId] = gameId
        }
    }

    fun reportError(requestId: UUID, errorCode: String, playerIdsCausingCancel: List<String>?) {
        erroredGames[requestId] = MatchError(errorCode, playerIdsCausingCancel)
        val pendingGame = pendingGames.remove(requestId)

        if (pendingGame == null) {
            logger.debug { "Request id $requestId is unknown, silently ignoring" }
        } else {
            logger.info { "Creating game for request id $requestId failed (error code $errorCode)" }
        }
    }

    fun reportMatchResult(matchResult: MatchResult) {
        val entries = runningGames.filter { it.value == matchResult.gameId }.entries
        if (entries.isEmpty()) {
            logger.debug { "Game id ${matchResult.gameId} is unknown, silently ignoring" }
            return
        }
        val runningGame = runningGames.remove(
            entries.first().key
        )

        if (runningGame == null) {
            logger.debug { "Game id ${matchResult.gameId} is unknown, silently ignoring" }
        } else {
            logger.debug { "Receive game results for game id ${matchResult.gameId}: $matchResult" }
        }
    }
}
