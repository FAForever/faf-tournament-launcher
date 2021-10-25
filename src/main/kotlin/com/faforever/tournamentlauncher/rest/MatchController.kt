package com.faforever.tournamentlauncher.rest

import com.faforever.tournamentlauncher.domain.MatchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class MatchController(private val launchService: MatchService) {
    @PostMapping("/createMatch")
    fun createMatch(
        @RequestBody match: Match
    ) {
        launchService.initiateGame(match.toDomainMatch())
    }

    @GetMapping("/listMatches")
    fun listMatches() = mapOf(
        "pendingGames" to launchService.getPendingGames(),
        "runningGames" to launchService.getRunningGames(),
    )
}
