package com.example.simple_english

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.simple_english.databinding.FragmentTheoryBinding

class Theory : Fragment() {
    private lateinit var fragBinding: FragmentTheoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragBinding = FragmentTheoryBinding.inflate(inflater)

        return fragBinding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = Theory()
    }
}