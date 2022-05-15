package com.example.baseproject.navigation

import android.os.Bundle
import com.example.core.navigationComponent.BaseNavigator

interface AppNavigation : BaseNavigator {

    fun splashToPairFragment(bundle: Bundle? = null)

    fun pairToHomeFragment(bundle: Bundle? = null)

    fun splashToHomeFragment(bundle: Bundle? = null)

}