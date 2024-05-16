package Data

import com.google.firebase.Firebase
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.firestore

class FoodItem {
    @DocumentId
    var foodId: String = ""
    var foodName: String = ""
    var calories: Int = 0
    var protein: Int = 0
    var carbs: Int = 0
    var fat: Int = 0
    var image: Blob = Blob.fromBytes(ByteArray(0))
    var description: String = ""

}

val FOOD = Firebase.firestore.collection("food")

fun RESTORE() {
    val food = listOf(
        FoodItem().apply {
            foodName = "Apple"
            calories = 52
            protein = 0
            carbs = 14
            fat = 0
            image = Blob.fromBytes(ByteArray(0))
            foodId = "F001"
            description = "A juicy red apple"
        },
        FoodItem().apply {
            foodName = "Banana"
            calories = 89
            protein = 1
            carbs = 23
            fat = 0
            image = Blob.fromBytes(ByteArray(0))
            foodId = "F002"
            description = "A ripe yellow banana"

        },
        FoodItem().apply {
            foodName = "Orange"
            calories = 47
            protein = 1
            carbs = 12
            fat = 0
            image = Blob.fromBytes(ByteArray(0))
            foodId = "F003"
            description = "A sweet orange"
        }
    )

    food.forEach {
        FOOD.document(it.foodId).set(it)
    }
}

