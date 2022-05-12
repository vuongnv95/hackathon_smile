package com.example.baseproject.ui.tabCommon

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import com.example.baseproject.R
import com.example.baseproject.databinding.FragmentCommonBinding
import com.example.core.base.BaseFragment
import com.example.core.utils.setOnSafeClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommonFragment :
    BaseFragment<FragmentCommonBinding, CommonViewModel>(R.layout.fragment_common) {

    private val viewModel: CommonViewModel by viewModels()

    override fun getVM(): CommonViewModel = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.btn.setOnSafeClickListener {
            Log.d("ahihi", ": fgbdfgb")
        }
    }
}