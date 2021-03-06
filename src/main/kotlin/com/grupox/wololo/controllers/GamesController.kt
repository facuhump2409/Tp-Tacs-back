package com.grupox.wololo.controllers

import com.grupox.wololo.model.Status
import com.grupox.wololo.model.externalservices.ProvinceGeoJSON
import com.grupox.wololo.model.externalservices.TownGeoJSON
import com.grupox.wololo.model.helpers.*
import com.grupox.wololo.services.GamesControllerService
import com.grupox.wololo.services.UsersControllerService
import io.swagger.annotations.ApiOperation
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore
import java.util.*
import javax.servlet.http.HttpServletRequest

@RequestMapping("/games")
@RestController
class GamesController : BaseController() {
    @Autowired
    lateinit var gamesControllerService: GamesControllerService

    @Autowired
    lateinit var usersControllerService: UsersControllerService

    @GetMapping
    @ApiOperation(value = "Gets the games of the current user. Params: sort=id|date|numberOfTowns|numberOfPlayers & status & date")
    fun getGames(
            @RequestParam("sort", required = false) sort: String?,
            @RequestParam("status", required = false) status: Status?,
            @RequestParam("date", required = false) date: Date?,
            request: HttpServletRequest): List<DTO.GameDTO> {
        val userId = checkAndGetUserId(request)
        return gamesControllerService.getGames(userId, sort, status, date)
    }

    @PostMapping
    @ApiOperation(value = "Creates a new game")
    fun createGame(@RequestBody form: GameForm, request: HttpServletRequest): DTO.GameDTO {
        val userId = checkAndGetUserId(request)
        return gamesControllerService.createGame(userId, form)
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Gets a game")
    fun getGameById(@PathVariable("id") id: String, request: HttpServletRequest): DTO.GameDTO {
        val userId = checkAndGetUserId(request)
        return gamesControllerService.getGame(userId, ObjectId(id))
    }

    @GetMapping("/stats")
    @ApiOperation(value = "Gets games stats from a date range")
    fun getGamesStats(
            @RequestParam("from", required = false) from: Date?,
            @RequestParam("to", required = false) to: Date?,
            @ApiIgnore @CookieValue("X-Auth") authCookie : String?,
            request: HttpServletRequest): GamePublicInfo {
        val userId = checkAndGetUserId(request)
        usersControllerService.throwIfNotAllowed(userId)
        val list: List<DTO.GameDTO> = if (from != null && to != null) {
            gamesControllerService.getGamesInADateRange(from, to)
        } else {
            gamesControllerService.getAllGamesDTO()
        }
        return gamesControllerService.getGamesStats(list)
    }

    @GetMapping("/date")
    @ApiOperation(value = "Gets games from a date range")
    fun getGamesByDateRange(
            @RequestParam("from", required = false) from: Date,
            @RequestParam("to", required = false) to: Date,
            @ApiIgnore @CookieValue("X-Auth") authCookie : String?,
            request: HttpServletRequest): List<DTO.GameDTO> {
        val userId = checkAndGetUserId(request)
        usersControllerService.throwIfNotAllowed(userId)
        return gamesControllerService.getGamesInADateRange(from, to)
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Surrenders in a game (it becomes CANCELED)")
    fun surrender(@PathVariable("id") id: String, request: HttpServletRequest): Change.GameChange {
        val userId = checkAndGetUserId(request)
        return gamesControllerService.surrender(ObjectId(id), userId)
    }

    @PutMapping("/{id}/actions/turn")
    @ApiOperation(value = "Finishes the current Turn")
    fun finishTurn(@PathVariable("id") id: String, request: HttpServletRequest): Change.GameChange{
        val userId = checkAndGetUserId(request)
        return gamesControllerService.finishTurn(userId, ObjectId(id))
    }

    @PostMapping("/{id}/actions/movement")
    @ApiOperation(value = "Moves the gauchos between towns")
    fun moveGauchosBetweenTowns(
            @PathVariable("id") id: String,
            @RequestBody movementData: MovementForm,
            request: HttpServletRequest): Change.GameChange {
        val userId = checkAndGetUserId(request)
        return gamesControllerService.moveGauchosBetweenTowns(userId, ObjectId(id), movementData)
    }

    @PostMapping("/{id}/actions/attack")
    @ApiOperation(value = "Attacks a town")
    fun attackTown(
            @PathVariable("id") id: String,
            @RequestBody attackData: AttackForm,
            request: HttpServletRequest): Change.GameChange {
        val userId = checkAndGetUserId(request)
        return gamesControllerService.attackTown(userId, ObjectId(id), attackData)
    }

    @PutMapping("/{id}/towns/{idTown}")
    @ApiOperation(value = "Updates the town specialization")
    fun updateTownSpecialization(
            @PathVariable("id") id: String,
            @PathVariable("idTown") townId: Int,
            @RequestBody newSpecialization: String,
            request: HttpServletRequest): Change.GameChange {
        val userId = checkAndGetUserId(request)
        return gamesControllerService.updateTownSpecialization(userId, ObjectId(id), townId, newSpecialization)
    }

    @GetMapping("/{id}/towns/{idTown}")
    @ApiOperation(value = "Gets the town stats and an image")
    fun getTownData(
            @PathVariable("id") id: String,
            @PathVariable("idTown") idTown: Int,
            request: HttpServletRequest) : DTO.TownDTO {
        checkAndGetUserId(request)
        return gamesControllerService.getTownStats(ObjectId(id), idTown)
    }

    @GetMapping("/provinces")
    @ApiOperation(value = "Gets all provinces")
    fun getProvinces(request: HttpServletRequest) : List<ProvinceGeoJSON> {
        checkAndGetUserId(request)
        return gamesControllerService.getProvinces()
    }

    @GetMapping("/provinces/towns-geojsons")
    @ApiOperation("Gets the GeoJSON data from the list of '|' separated towns")
    fun getTownsGeoJSONs(
            @RequestParam("province") province: String,
            @RequestParam("towns") towns: String,
            request: HttpServletRequest) : List<TownGeoJSON> {
        checkAndGetUserId(request)
        return gamesControllerService.getTownsGeoJSONs(province, towns)
    }

    @PatchMapping("/configuration")
    @ApiOperation(value = "Changes a value of a configuration item")
    fun changeGameModeConfiguration(@RequestBody changes: Map<String, Double>, request: HttpServletRequest) {
        val userId = checkAndGetUserId(request)
        usersControllerService.throwIfNotAllowed(userId)
        GamesConfigHelper.updateValues(changes)
    }

    @GetMapping("/configuration")
    @ApiOperation("Gets the games configuration values")
    fun getConfigurationValues(request: HttpServletRequest): Map<String, Double> {
        val userId = checkAndGetUserId(request)
        usersControllerService.throwIfNotAllowed(userId)
        return GamesConfigHelper.getAllConfigurationValues()
    }
}