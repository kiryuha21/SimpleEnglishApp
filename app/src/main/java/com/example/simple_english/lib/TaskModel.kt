package com.example.simple_english.lib

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simple_english.data.TaskHeader

open class TaskModel: ViewModel() {
    val tasks: MutableLiveData<ArrayList<TaskHeader>> by lazy {
        MutableLiveData<ArrayList<TaskHeader>>()
    }

    val tasksType: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val currentTask: MutableLiveData<TaskHeader> by lazy {
        MutableLiveData<TaskHeader>()
    }
}