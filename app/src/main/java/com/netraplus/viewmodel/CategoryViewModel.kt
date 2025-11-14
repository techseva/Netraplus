package com.netraplus.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.netraplus.data.CategoryRepository
import com.netraplus.data.ServiceItem
import java.util.concurrent.Executors

class CategoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = CategoryRepository(app)
    private val executor = Executors.newSingleThreadExecutor()

    private val _items = MutableLiveData<List<ServiceItem>>()
    val items: LiveData<List<ServiceItem>> = _items

    fun load(category: String) {
        executor.execute {
            val data = repo.load(category)
            _items.postValue(data)
        }
    }
}
