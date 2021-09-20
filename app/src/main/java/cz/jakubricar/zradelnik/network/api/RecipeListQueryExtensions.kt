package cz.jakubricar.zradelnik.network.api

import cz.jakubricar.zradelnik.RecipeListQuery
import cz.jakubricar.zradelnik.domain.RecipeList

fun RecipeListQuery.Data.asDomain() =
    RecipeList(
        recipes = recipes.map { it.asDomain() },
    )

fun RecipeListQuery.Recipe.asDomain() =
    RecipeList.Recipe(
        id = fragments.recipeFragment.id,
        slug = fragments.recipeFragment.slug,
        title = fragments.recipeFragment.title,
        imageUrl = fragments.recipeFragment.thumbImageUrl,
    )
