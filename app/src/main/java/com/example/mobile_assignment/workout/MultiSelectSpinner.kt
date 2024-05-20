// MultiSelectSpinner.kt
package com.example.mobile_assignment.workout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class MultiSelectSpinner(context: Context, attrs: AttributeSet?) : Spinner(context, attrs),
    AdapterView.OnItemSelectedListener {

    private val selectedItems = BooleanArray(daysOfWeek.size)

    companion object {
        val daysOfWeek = listOf(
            "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
        )
    }

    private var adapter: ArrayAdapter<String>

    init {
        adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, daysOfWeek)
        setAdapter(adapter)
        onItemSelectedListener = this
    }

    override fun onItemSelected(
        parent: AdapterView<*>?, view: View?, position: Int, id: Long
    ) {
        selectedItems[position] = !selectedItems[position]
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    fun getSelectedDays(): List<String> {
        return daysOfWeek.filterIndexed { index, _ -> selectedItems[index] }
    }

    fun setSelectedDays(days: List<String>) {
        days.forEach {
            val index = daysOfWeek.indexOf(it)
            if (index >= 0) selectedItems[index] = true
        }
    }
}
