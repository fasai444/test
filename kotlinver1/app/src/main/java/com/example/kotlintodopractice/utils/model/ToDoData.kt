package com.example.kotlintodopractice.utils.model

data class ToDoData(
    val taskId: String = "",
    var task: String = "",
    var completed: Boolean = false,
    var dueDate: String = "",
    var dueTime: String = "",
    var category: String = "",
    var priority: String = "", // New field for priority
    var iconResource: Int = 0 // New field for icon
)

