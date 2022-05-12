package com.example.baseproject.ui.tabFavorite

import androidx.fragment.app.viewModels
import com.example.baseproject.R
import com.example.baseproject.databinding.FragmentFavoriteBinding
import com.example.core.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoriteFragment :
    BaseFragment<FragmentFavoriteBinding, FavoriteViewModel>(R.layout.fragment_favorite) {

    private val viewModel: FavoriteViewModel by viewModels()

    override fun getVM(): FavoriteViewModel = viewModel

}