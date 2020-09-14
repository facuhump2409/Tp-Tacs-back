package com.grupox.wololo.model

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import java.time.Duration
import java.time.Instant
import java.util.*

object RepoGames {

    private val gamesInDB: ArrayList<Game> = arrayListOf(
            Game(
                    id = 1,
                    players = listOf(User(5, "mail", "password", false)),
                    province = Province( id = 1,
                            name = "Santiago del Estero",
                            towns = arrayListOf(Town(1, "Termas de Río Hondo", Coordinates(0f,0f), 0f, null), Town(2, "La Banda", Coordinates(0f,0f), 0f, null))
                    ),
                    status= Status.NEW
            ),
            Game(
                    id= 2,
                    players = listOf(User(5, "mail", "password", false)),
                    province = Province( id = 2,
                            name = "Córdoba",
                            towns = arrayListOf(Town(3, "Cipolletti", Coordinates(0f,0f), 0f, null))
                    ),
                    status = Status.FINISHED
            )
    )

    fun getGames(): List<Game> = gamesInDB

    fun getGameById(id: Int): Option<Game> = gamesInDB.find { it.id == id }.toOption()

    fun filterGames(predicate: (game: Game) -> Boolean) = gamesInDB.filter { predicate(it) }

    fun insertGame(game: Game) {
        gamesInDB.add(game)
    }
}
