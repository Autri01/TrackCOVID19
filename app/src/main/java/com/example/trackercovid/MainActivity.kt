package com.example.trackercovid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    lateinit var stateAdapter: StateAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        list.addHeaderView(LayoutInflater.from(this).inflate(R.layout.item__header,list,false))
        fetchResult()

    }

    private fun fetchResult() {
        GlobalScope.launch {
                        val response = withContext(Dispatchers.IO) { Client.api.execute() }
                        if(response.isSuccessful){
                            val data=Gson().fromJson(response.body?.string(),Response::class.java)
                            launch (Dispatchers.Main){
                            bindCombinedData(data.statewise[0])
                    bindStateWiseData(data.statewise.subList(1,data.statewise.size))
                }
            }
        }
    }

    private fun bindStateWiseData(subList: List<StatewiseItem?>) {
        stateAdapter= StateAdapter(subList as List<StatewiseItem>)
        list.adapter=stateAdapter
    }

    private fun bindCombinedData(data: StatewiseItem?) {
        val lastUpdatedTime=data?.lastupdatedtime
        val simpleDateFormat=SimpleDateFormat("dd/MM/YYY HH:mm:ss")
        lastUpdatedTv.text="Last Updated\n ${getTimeAgo(simpleDateFormat.parse(lastUpdatedTime))}"
        confirmedTv.text=data?.confirmed
        recoveredTv.text=data?.recovered
        activeTv.text=data?.active
        deceasedTv.text=data?.deaths
    }

    fun getTimeAgo(past:Date):String{
        val now=Date()
        val seconds=TimeUnit.MILLISECONDS.toSeconds(now.time-past.time)
        val minutes=TimeUnit.MILLISECONDS.toMinutes(now.time-past.time)
        val hours=TimeUnit.MILLISECONDS.toHours(now.time-past.time)

        return when{
            seconds<60 -> {
                "Few seconds ago"
            }
            minutes<60 ->{
                "$minutes minutes ago"
            }
            hours<24 ->{
                "$hours hour ${minutes%60} min ago"
            }
            else -> {
                SimpleDateFormat(" HH:mm:ss").format(past).toString()
            }
        }
    }
}