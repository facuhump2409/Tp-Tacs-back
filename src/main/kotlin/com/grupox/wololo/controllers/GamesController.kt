package com.grupox.wololo.controllers

import arrow.core.*
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.fx
import arrow.core.extensions.list.traverse.sequence

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.*
import com.grupox.wololo.model.helpers.GameForm
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.ProvinceGeoRef
import com.grupox.wololo.model.helpers.TownForm
import com.grupox.wololo.model.helpers.*
import com.grupox.wololo.model.repos.RepoGames
import com.grupox.wololo.model.repos.RepoUsers
import com.grupox.wololo.model.services.GeoRef
import com.grupox.wololo.model.services.TopoData
import com.grupox.wololo.services.GamesService
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import kotlin.collections.ArrayList


@RequestMapping("/games")
@RestController
class GamesController(@Autowired private val geoRef: GeoRef, @Autowired private val topoData: TopoData) : BaseController() {
    @Autowired
    lateinit var gamesService: GamesService

    @GetMapping
    @ApiOperation(value = "Gets the games of the current user")
    fun getGames(@RequestParam("sort", required = false) sort: String?, // TODO query params
                 @RequestParam("filter", required = false) filter: String?,
                 @ApiIgnore @CookieValue("X-Auth") authCookie : String?): List<Game> {
        val token = checkAndGetToken(authCookie)
        val user = RepoUsers.getById(token.body.subject.toInt()).getOrElse { throw CustomException.NotFound.UserNotFoundException() }
        return RepoGames.filter { it.isParticipating(user) }
    }


    @PostMapping
    @ApiOperation(value = "Creates a new game")
    fun createGame(@RequestBody form: GameForm, @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        checkAndGetToken(authCookie)

        val game: Game = Either.fx<CustomException, Game> {
            val users = !form.participantsIds.map { RepoUsers.getById(it).toEither { CustomException.NotFound.UserNotFoundException() } }
                                            .sequence(Either.applicative()).fix().map{ it.fix() }

            val townsData: List<TownGeoRef> = !geoRef.requestTownsData(form.provinceName, form.townAmount)

            val towns = !townsData.map { data ->
                topoData.requestElevation(data.coordinates).map { elevation ->
                    Town(data.id, data.name, data.coordinates, elevation.toDouble())
                }
            }.sequence(Either.applicative()).fix().map { it.fix() }
            Game(0,users,  Province(0,form.provinceName, ArrayList(towns)))
        }.getOrHandle { throw it }

        RepoGames.insert(game)
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Gets a game")
    fun getGameById(@PathVariable("id") id: Int,
                    @ApiIgnore @CookieValue("X-Auth") authCookie : String?): Game {
        val token = checkAndGetToken(authCookie)
        val user = RepoUsers.getById(token.body.subject.toInt()).getOrElse { throw CustomException.NotFound.UserNotFoundException() }
        val game = RepoGames.getById(id).getOrElse { throw CustomException.NotFound.GameNotFoundException() }

        if(!game.isParticipating(user)) throw CustomException.Unauthorized.TokenException("User not participating in this game") // Por ahi no corresponde esta excepcion
        return game
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Surrenders in a game (it becomes CANCELED)")
    fun surrender(@PathVariable("id") id: Int, @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {

        val userId: String = JwtSigner.validateJwt(authCookie.toOption()).getOrHandle { throw it }.body.subject
        val game: Game = RepoGames.getById(id).getOrElse { throw CustomException.NotFound.GameNotFoundException() }
        val user: User = game.getMember(userId.toInt()).getOrElse { throw CustomException.NotFound.UserNotFoundException() }
        val loserUserId: Int = user.id

        val participantsIds: List<Int> = game.players.map{it.id}

        val loserUser: User = RepoUsers.getById(loserUserId).getOrElse {  throw CustomException.NotFound.UserNotFoundException() }

        loserUser.updateGamesLostStats()

        if ((participantsIds.size) <= 2) {
            val winnerUserID : Int = participantsIds.find { it != userId.toInt() }.toOption().getOrElse {throw CustomException.BadRequest.IllegalGameException("Not enough participants from game") }
            game.status = Status.CANCELED
            RepoUsers.getById(winnerUserID).getOrElse {  throw CustomException.NotFound.UserNotFoundException() }.updateGamesWonStats()}
        }




    @PostMapping("/{id}/actions/movement")
    @ApiOperation(value = "Moves the gauchos between towns")
    fun moveGauchosBetweenTowns(
            @PathVariable("id") id: Int,
            @RequestBody movementData: MovementForm,
            @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        val userId = checkAndGetToken(authCookie).body.subject
        gamesService.moveGauchosBetweenTowns(userId.toInt(), id, movementData)
    }

    @PostMapping("/{id}/actions/attack")
    @ApiOperation(value = "Attacks a town")
    fun attackTown(
            @PathVariable("id") id: Int,
            @RequestBody attackData: AttackForm,
            @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        val userId = checkAndGetToken(authCookie).body.subject
        gamesService.attackTown(userId.toInt(), id, attackData)
    }

    @PutMapping("/{id}/towns/{idTown}")
    @ApiOperation(value = "Updates the town specialization")
    fun updateTownSpecialization(
            @PathVariable("id") id: Int,
            @PathVariable("idTown") idTown: Int,
            @RequestBody townData: TownForm,
            @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        checkAndGetToken(authCookie)

        if (townData.specialization == "PRODUCTION"){

            RepoGames.getById(id).getOrElse {throw CustomException.NotFound.GameNotFoundException() }.changeTownSpecialization(idTown, Production())

        } else if (townData.specialization == "DEFENSE"){
            RepoGames.getById(id).getOrElse {throw CustomException.NotFound.GameNotFoundException() }.changeTownSpecialization(idTown, Defense())
        }

    }

    @GetMapping("/{id}/towns/{idTown}")
    @ApiOperation(value = "Gets the town stats and an image")
    fun getTownData(
            @PathVariable("id") id: Int,
            @PathVariable("idTown") idTown: Int,
            @ApiIgnore @CookieValue("X-Auth") authCookie : String?) {
        checkAndGetToken(authCookie)
        TODO("visualizar las estadísticas de producción y defensa de cada municipio, y la imagen")
    }

    @GetMapping("/provinces")
    @ApiOperation(value = "Gets all provinces")
    fun getProvinces(@ApiIgnore @CookieValue("X-Auth") authCookie : String?) : List<ProvinceGeoRef> {
        checkAndGetToken(authCookie)
        return geoRef.requestAvailableProvinces().getOrHandle { throw it }
    }
}
