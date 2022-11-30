package com.example.simple_english.lib

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simple_english.data.TaskHeader
import com.example.simple_english.data.User

open class TaskModel: ViewModel() {
    val tasks: MutableLiveData<ArrayList<TaskHeader>> by lazy {  // tasks of certain type
        MutableLiveData<ArrayList<TaskHeader>>()
    }

    val currentTask: MutableLiveData<TaskHeader> by lazy {  // currently chosen task
        MutableLiveData<TaskHeader>()
    }

    val currentType: MutableLiveData<String> by lazy {  // type of current tasks
        MutableLiveData<String>()
    }

    val transitionName: MutableLiveData<String> by lazy {  // transition name for chosen task
        MutableLiveData<String>()
    }

    val user: MutableLiveData<User> by lazy {  // current user
        MutableLiveData<User>()
    }
}