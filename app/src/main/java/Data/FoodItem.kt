package Data

import com.google.firebase.Firebase
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.firestore

data class FoodItem (
    @DocumentId
    var foodId: String = "",
    var foodName: String = "",
    var calories: Int = 0,
    var protein: Int = 0,
    var carbs: Int = 0,
    var fat: Int = 0,
    var image: Blob = Blob.fromBytes(ByteArray(0)),
    var description: String = ""


)

data class CalTracker (
    @DocumentId
    var trackerId: String = "",
    var userId: String = "",
    var foodId: String = ""
)

val FOOD = Firebase.firestore.collection("foodList")
val TRACKER = Firebase.firestore.collection("trackerList")

