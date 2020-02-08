package parsing.flat

import java.util.*

data class Flat(
    val name: String,
    val date: Calendar,
    val url: String,
    val price: String,
    val address: String,
    val description: String,
    val images: List<String>
)