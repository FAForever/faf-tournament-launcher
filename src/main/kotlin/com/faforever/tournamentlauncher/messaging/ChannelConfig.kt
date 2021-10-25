package com.faforever.tournamentlauncher.messaging

import com.faforever.tournamentlauncher.domain.MatchService
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
    @Bean
    fun createGameRequestSink(streamBridge: StreamBridge): (MatchCreateRequest) -> Unit =
        { createGameRequest ->
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
        launchService: MatchService,
    ): (Message<MatchCreateSuccess>) -> Unit = { successMessage ->
        val requestId = UUID.fromString(successMessage.headers[CORRELATION_ID] as String)
        launchService.reportSuccess(requestId, successMessage.payload.gameId)
    }

    @Bean
    fun createGameFailed(
        launchService: MatchService,
    ): (Message<MatchCreateError>) -> Unit = { errorMessage ->
        val requestId = UUID.fromString(errorMessage.headers[CORRELATION_ID] as String)
        launchService.reportError(requestId, errorMessage.payload.errorCode)
    }

    @Bean
    fun gameResult(
        launchService: MatchService,
    ): (Message<MatchResult>) -> Unit = { resultMessage ->
    }
}
