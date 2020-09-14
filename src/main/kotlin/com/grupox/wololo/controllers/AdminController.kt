package com.grupox.wololo.controllers

import arrow.core.extensions.list.functorFilter.filter
import arrow.core.getOrHandle
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.GameStats
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.UserStats
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import io.swagger.annotations.ApiOperation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.util.*

@RequestMapping("/admin")
@RestController
class AdminController : BaseController() {
    @GetMapping("/games")
    @ApiOperation(value = "Gets the games stats")
    fun getGamesStats(@RequestParam("from", required = false) from: Date,
                      @RequestParam("to", required = false) to: Date,
                      @ApiIgnore @CookieValue("X-Auth") authCookie : String?): GameStats {

        val games: List<Game> = RepoGames.getAll().filter { it.date >= from && it.date <= to  }

        fun numberOfGames(status : String) : Int {
            return games.map { it.status }.filter { it.toString() == status }.count()
        }

        checkAndGetToken(authCookie)

        return GameStats(numberOfGames("NEW"), numberOfGames("ONGOING"), numberOfGames("FINISHED"), numberOfGames("CANCELED"), games)

    }

    @GetMapping("/scoreboard")
    @ApiOperation(value = "Gets the scoreboard")
    fun getScoreBoard(@ApiIgnore @CookieValue("X-Auth") authCookie : String?): List<UserStats> {
        checkAndGetToken(authCookie)
        return RepoUsers.getUsersStats()
     //   TODO("devolver el scoreboard")
    }

    @GetMapping("/users")
    @ApiOperation(value = "Gets the users stats")
    fun getUsersStats(@RequestParam("username", required = false) username: String?,
                      @ApiIgnore @CookieValue("X-Auth") authCookie : String?): List<User> {
        checkAndGetToken(authCookie)
        // Seguramente no devuelva una lista de users sino algo como lista de UserStats (a confirmar)

        TODO("proveer estadísticas de los usuarios permitiendo seleccionar un usuario particular")
    }
}