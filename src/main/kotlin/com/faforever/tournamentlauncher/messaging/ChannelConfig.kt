package com.faforever.tournamentlauncher.messaging

import com.faforever.tournamentlauncher.domain.MatchService
import mu.KLogging
import org.springframework.amqp.support.AmqpHeaders.CORRELATION_ID
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import java.util.UUID

const val ROUTING_KEY_HEADER = "routingKey"
const val ROUTING_KEY_LAUNCH_GAME_REQUEST = "request.match.create"

@Component
class RabbitConfig {
    companion object : KLogging()

    @Bean
    fun createGameRequestSink(streamBridge: StreamBridge): (MatchCreateRequest) -> Unit =
        { createGameRequest ->
            logger.trace { "Sending MatchCreateRequest: $createGameRequest" }

            streamBridge.send(
                "createGameRequest-out-0",
                MessageBuilder.withPayload(createGameRequest)
                    .setHeader(ROUTING_KEY_HEADER, ROUTING_KEY_LAUNCH_GAME_REQUEST)
                    .setHeader(CORRELATION_ID, createGameRequest.requestId.toString())
                    .build()
            )
        }

    @Bean
    fun createGameSuccess(
        matchService: MatchService,
    ): (Message<MatchCreateSuccess>) -> Unit = { successMessage ->
        val requestId = UUID.fromString(successMessage.headers[CORRELATION_ID] as String)
        logger.trace { "Received MatchCreateSuccess (request id $requestId): ${successMessage.payload}" }

        matchService.reportSuccess(requestId, successMessage.payload.gameId)
    }

    @Bean
    fun createGameFailed(
        matchService: MatchService,
    ): (Message<MatchCreateError>) -> Unit = { errorMessage ->
        val requestId = UUID.fromString(errorMessage.headers[CORRELATION_ID] as String)
        logger.trace { "Received MatchCreateSuccess (request id $requestId): ${errorMessage.payload}" }

        matchService.reportError(requestId, errorMessage.payload.errorCode.name, errorMessage.payload.playerIdsCausingCancel)
    }

    @Bean
    fun gameResult(
        matchService: MatchService,
    ): (Message<MatchResult>) -> Unit = { resultMessage ->
        logger.trace { "Received MatchResult: ${resultMessage.payload}" }

        matchService.reportMatchResult(resultMessage.payload.toDomainMatchResult())
    }
}
