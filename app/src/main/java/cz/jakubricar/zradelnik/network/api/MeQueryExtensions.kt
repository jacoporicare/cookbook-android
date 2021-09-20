package cz.jakubricar.zradelnik.network.api

import cz.jakubricar.zradelnik.MeQuery
import cz.jakubricar.zradelnik.domain.LoggedInUser

fun MeQuery.Data.asDomain() =
    LoggedInUser(
        id = me.id,
        displayName = me.displayName
    )
