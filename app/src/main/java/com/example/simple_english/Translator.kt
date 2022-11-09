package com.example.simple_english

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.simple_english.databinding.FragmentTranslatorBinding
import me.bush.translator.Language
import me.bush.translator.Translator as BushTranslator


class Translator : Fragment() {
    private lateinit var fragBinding: FragmentTranslatorBinding
    private var currentSourceLanguage = Language.RUSSIAN
    private val translator = BushTranslator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragBinding = FragmentTranslatorBinding.inflate(inflater)

        val rotateAnim = RotateAnimation(
            0.0f, 180f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
        )
        rotateAnim.duration = 1000
        rotateAnim.fillAfter = true

        fragBinding.changeLanguages.setOnClickListener {
           it.startAnimation(rotateAnim)

            if (currentSourceLanguage == Language.RUSSIAN) {
                currentSourceLanguage = Language.ENGLISH

                fragBinding.fromLanguage.text = getText(R.string.english)
                fragBinding.toLanguage.text = getText(R.string.russian)
                fragBinding.textToTranslate.hint = getText(R.string.enter_text_en)
                fragBinding.translatedText.text = getText(R.string.translation_ru)
                fragBinding.pronunciation.text = getText(R.string.pronunciation_en)
            } else {
                currentSourceLanguage = Language.RUSSIAN

                fragBinding.fromLanguage.text = getText(R.string.russian)
                fragBinding.toLanguage.text = getText(R.string.english)
                fragBinding.textToTranslate.hint = getText(R.string.enter_text_ru)
                fragBinding.translatedText.text = getText(R.string.translation_en)
                fragBinding.pronunciation.text = getText(R.string.pronunciation_ru)
            }
        }

        fragBinding.translateButton.setOnClickListener {
            translateText()
        }

        return fragBinding.root
    }

    private fun translateText() {
        val sourceText = fragBinding.textToTranslate.text.toString()
        if (sourceText.isEmpty()) {
            Toast.makeText(context, "Введите текст для перевода!", Toast.LENGTH_SHORT).show()
            return
        }

        val translation = translator.translateBlocking(
            sourceText,
            if (currentSourceLanguage == Language.RUSSIAN) Language.ENGLISH else Language.RUSSIAN,
            currentSourceLanguage)

        fragBinding.translatedText.text = translation.translatedText
        fragBinding.pronunciation.text = translation.pronunciation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        @JvmStatic
        fun newInstance() = Translator()
    }
}