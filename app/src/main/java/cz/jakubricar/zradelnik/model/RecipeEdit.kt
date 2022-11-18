package cz.jakubricar.zradelnik.model

import androidx.compose.runtime.Immutable

@Immutable
data class RecipeEdit(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val directions: String?,
    val ingredients: List<Ingredient>,
    val preparationTime: Int?,
    val servingCount: Int?,
    val sideDish: String?,
    val tags: List<String>,
) {

    @Immutable
    data class Ingredient(
        val id: String,
        val name: String,
        val isGroup: Boolean,
        val amount: Double?,
        val amountUnit: String?,
    )

    data class NewImage(
        val mimeType: String,
        val bytes: ByteArray,
    ) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NewImage

            if (mimeType != other.mimeType) return false
            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = mimeType.hashCode()
            result = 31 * result + bytes.contentHashCode()
            return result
        }
    }
}
