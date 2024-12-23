package com.example.kotlintodopractice.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.kotlintodopractice.databinding.FragmentToDoDialogBinding
import com.example.kotlintodopractice.utils.model.ToDoData
import java.util.Calendar
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.example.kotlintodopractice.R

class ToDoDialogFragment : DialogFragment() {

    private val categoryIcons = mapOf(
        "Work" to R.drawable.ic_work,
        "Personal" to R.drawable.ic_personal,
        "School" to R.drawable.ic_school
    )

    private lateinit var binding: FragmentToDoDialogBinding
    private var listener: OnDialogNextBtnClickListener? = null
    private var toDoData: ToDoData? = null

    fun setListener(listener: OnDialogNextBtnClickListener) {
        this.listener = listener
    }

    companion object {
        const val TAG = "DialogFragment"

        @JvmStatic
        fun newInstance(taskId: String, task: String) =
            ToDoDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("taskId", taskId)
                    putString("task", task)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentToDoDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(), // 95% of screen width
            ViewGroup.LayoutParams.WRAP_CONTENT // Dynamic height to fit content
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Predefined categories
        val categories = listOf("Work", "Personal", "School")
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.categoryDropdown.setAdapter(categoryAdapter)

        // Predefined priorities
        val priorities = listOf("High", "Medium", "Low")
        val priorityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, priorities)
        binding.priorityDropdown.setAdapter(priorityAdapter)

        // Show dropdown when the field gains focus (category)
        binding.categoryDropdown.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                (view as AutoCompleteTextView).showDropDown()
            }
        }

        // Show dropdown when the field gains focus (priority)
        binding.priorityDropdown.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                (view as AutoCompleteTextView).showDropDown()
            }
        }

        // Pre-fill fields if editing an existing task
        if (arguments != null) {
            toDoData = ToDoData(
                arguments?.getString("taskId").toString(),
                arguments?.getString("task").toString()
            )
            binding.todoEt.setText(toDoData?.task)
            binding.dueDateEditText.setText(toDoData?.dueDate)
            binding.dueTimeEditText.setText(toDoData?.dueTime)
            binding.categoryDropdown.setText(toDoData?.category, false) // Load existing category
            binding.priorityDropdown.setText(toDoData?.priority, false) // Load existing priority
        }

        // Close Button
        binding.todoClose.setOnClickListener {
            dismiss()
        }

        // Save Button
        binding.todoNextBtn.setOnClickListener {
            val todoTask = binding.todoEt.text.toString()
            val dueDate = binding.dueDateEditText.text.toString()
            val dueTime = binding.dueTimeEditText.text.toString()
            val category = binding.categoryDropdown.text.toString()
            val priority = binding.priorityDropdown.text.toString()

            if (todoTask.isNotEmpty() && dueDate.isNotEmpty() && dueTime.isNotEmpty() && category.isNotEmpty() && priority.isNotEmpty()) {
                val icon = categoryIcons[category] ?: R.drawable.ic_default
                if (toDoData == null) {
                    listener?.saveTask(todoTask, dueDate, dueTime, category, priority, icon)
                } else {
                    toDoData!!.task = todoTask
                    toDoData!!.dueDate = dueDate
                    toDoData!!.dueTime = dueTime
                    toDoData!!.category = category
                    toDoData!!.priority = priority
                    toDoData!!.iconResource = icon
                    listener?.updateTask(toDoData!!)
                }
                dismiss()
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Date Picker Dialog
        binding.dueDateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val formattedDate = "$dayOfMonth/${month + 1}/$year"
                    binding.dueDateEditText.setText(formattedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Time Picker Dialog
        binding.dueTimeEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                    binding.dueTimeEditText.setText(formattedTime)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }
    }

    interface OnDialogNextBtnClickListener {
        fun saveTask(
            todoTask: String,
            dueDate: String,
            dueTime: String,
            category: String,
            priority: String,
            iconResource: Int
        )

        fun updateTask(toDoData: ToDoData)
    }
}
