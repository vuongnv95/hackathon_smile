package com.example.baseproject.ui.pair

import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.Constant
import com.example.baseproject.R
import com.example.baseproject.databinding.FragmentPairBinding
import com.example.baseproject.model.Car
import com.example.baseproject.navigation.AppNavigation
import com.example.core.base.BaseFragment
import com.example.core.pref.RxPreferences
import com.example.core.utils.toastMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@AndroidEntryPoint
class PairFragment : BaseFragment<FragmentPairBinding, PairViewModel>(R.layout.fragment_pair) {

    @Inject
    lateinit var appNavigation: AppNavigation

    @Inject
    lateinit var rxPreferences: RxPreferences

    private val viewModel: PairViewModel by viewModels()

    override fun getVM(): PairViewModel = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        startAnimation()

        binding.btnPair.setOnClickListener {
            val imei = binding.edtImei.text?.toString()
            if (imei.isNullOrBlank()) return@setOnClickListener

            showHideLoading(true)
            val db = FirebaseDatabase.getInstance().getReference(Constant.RealtimeDBKey.CAR)
            db.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(Car::class.java) ?: return
                    if (imei == value.imei_alcohol) {
                        db.removeEventListener(this)
                        rxPreferences.put("imei", imei)
                        showHideLoading(false)
                        navigationToHome()
                    } else {
                        db.removeEventListener(this)
                        showHideLoading(false)
                        toastMessage("IMEI không đúng. Vui lòng thử lại")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showHideLoading(false)
                }
            })
        }
    }

    private fun startAnimation() {
        val rotate = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )

        rotate.duration = 1200
        rotate.repeatCount = Animation.INFINITE
        binding.ivProgressBar.startAnimation(rotate)
    }

    private fun navigationToHome() {
        lifecycleScope.launch(Dispatchers.Main) {
            FirebaseDatabase.getInstance().getReference(Constant.RealtimeDBKey.ACCOUNT)
                .child(Constant.RealtimeDBKey.TOKEN).setValue(getFCMToken())
            appNavigation.pairToHomeFragment()
        }
    }

    private suspend fun getFCMToken() = suspendCoroutine<String> { continuation ->
        Firebase.messaging.token.addOnCompleteListener { token -> continuation.resume(token.result) }
    }
}