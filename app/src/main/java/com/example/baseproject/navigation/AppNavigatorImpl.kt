package com.example.baseproject.navigation

import android.os.Bundle
import com.example.baseproject.R
import com.example.core.navigationComponent.BaseNavigatorImpl
import com.example.setting.DemoNavigation
import javax.inject.Inject

class AppNavigatorImpl @Inject constructor() : BaseNavigatorImpl(),
    AppNavigation, DemoNavigation {


    override fun splashToPairFragment(bundle: Bundle?) {
        openScreen(R.id.action_splashFragment_to_pairFragment, bundle)
    }

    override fun pairToHomeFragment(bundle: Bundle?) {
        openScreen(R.id.action_pairFragment_to_homeFragment, bundle)
    }

    override fun splashToHomeFragment(bundle: Bundle?) {
        openScreen(R.id.action_splashFragment_to_homeFragment, bundle)
    }
}