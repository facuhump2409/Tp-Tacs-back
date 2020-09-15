package com.grupox.wololo.model.repos

import arrow.core.*
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import java.util.*

object RepoGames : Repository<Game> {

    private val gamesInDB: ArrayList<Game> = arrayListOf(
            Game(
                    id = 1,
                    players = listOf(User(5, "mail", "password", false, Stats(1,1)),  User(6, "mail2", "password2", false)),
                    province = Province( id = 1,
                            name = "Santiago del Estero",
                            towns = arrayListOf(Town(1, "Termas de Río Hondo", Coordinates(0f,0f), 0.0, null), Town(2, "La Banda", Coordinates(0f,0f), 0.0, null))
                    ),
                    status= Status.NEW
            ),
            Game(
                    id= 2,
                    players = listOf(User(5, "mail", "password", false)),
                    province = Province( id = 2,
                            name = "Córdoba",
                            towns = arrayListOf(Town(3, "Cipolletti", Coordinates(0f,0f), 0.0, null))
                    ),
                    status = Status.FINISHED
            )
    )

    override fun getAll(): List<Game> = gamesInDB

    override fun getById(id: Int): Either<CustomException.NotFound, Game> = getAll().find { it.id == id }.rightIfNotNull { CustomException.NotFound.GameNotFoundException() }

    override fun filter(predicate: (game: Game) -> Boolean) = getAll().filter { predicate(it) }

    override fun insert(obj: Game) {
        gamesInDB.add(obj)
    }
}
