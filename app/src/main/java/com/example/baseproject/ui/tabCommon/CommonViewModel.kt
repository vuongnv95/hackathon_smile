package com.example.baseproject.ui.tabCommon

import android.util.Log
import com.example.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CommonViewModel @Inject constructor() : BaseViewModel() {
    init {
        Log.d("TAG", ": ")
    }
}
