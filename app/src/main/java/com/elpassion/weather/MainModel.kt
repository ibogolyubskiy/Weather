package com.elpassion.weather

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.elpassion.weather.data.Repository
import com.elpassion.weather.utils.getFreshCharts
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainModel : ViewModel() {

    private val cache = HashMap<String, List<Chart>>()

    val city = MutableLiveData<String>().apply { value = "" }
    val charts = MutableLiveData<List<Chart>>().apply { value = emptyList() }
    val loading = MutableLiveData<Boolean>().apply { value = false }
    val message = MutableLiveData<String>()

    init {
        selectCity("Moscow")
    }

    private var action: Job? = null

    fun selectCity(city: String): Boolean {
        action?.cancel()
        action = GlobalScope.launch(Dispatchers.Main) {
            this@MainModel.city.value = city
            loading.value = true
            try {
                charts.value = cache.getFreshCharts(city) ?: getNewCharts(city)
            }
            catch (e: CancellationException) { }
            catch (e: Exception) {
                message.value = e.toString()
                message.value = null
                charts.value = emptyList()
            }
            loading.value = false
        }
        return action!!.start()
    }

    override fun onCleared() {
        action?.cancel()
    }

    /**
     * @throws IllegalStateException
     */
    private suspend fun getNewCharts(city: String) = Repository.getCityCharts(city).also { cache[city] = it }
}

