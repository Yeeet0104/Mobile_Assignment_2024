package Data

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects

class NutritionVM : ViewModel() {

    private val db = Firebase.firestore
    private val foodLD = MutableLiveData<List<FoodItem>>(emptyList())
    private var listener : ListenerRegistration? = null


    private val resultLD = MutableLiveData<List<FoodItem>>()
    private var foodName = ""

    init {
        listener = FOOD.addSnapshotListener { snap, _ ->
            foodLD.value = snap?.toObjects(FoodItem::class.java)
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


}


