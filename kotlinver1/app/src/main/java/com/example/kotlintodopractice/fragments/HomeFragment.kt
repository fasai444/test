package com.example.kotlintodopractice.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlintodopractice.databinding.FragmentHomeBinding
import com.example.kotlintodopractice.utils.adapter.TaskAdapter
import com.example.kotlintodopractice.utils.model.ToDoData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(), ToDoDialogFragment.OnDialogNextBtnClickListener,
    TaskAdapter.TaskAdapterInterface {

    private val TAG = "HomeFragment"
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: DatabaseReference
    private var frag: ToDoDialogFragment? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var authId: String

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var toDoItemList: MutableList<ToDoData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        // Get data from Firebase
        getTaskFromFirebase()

        binding.addTaskBtn.setOnClickListener {
            if (frag != null)
                childFragmentManager.beginTransaction().remove(frag!!).commit()
            frag = ToDoDialogFragment()
            frag!!.setListener(this)
            frag!!.show(
                childFragmentManager,
                ToDoDialogFragment.TAG
            )
        }
    }

    private fun getTaskFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                toDoItemList.clear()
                for (taskSnapshot in snapshot.children) {
                    try {
                        val todoTask = taskSnapshot.getValue(ToDoData::class.java)
                        if (todoTask != null) {
                            toDoItemList.add(todoTask)
                        }
                    } catch (e: DatabaseException) {
                        Log.e(TAG, "Data format mismatch: ${e.message}")
                    }
                }
                taskAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        authId = auth.currentUser!!.uid
        database = Firebase.database.reference.child("Tasks").child(authId)

        binding.mainRecyclerView.setHasFixedSize(true)
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(context)

        toDoItemList = mutableListOf()
        taskAdapter = TaskAdapter(toDoItemList)
        taskAdapter.setListener(this)
        binding.mainRecyclerView.adapter = taskAdapter
    }

    override fun saveTask(
        todoTask: String,
        dueDate: String,
        dueTime: String,
        category: String,
        priority: String,
        iconResource: Int
    ) {
        val taskId = database.push().key ?: return
        val toDoData = ToDoData(
            taskId = taskId,
            task = todoTask,
            completed = false,
            dueDate = dueDate,
            dueTime = dueTime,
            category = category,
            priority = priority,
            iconResource = iconResource
        )

        database.child(taskId).setValue(toDoData)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Task Added Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        frag!!.dismiss()
    }

    override fun updateTask(toDoData: ToDoData) {
        val map = HashMap<String, Any>()
        map["task"] = toDoData.task
        map["completed"] = toDoData.completed
        map["dueDate"] = toDoData.dueDate ?: ""
        map["dueTime"] = toDoData.dueTime ?: ""
        map["category"] = toDoData.category ?: ""
        map["priority"] = toDoData.priority ?: ""

        database.child(toDoData.taskId).updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Task Updated Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDeleteItemClicked(toDoData: ToDoData, position: Int) {
        database.child(toDoData.taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditItemClicked(toDoData: ToDoData, position: Int) {
        if (frag != null)
            childFragmentManager.beginTransaction().remove(frag!!).commit()

        frag = ToDoDialogFragment.newInstance(toDoData.taskId, toDoData.task)
        frag!!.setListener(this)
        frag!!.show(
            childFragmentManager,
            ToDoDialogFragment.TAG
        )
    }

    override fun onTaskCompletionToggled(toDoData: ToDoData, position: Int) {
        val map = HashMap<String, Any>()
        map["completed"] = toDoData.completed
        database.child(toDoData.taskId).updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Task status updated", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
