package com.example.simple_english

import android.content.Intent
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.simple_english.data.Constants
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.data.User
import com.example.simple_english.lib.HttpsRequests
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainMenuTest {
    private val activityContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val setupIntent = Intent(activityContext, MainMenu::class.java).apply {
        putExtra("user", User(
            username = "ex@gmail.com",
            password = "yexLoNmMHY9Y6nSPXDwhjA==",
            secretWord = "roflan",
            secretWordType = "Другое",
            completedTasks = intArrayOf(-1),
            startedMemories = intArrayOf(160,162,164,192,194,196,200,202,204,206,208,210,212,214,216,218,220,222,224,226,228,230,236,257,267,269,271,273,282,299,340,342,344),
            id = 126,
            name = "Кирилл",
            XP = 4937
        ))
    }

    @get:Rule
    val activityRule: ActivityScenarioRule<MainMenu> = ActivityScenarioRule(setupIntent)

    @get:Rule
    val mainActivityRule = IntentsRule()

    @Test
    fun mainViewsDisplayed() {
        onView(withId(R.id.theory_card)).check(matches(isDisplayed()))
        onView(withId(R.id.audio_card)).check(matches(isDisplayed()))
        onView(withId(R.id.insert_words_card)).check(matches(isDisplayed()))
        onView(withId(R.id.reading_card)).check(matches(isDisplayed()))
        onView(withId(R.id.menu_image)).check(matches(isDisplayed()))
    }

    @Test
    fun buttonsAreClickable() {
        onView(withId(R.id.theory_card)).check(matches(isClickable()))
        onView(withId(R.id.audio_card)).check(matches(isClickable()))
        onView(withId(R.id.insert_words_card)).check(matches(isClickable()))
        onView(withId(R.id.reading_card)).check(matches(isClickable()))
        onView(withId(R.id.menu_image)).check(matches(isClickable()))
    }

    private fun commonCheckForTasks(cardId: Int, learningType: String) = runTest {
        onView(withId(cardId)).perform(click())
        intended(hasComponent(Learning::class.java.name))

        onView(withId(R.id.fragmentContainer)).check { view, _ -> (view as FragmentContainerView).getFragment<ChooseTask>() }

        var jsonTasks = ""
        launch { jsonTasks = HttpsRequests().sendAsyncRequest("/find_task_headers_by_type", mapOf("type" to learningType), HttpMethods.POST) }
        advanceUntilIdle()

        assert(jsonTasks.isNotEmpty())
        onView(withId(R.id.optionsRecycle)).check { view, _ -> (view as RecyclerView).childCount > 0 }
    }

    @Test
    fun theoryTasksDisplayed() {
        commonCheckForTasks(R.id.theory_card, Constants.theory)
    }

    @Test
    fun readingTasksDisplayed() {
        commonCheckForTasks(R.id.reading_card, Constants.reading)
    }

    @Test
    fun insertWordsTasksDisplayed() {
        commonCheckForTasks(R.id.insert_words_card, Constants.insertWords)
    }

    @Test
    fun audioTasksDisplayed() {
        commonCheckForTasks(R.id.audio_card, Constants.audio)
    }

    @Test
    fun navigationWorks() {
        onView(withId(R.id.menu_image)).perform(click())
        onView(withId(R.id.navigation)).check(matches(isDisplayed()))
    }

    @Test
    fun memorisingDisplayed() {
        onView(withId(R.id.menu_image)).perform(click())
        commonCheckForTasks(R.id.memorising, Constants.memorising)
    }

    @Test
    fun translatorDisplayed() {
        onView(withId(R.id.menu_image)).perform(click())
        onView(withId(R.id.translator)).perform(click())

        intended(hasComponent(Learning::class.java.name))

        onView(withId(R.id.fragmentContainer)).check { view, _ -> (view as FragmentContainerView).getFragment<Translator>() }
    }

    @Test
    fun statisticsDisplayed() {
        onView(withId(R.id.menu_image)).perform(click())
        onView(withId(R.id.statistics)).perform(click())

        intended(hasComponent(Learning::class.java.name))

        onView(withId(R.id.fragmentContainer)).check { view, _ -> (view as FragmentContainerView).getFragment<Statistics>() }
    }
}
