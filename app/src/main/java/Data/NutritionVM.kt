package Data

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import java.time.LocalDateTime

class NutritionVM : ViewModel() {

    private val db = Firebase.firestore
    private val foodLD = MutableLiveData<List<FoodItem>>(emptyList())
    private var listener : ListenerRegistration? = null


    private val resultLD = MutableLiveData<List<FoodItem>>()

    init {
        listener = FOOD.addSnapshotListener { snap, _ ->
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


    fun addFoodItem(foodItem: FoodItem) {
        FOOD.document(foodItem.foodId).set(foodItem)
    }

    fun edit(foodId: String, foodName: String, calories : Int, carbs: Int, protein: Int, fat: Int, description: String) {
        val foodRef = FOOD.document(foodId)
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

    fun delete(foodId: String) {
        FOOD.document(foodId)
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
    fun deleteTrackerItem(item: TrackerItem) {
        val userId = "A001" // Get the user ID from wherever it's stored
        val date = LocalDateTime.now().toLocalDate().toString() // Get the current date
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



}


