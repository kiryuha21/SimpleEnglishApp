package com.example.simple_english

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.simple_english.data.HttpMethods
import com.example.simple_english.data.User
import com.example.simple_english.databinding.FragmentStatisticsBinding
import com.example.simple_english.lib.HttpsRequests
import com.example.simple_english.lib.TaskModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
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

    private val xAxisMemo = arrayListOf("Впервые", "20 минут", "12 часов", "32 часа", "5 дней", "Выполнено")
    private val xAxisUsers = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragBinding = FragmentStatisticsBinding.inflate(inflater)

        lateinit var memoChartData: BarData
        lifecycleScope.launch(Dispatchers.IO) {
            memoChartData = setMemoStat()
        }.invokeOnCompletion {
            val memoChart = fragBinding.memorisingProgress

            configChart(memoChart, IndexAxisValueFormatter(xAxisMemo))
            memoChart.data = memoChartData

            requireActivity().runOnUiThread {
                memoChart.animateY(1500, Easing.EaseInOutQuad)
            }

            memoChart.invalidate()
        }

        lateinit var usersChartData: BarData
        lifecycleScope.launch(Dispatchers.IO) {
            usersChartData = setUsersXpStat()
        }.invokeOnCompletion {
            val usersChart = fragBinding.usersXp

            configChart(usersChart, IndexAxisValueFormatter(xAxisUsers))
            usersChart.data = usersChartData

            requireActivity().runOnUiThread {
                usersChart.animateY(1500, Easing.EaseInOutQuad)
            }

            usersChart.invalidate()
        }

        return fragBinding.root
    }

    private fun configChart(chart: BarChart, formatter: IndexAxisValueFormatter) {
        chart.xAxis.apply {
            valueFormatter = formatter
            granularity = 1f
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
        }
        chart.axisLeft.axisMinimum = 0f
        chart.legend.isEnabled = false
        chart.description.isEnabled = false
    }

    private suspend fun setUsersXpStat(): BarData {
        val rawUserStat = withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            requests.sendAsyncRequest(
                "/get_all_users",
                mapOf(),
                HttpMethods.GET
            )
        }

        val entriesArray = ArrayList<BarEntry>()
        val primaryArray: ArrayList<User> = Json.decodeFromString(rawUserStat.replace("xp", "XP"))
        for (i in primaryArray.indices) {
            xAxisUsers.add(primaryArray[i].name ?: primaryArray[i].username)
            entriesArray.add(BarEntry(i.toFloat(), primaryArray[i].XP.toFloat()))
        }

        val dataset = BarDataSet(entriesArray, "Опыт пользователей")
        dataset.colors = ColorTemplate.JOYFUL_COLORS.toMutableList()

        val data = BarData(dataset)
        data.setValueTextSize(8f)

        return data
    }

    private suspend fun setMemoStat(): BarData {
        val rawMemoStat = withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                requests.sendAsyncRequest(
                    "/user_memorising",
                    mapOf("id" to taskModel.user.value!!.id.toString()),
                    HttpMethods.POST
                )
            }

        val sortMap = mutableMapOf("'0 seconds'" to 0f, "'20 minutes'" to 0f, "'12 hours'" to 0f, "'32 hours'" to 0f, "'5 days'" to 0f, "Finished" to 0f)
        val entriesArray = ArrayList<BarEntry>()
        val primaryArray = Json.parseToJsonElement(rawMemoStat).jsonArray
        for (i in primaryArray.indices) {
            val secondaryArray = primaryArray[i].jsonArray
            val key = secondaryArray[0].toString().trim('"')
            val value = secondaryArray[1].toString().toFloat()
            sortMap[key] = value
        }
        sortMap.onEachIndexed { index, entry -> entriesArray.add(BarEntry(index.toFloat(), entry.value)) }

        val dataset = BarDataSet(entriesArray, "Статус запоминания")
        dataset.colors = ColorTemplate.JOYFUL_COLORS.toMutableList()

        val data = BarData(dataset)
        data.setValueTextSize(8f)

        return data
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = Statistics()
    }
}