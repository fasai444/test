package com.example.kotlintodopractice.utils.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlintodopractice.R
import com.example.kotlintodopractice.databinding.EachTodoItemBinding
import com.example.kotlintodopractice.utils.model.ToDoData

class TaskAdapter(private val list: MutableList<ToDoData>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private  val TAG = "TaskAdapter"
    private var listener:TaskAdapterInterface? = null
    fun setListener(listener:TaskAdapterInterface){
        this.listener = listener
    }
    class TaskViewHolder(val binding: EachTodoItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding =
            EachTodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.todoTask.text = this.task
                binding.dueDate.text = "Due: ${this.dueDate} at ${this.dueTime}"
                binding.categoryIcon.setImageResource(this.iconResource)

                // Set background color based on priority
                when (this.priority) {
                    "High" -> binding.root.setBackgroundResource(R.color.priority_high) // Red background
                    "Medium" -> binding.root.setBackgroundResource(R.color.priority_medium) // Orange background
                    "Low" -> binding.root.setBackgroundResource(R.color.priority_low) // Green background
                    else -> binding.root.setBackgroundResource(R.color.default_background) // Default background
                }

                // Handle checkbox state changes
                binding.checkboxComplete.setOnCheckedChangeListener(null)
                binding.checkboxComplete.setOnCheckedChangeListener { _, isChecked ->
                    this.completed = isChecked
                    listener?.onTaskCompletionToggled(this, position)
                }

                // Edit and delete actions
                binding.editTask.setOnClickListener {
                    listener?.onEditItemClicked(this, position)
                }

                binding.deleteTask.setOnClickListener {
                    listener?.onDeleteItemClicked(this, position)
                }
            }
        }
    }







    override fun getItemCount(): Int {
        return list.size
    }

    interface TaskAdapterInterface {
        fun onDeleteItemClicked(toDoData: ToDoData, position: Int)
        fun onEditItemClicked(toDoData: ToDoData, position: Int)
        fun onTaskCompletionToggled(toDoData: ToDoData, position: Int)
    }


}