package com.example.simple_english.lib

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simple_english.data.TaskHeader
import com.example.simple_english.data.User

open class TaskModel: ViewModel() {
    val tasks: MutableLiveData<ArrayList<TaskHeader>> by lazy {
        MutableLiveData<ArrayList<TaskHeader>>()
    }

    val currentTask: MutableLiveData<TaskHeader> by lazy {
        MutableLiveData<TaskHeader>()
    }

    val currentType: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val transitionName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val user: MutableLiveData<User> by lazy {
        MutableLiveData<User>()
    }
}