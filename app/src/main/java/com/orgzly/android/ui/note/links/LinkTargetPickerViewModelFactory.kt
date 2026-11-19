package com.orgzly.android.ui.note.links

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.orgzly.android.data.DataRepository

class LinkTargetPickerViewModelFactory(
    private val dataRepository: DataRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LinkTargetPickerViewModel(dataRepository) as T
    }
}
