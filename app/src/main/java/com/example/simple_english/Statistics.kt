package com.example.simple_english

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.core.Chart
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.data.User
import com.example.simple_english.databinding.FragmentStatisticsBinding
import com.example.simple_english.lib.HttpsRequests
import com.example.simple_english.lib.TaskModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray


class Statistics : Fragment() {
    private lateinit var fragBinding: FragmentStatisticsBinding
    private val taskModel: TaskModel by activityViewModels()
    private val requests = HttpsRequests()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragBinding = FragmentStatisticsBinding.inflate(inflater)

        lateinit var memoChart: Chart
        lifecycleScope.launch(Dispatchers.IO) {
            memoChart = setMemoStat()
        }.invokeOnCompletion {
            requireActivity().runOnUiThread {
                fragBinding.memorisingProgress.setChart(memoChart)
            }
        }

//        lateinit var usersChart: Chart
//        lifecycleScope.launch(Dispatchers.IO) {
//            usersChart = setUsersXpStat()
//        }.invokeOnCompletion {
//            fragBinding.usersXp.setChart(usersChart)
//        }

        return fragBinding.root
    }

    private suspend fun setUsersXpStat(): Chart {
        val cartesian = AnyChart.column()

        val rawUserStat = withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            requests.sendAsyncRequest(
                "/get_all_users",
                mapOf(),
                HttpMethods.GET
            )
        }

        val data = ArrayList<DataEntry>()
        val primaryArray: ArrayList<User> = Json.decodeFromString(rawUserStat)
        for (i in primaryArray) {
            data.add(ValueDataEntry(i.name ?: i.username, i.XP))
        }

        val column = cartesian.column(data)
        column.tooltip()
            .titleFormat("{%X}")
            .position(Position.CENTER_BOTTOM)
            .anchor(Anchor.CENTER_BOTTOM)
            .offsetX(0)
            .offsetY(5)

        cartesian.animation(true)
        cartesian.title("XP пользователей")

        cartesian.yScale().minimum(0.0)

        cartesian.yAxis(0).labels().format("\${%Value}{groupsSeparator: }")

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.interactivity().hoverMode(HoverMode.BY_X)

        cartesian.xAxis(0).title("Имя")
        cartesian.yAxis(0).title("XP")

        return cartesian
    }

    private suspend fun setMemoStat(): Chart {
        val cartesian = AnyChart.column()

        val rawMemoStat = withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                requests.sendAsyncRequest(
                    "/user_memorising",
                    mapOf("id" to taskModel.user.value!!.id.toString()),
                    HttpMethods.POST
                )
            }

        val data = ArrayList<DataEntry>()
        val primaryArray = Json.parseToJsonElement(rawMemoStat).jsonArray
        for (i in primaryArray) {
            val secondaryArray = i.jsonArray
            val key = secondaryArray[0].toString()
            val value = secondaryArray[1].toString().toInt()
            println("$key $value")
            data.add(ValueDataEntry(key, value))
        }

        val column = cartesian.column(data)
        column.tooltip()
            .titleFormat("{%X}")
            .position(Position.CENTER_BOTTOM)
            .anchor(Anchor.CENTER_BOTTOM)
            .offsetX(0)
            .offsetY(5)

        cartesian.animation(true)
        cartesian.title("Статус запоминания")

        cartesian.yScale().minimum(0.0)

        cartesian.yAxis(0).labels().format("\${%Value}{groupsSeparator: }")

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.interactivity().hoverMode(HoverMode.BY_X)

        cartesian.xAxis(0).title("Статус")
        cartesian.yAxis(0).title("Количество")

        return cartesian
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = Statistics()
    }
}