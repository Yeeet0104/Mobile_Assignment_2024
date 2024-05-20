package Nutrition.Data

import com.google.firebase.Firebase
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
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
    var userId: String = ""
)

data class TrackerItem (
    @DocumentId
    val documentId: String = "",
    var foodId: String = "",
    var foodName: String = "",
    var calories: Int = 0,
    var image: Blob = Blob.fromBytes(ByteArray(0)),

)

data class DateItem (
    @DocumentId
    var date: String = "",
    var caloriesTarget: Int = 0
)


val FOOD = Firebase.firestore.collection("foodList")

fun getDateReference(userId: String, date: String) =
    FirebaseFirestore.getInstance().collection("trackerList")
        .document(userId).collection("dates").document(date)

fun getDailyFoodReference(userId: String, date: String) =
    FirebaseFirestore.getInstance().collection("trackerList")
        .document(userId).collection("dates").document(date)
        .collection("trackerItems")

fun getPersonalFoodReference(userId: String) =
    FirebaseFirestore.getInstance().collection("trackerList")
        .document(userId).collection("personalFood")


