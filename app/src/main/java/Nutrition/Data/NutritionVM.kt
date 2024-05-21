package Nutrition.Data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

class NutritionVM : ViewModel() {

    private val db = Firebase.firestore
    private val foodLD = MutableLiveData<List<FoodItem>>(emptyList())
    private var listener : ListenerRegistration? = null
    private val resultLD = MutableLiveData<List<FoodItem>>()

    private var userId = "U001"

    init {
        listener = getPersonalFoodReference(userId).addSnapshotListener { snap, _ ->
            foodLD.value = snap?.toObjects(FoodItem::class.java)
            updateResult()
        }
    }


    override fun onCleared() {
        listener?.remove()
    }


    fun init() = Unit

    fun getFoodLD() = foodLD

    fun getAll() = foodLD.value ?: emptyList()

    fun get(id: String) = getAll().find { it.foodId == id }

    fun getFoodById(id: String): LiveData<FoodItem?> {
        val foodItemLD = MutableLiveData<FoodItem?>()
        db.collection("foodList").document(id).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                foodItemLD.value = snapshot.toObject(FoodItem::class.java)
            } else {
                foodItemLD.value = null
            }
        }
        return foodItemLD
    }

    fun getResultLD() = resultLD

    private var name = ""
    fun search(name: String) {
        this.name = name
        updateResult()
    }

    fun updateResult() {
        var list = getAll()

        list = list.filter {
            it.foodName.contains(name, true)
        }

        resultLD.value = list
    }

    fun generateCustomId(callback: (String) -> Unit) {
        db.collection("foodList")
            .get()
            .addOnSuccessListener { result ->
                val idList = result.documents.map { it.id }
                var newId = "F001"
                var idNumber = 1
                while (idList.contains(newId)) {
                    idNumber++
                    newId = "F" + String.format("%03d", idNumber)
                }
                callback(newId)
            }
            .addOnFailureListener {
                callback("F001") // Default ID in case of error
            }
    }


    fun addFoodItem(userId: String, foodItem: FoodItem) {
        getPersonalFoodReference(userId).document(foodItem.foodId).set(foodItem)
    }

    fun edit(userId: String, foodId: String, foodName: String, calories : Int, carbs: Int, protein: Int, fat: Int, description: String) {
        val foodRef = getPersonalFoodReference(userId).document(foodId)
        foodRef.update(
            mapOf(
                "foodName" to foodName,
                "calories" to calories,
                "protein" to protein,
                "carbs" to carbs,
                "fat" to fat,
                "description" to description
            )
        ).addOnSuccessListener {
            // Handle success, e.g., log or show a message
        }.addOnFailureListener { exception ->
            // Handle failure, e.g., log or show a message
        }
    }

    fun delete(userId: String, foodId: String) {
        getPersonalFoodReference(userId).document(foodId)
            .delete()
            .addOnSuccessListener {
                // Handle success if needed
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }

    fun getDailyTrackerReference(userId: String, date: String): LiveData<List<TrackerItem>> {
        val trackerItemsLiveData = MutableLiveData<List<TrackerItem>>()

        db.collection("trackerList")
            .document(userId)
            .collection("dates")
            .document(date)
            .collection("trackerItems")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val trackerItems = snapshot.documents.map { document ->
                        val item = document.toObject(TrackerItem::class.java)
                        item?.copy(documentId = document.id) ?: TrackerItem()
                    }
                    trackerItemsLiveData.value = trackerItems
                } else {
                    trackerItemsLiveData.value = emptyList()
                }
            }

        return trackerItemsLiveData
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteTrackerItem(date: String, item: TrackerItem) {
        val userId = "A001" // Get the user ID from wherever it's stored
        val itemId = item.documentId // Get the ID of the item to delete

        // Reference to the tracker item document to delete
        val docRef = Firebase.firestore.collection("trackerList")
            .document(userId)
            .collection("dates")
            .document(date)
            .collection("trackerItems")
            .document(itemId)

        // Delete the document
        docRef.delete()
            .addOnSuccessListener {
                // Document successfully deleted
            }
            .addOnFailureListener { e ->
                // Log the error message or handle it as needed
            }
    }

    fun updateTargetCalories(userId: String, date: String, newTargetCalories: Int) {
        // Reference to the date document
        val dateDocRef = db.collection("trackerList")
            .document(userId)
            .collection("dates")
            .document(date)

        // Update the targetCalories field in the date document
        dateDocRef.update("caloriesTarget", newTargetCalories)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }

    fun getTargetCalories(userId: String, date: String): LiveData<Int> {
        val targetCaloriesLiveData = MutableLiveData<Int>()

        db.collection("trackerList")
            .document(userId)
            .collection("dates")
            .document(date)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val targetCalories = snapshot.getLong("caloriesTarget")?.toInt() ?: 0
                    targetCaloriesLiveData.value = targetCalories
                } else {
                    targetCaloriesLiveData.value = 0
                }
            }

        return targetCaloriesLiveData
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun initializeTrackerForNewDay(userId: String, date: String) {
        val dateRef = db.collection("trackerList")
            .document(userId)
            .collection("dates")
            .document(date)

        dateRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Document for the current date already exists, no need to create a new one
                } else {
                    // Document for the current date doesn't exist, create a new one
                    val dateItem = Nutrition.Data.DateItem(date = date, caloriesTarget = 2000)
                    dateRef.set(dateItem)
                        .addOnFailureListener { e ->
                            // Handle failure to set DateItem
                            e.printStackTrace()
                        }
                }
            }
            .addOnFailureListener { e ->
                // Handle failure to get DateItem
                e.printStackTrace()
            }
    }

    fun initializePersonalFoodForNewUser(userId: String) {
        val personalFoodRef = FirebaseFirestore.getInstance().collection("trackerList")
            .document(userId).collection("personalFood")

        personalFoodRef.get().addOnSuccessListener { snapshot ->
            if (snapshot != null && !snapshot.isEmpty) {
                // Personal food for the user already exists, no need to create a new one
            } else {
                // Personal food for the user doesn't exist, create a new one
                val foodListRef = Firebase.firestore.collection("foodList")
                foodListRef.get().addOnSuccessListener { foodListSnapshot ->
                    if (foodListSnapshot != null) {
                        // Get all documents from "foodList" collection
                        val foodItems = foodListSnapshot.documents.mapNotNull { document ->
                            document.toObject(FoodItem::class.java)
                        }

                        // Write each food item into the new user's "personalFood" collection
                        for (foodItem in foodItems) {
                            personalFoodRef.document(foodItem.foodId).set(foodItem)
                        }
                    }
                }
            }
        }
    }


}


