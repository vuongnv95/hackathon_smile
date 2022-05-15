package com.example.baseproject.ui.splash

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.example.baseproject.R
import com.example.baseproject.databinding.FragmentSplashBinding
import com.example.baseproject.navigation.AppNavigation
import com.example.core.base.BaseFragment
import com.example.core.utils.setTextCompute
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment :
    BaseFragment<FragmentSplashBinding, SplashViewModel>(R.layout.fragment_splash) {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: SplashViewModel by viewModels()

    override fun getVM() = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.text.setTextCompute("Splash")
    }

    override fun bindingAction() {
        super.bindingAction()
        viewModel.actionSplash.observe(viewLifecycleOwner) {
            when (it) {
                is SplashActionState.OpenPair -> {
                    appNavigation.splashToPairFragment()
                }

                is SplashActionState.OpenHome -> {
                    appNavigation.splashToHomeFragment()
                }
            }
        }
    }

}