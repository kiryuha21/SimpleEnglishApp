package com.example.simple_english

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


fun hasTextInputLayoutErrorText(expectedErrorText: String): Matcher<View?> {
    return object : TypeSafeMatcher<View?>() {
        override fun matchesSafely(view: View?): Boolean {
            if (view !is TextInputLayout) {
                return false
            }
            val error = view.error ?: return false
            val hint = error.toString()
            return expectedErrorText == hint
        }

        override fun describeTo(description: Description?) {}
    }
}


@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    private val wrongLogin = "doesnt_exist@gmail.com"
    private val wrongPassword = "12345"
    private val correctLogin = "ex@gmail.com"
    private val correctPassword = "1234"

    private val activityContext = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    val activityRule: ActivityScenarioRule<SignIn> = ActivityScenarioRule(SignIn::class.java)

    @get:Rule
    val mainActivityRule = IntentsRule()

    @Test
    fun mainViewsDisplayed() {
        onView(withId(R.id.login)).check(matches(isDisplayed()))
        onView(withId(R.id.password)).check(matches(isDisplayed()))
        onView(withId(R.id.sign_in_button)).check(matches(isDisplayed()))
        onView(withId(R.id.forgot_password_button)).check(matches(isDisplayed()))
        onView(withId(R.id.SignUpButton)).check(matches(isDisplayed()))
    }

    @Test
    fun buttonsAreClickable() {
        onView(withId(R.id.sign_in_button)).check(matches(isClickable()))
        onView(withId(R.id.forgot_password_button)).check(matches(isClickable()))
        onView(withId(R.id.SignUpButton)).check(matches(isClickable()))
    }

    @Test
    fun doNotLoginWrongUser() {
        val errorText = activityContext.getString(R.string.no_such_user)

        onView(withId(R.id.login)).perform(typeText(wrongLogin), closeSoftKeyboard())
        onView(withId(R.id.password)).perform(typeText(wrongPassword), closeSoftKeyboard())
        onView(withId(R.id.sign_in_button)).perform(click())
        onView(withId(R.id.login_layout)).check(matches(hasTextInputLayoutErrorText(errorText)))
    }

    @Test
    fun handleWrongPassword() {
        val errorText = activityContext.getString(R.string.wrong_password)

        onView(withId(R.id.login)).perform(typeText(correctLogin), closeSoftKeyboard())
        onView(withId(R.id.password)).perform(typeText(wrongPassword), closeSoftKeyboard())
        onView(withId(R.id.sign_in_button)).perform(click())
        onView(withId(R.id.password_layout)).check(matches(hasTextInputLayoutErrorText(errorText)))
    }

    @Test
    fun handleCorrectPassword() {
        onView(withId(R.id.login)).perform(typeText(correctLogin), closeSoftKeyboard())
        onView(withId(R.id.password)).perform(typeText(correctPassword), closeSoftKeyboard())
        onView(withId(R.id.sign_in_button)).perform(click())

        intended(hasComponent(MainMenu::class.java.name))
    }

    @Test
    fun goesToSignUp() {
        onView(withId(R.id.SignUpButton)).perform(click())
        intended(hasComponent(SignUp::class.java.name))
    }

    @Test
    fun goesToPasswordReset() {
        onView(withId(R.id.forgot_password_button)).perform(click())
        intended(hasComponent(PasswordReset::class.java.name))
    }
}