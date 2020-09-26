package com.grupox.wololo.services

import arrow.core.extensions.list.functorFilter.filter
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.GamePublicInfo
import com.grupox.wololo.model.helpers.UserPublicInfo
import com.grupox.wololo.model.helpers.getOrThrow
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import org.springframework.stereotype.Service
import java.util.*

@Service
class AdminControllerService {



    fun getScoreBoard(): List<UserPublicInfo> {
        return RepoUsers.getUsersStats()
    }

    fun getScoreBoardById(id: Int): UserPublicInfo {
        return RepoUsers.getById(id).getOrThrow().publicInfo()
    }

    fun getGamesStats(from: Date, to: Date): GamePublicInfo {
        val games: List<Game> = RepoGames.getAll().filter { it.date in from..to }

        fun numberOfGames(status : String) : Int {
            return games.map { it.status }.filter { it.toString() == status }.count()
        }

        return GamePublicInfo(numberOfGames("NEW"), numberOfGames("ONGOING"), numberOfGames("FINISHED"), numberOfGames("CANCELED"), games)

    }



}