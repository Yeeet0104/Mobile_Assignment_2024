package Data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObjects

class NutritionVM : ViewModel() {

    private val foodLD = MutableLiveData<List<FoodItem>>(emptyList())
    private var listener : ListenerRegistration? = null
    private val _selectedFoodItem = MutableLiveData<FoodItem>()
    val selectedFoodItem: LiveData<FoodItem> get() = _selectedFoodItem

    val foodItems: MutableLiveData<List<FoodItem>> = MutableLiveData()

    fun loadFoodItems() {
        FOOD.get().addOnSuccessListener { documents ->
            val items = documents.map { it.toObject(FoodItem::class.java)!! }
            foodItems.value = items
        }
    }

    fun selectFoodItem(foodItem: FoodItem) {
        _selectedFoodItem.value = foodItem
    }
    init {
        listener = FOOD.addSnapshotListener { snap, _ ->
            foodLD.value = snap?.toObjects()

        }
    }

    override fun onCleared() {
        listener?.remove()
    }


    fun init() = Unit

    fun getFoodLD() = foodLD

    fun getAll() = foodLD.value ?: emptyList()

    fun get(id: String) = getAll().find { it.foodId == id }


    private val resultLD = MutableLiveData<List<FoodItem>>()
    private var foodName = ""


    fun getResultLD() = resultLD

    fun search(name: String) {
        resultLD.value = getAll().filter { it.foodName.contains(name, ignoreCase = true) }
        updateResult()
    }

    fun updateResult() {
        var list = getAll()

        // TODO(12A): Search by name, filter by categoryId
        list = list.filter {
            it.foodName.contains(foodName, true)
        }

        resultLD.value = list
    }
}


