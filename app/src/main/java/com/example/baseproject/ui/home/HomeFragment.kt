package com.example.baseproject.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.Constant
import com.example.baseproject.R
import com.example.baseproject.databinding.FragmentHomeBinding
import com.example.baseproject.model.Account
import com.example.baseproject.model.Car
import com.example.baseproject.navigation.AppNavigation
import com.example.core.base.BaseFragment
import com.example.core.utils.toastMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(R.layout.fragment_home) {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: HomeViewModel by viewModels()

    private var phoneNumber = ""

    private lateinit var mediaPlayer: MediaPlayer

    private val callPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                toastMessage("Bạn phải cấp quyền để thực hiện cuộc gọi")
            }
        }

    override fun getVM() = viewModel

    private lateinit var car: Car

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        mediaPlayer = MediaPlayer.create(
            requireContext(),
            Uri.parse(
                "android.resource://"
                        + requireActivity().packageName + "/" + R.raw.warning
            )
        )
        binding.ivCall.apply {
            alpha = 0.5F
            isEnabled = false
        }

        binding.ivStopCar.setOnClickListener {
            lifecycleScope.launch {
                FirebaseDatabase.getInstance().getReference(Constant.RealtimeDBKey.CAR)
                    .child("state").setValue(
                        if (car.state == 0) 1 else 0
                    )
            }
        }

        firebaseConfig()
    }

    override fun setOnClick() {
        super.setOnClick()

        binding.ivCall.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                return@setOnClickListener
            }

            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        }
    }

    private fun firebaseConfig() {
        FirebaseDatabase.getInstance().getReference(Constant.RealtimeDBKey.CAR)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(Car::class.java) ?: return
                    car = value

                    handleAncolNoti(car)

                    if (value.state == 0) {
                        binding.viewStop.visibility = View.VISIBLE
                        binding.ivStopCar.setPadding(20, 20, 20, 20)
                    } else {
                        binding.viewStop.visibility = View.GONE
                        binding.ivStopCar.setPadding(0, 0, 0, 0)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseDatabase.getInstance().getReference(Constant.RealtimeDBKey.ACCOUNT)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(Account::class.java) ?: return
                    phoneNumber = value.phone_number
                    if (phoneNumber.isNotBlank()) {
                        binding.ivCall.apply {
                            alpha = 1F
                            isEnabled = true
                        }
                    } else {
                        binding.ivCall.apply {
                            alpha = 0.5F
                            isEnabled = false
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun handleAncolNoti(car: Car) {
        binding.tvInformation.visibility = View.VISIBLE
        binding.tvInformation.text =
            getString(
                R.string.information,
                car.name,
                car.alcohol_concentration,
                if (car.alcohol_concentration >= 250) "Xấu" else "Tốt"
            )
        binding.tvInformation.isActivated = car.alcohol_concentration >= 250

        if (car.alcohol_concentration < 250) {
            if (mediaPlayer.isPlaying) mediaPlayer.pause()
        } else {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            }
        }
    }
}