package com.grupox.wololo.services

import arrow.core.extensions.list.functorFilter.filter
import arrow.core.getOrElse
import arrow.core.getOrHandle
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.RepoUsers.getNormalUsers
import com.grupox.wololo.model.helpers.GameStats
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.UserStats
import com.grupox.wololo.model.helpers.UserWithoutStats
import io.swagger.annotations.ApiOperation
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import springfox.documentation.annotations.ApiIgnore
import java.util.*

class AdminService {

    fun getScoreBoard(): List<UserStats> {
        return RepoUsers.getUsersStats()
    }

    fun getGamesStats(from: Date, to: Date): GameStats {
        val games: List<Game> = RepoGames.getGames().filter { it.date > from && it.date <to }

        fun numberOfGames(status : String) : Int {
            return games.map { it.status }.filter { it.toString() == status }.count()
        }

        return GameStats(numberOfGames("NEW"), numberOfGames("ONGOING"), numberOfGames("FINISHED"), numberOfGames("CANCELED"), games)

    }


    fun surrender(gameId: Int, participantsIds: List<Int> , userMail : String) : Int? {

        val userID : Int = RepoUsers.getUserByName(userMail).getOrElse {  throw CustomException.NotFoundException("User was not found")  }.id
        val game: Game = RepoGames.getGameById(gameId).getOrElse { throw CustomException.NotFoundException("Game was not found") }

        if ((participantsIds.size) <= 2) {
            RepoGames.changeGameStatus(gameId, Status.CANCELED)
            RepoUsers.updateUserGamesWon(participantsIds.find { it != userID }.toOption().getOrElse {throw CustomException.NotFoundException("Not enough participants from game")})
        }

        RepoUsers.updateUserGamesLost(userID)
//lo cambio por option
        return getNormalUsers().find { it.mail == userMail }?.stats?.gamesLost
    }

    fun changeSpecialization(specialization: String, gameId: Int, townId: Int) {

        if (specialization == "PRODUCTION"){
            RepoGames.changeGameTownSpecialization(gameId,townId, Production())
        } else{
            if (specialization == "DEFENSE"){
                RepoGames.changeGameTownSpecialization(gameId,townId, Defense())
            }
        }
    }

}