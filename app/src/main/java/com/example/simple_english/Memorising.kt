package com.example.simple_english

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.simple_english.databinding.FragmentMemorisingBinding

class Memorising : Fragment() {
    private lateinit var fragBinding: FragmentMemorisingBinding

    // https://www.google.com/search?q={0}&tbm=isch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragBinding = FragmentMemorisingBinding.inflate(inflater)

        return fragBinding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = Memorising()
    }
}