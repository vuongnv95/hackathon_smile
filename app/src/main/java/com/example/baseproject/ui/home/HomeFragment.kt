package com.example.baseproject.ui.home

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.baseproject.Constant
import com.example.baseproject.R
import com.example.baseproject.databinding.FragmentHomeBinding
import com.example.baseproject.navigation.AppNavigation
import com.example.core.base.BaseFragment
import com.example.core.service.AIService
import com.example.core.utils.toastMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(R.layout.fragment_home) {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: HomeViewModel by viewModels()

    private var alcoholConcentration = "0"
    private var carName = ""
    private var phoneNumber = ""

    private val audioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                toastMessage("Bạn phải cấp quyền để sử dụng chức năng ra lệnh bằng giọng nói")
            }
        }

    private val aiBroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    AIService.ResultAction.NEW_MESSAGE.value -> {
                        val message = intent.getStringExtra(AIService.ARG_DATA)!!
                        viewModel.sendMessage(message)
//                        binding.ivMic.isActivated = false
                        Timber.tag("hihi").e("message: $message")
                    }

                    AIService.ResultAction.ERROR.value -> {
                        val errorCode = intent.getStringExtra(AIService.ARG_DATA)!!
//                        binding.ivMic.isActivated = false
                        Timber.tag("hihi").e("error: $errorCode")
                    }

                    AIService.ResultAction.BEGIN.value -> {
//                        binding.ivMic.isActivated = true
                    }
                }
            }
        }
    }

    override fun getVM() = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        viewModel.connect()

        binding.ivCall.apply {
            alpha = 0.5F
            isEnabled = false
        }

        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            toastMessage("Thiết bị không hỗ trợ tính năng ra lệnh bằng giọng nói!")
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        val intentFilter = IntentFilter().apply {
            addAction(AIService.ResultAction.ERROR.value)
            addAction(AIService.ResultAction.NEW_MESSAGE.value)
            addAction(AIService.ResultAction.BEGIN.value)
        }
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(aiBroadcastReceiver, intentFilter)

        firebaseConfig()
    }

    override fun setOnClick() {
        super.setOnClick()

//        binding.ivMic.setOnClickListener {
//            startAIService(AIService.Action.START_RECORD.value)
//        }

        binding.ivCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            startActivity(intent)
        }
    }

    override fun bindingStateView() {
        super.bindingStateView()

        viewModel.serverMessageResponse.observe(viewLifecycleOwner) { message ->
            Timber.tag("hihi").e("AI response: $message")
        }
    }

    override fun onStop() {
        startAIService(AIService.Action.STOP_RECORD.value)
        super.onStop()
    }

    override fun onDestroyView() {
        viewModel.disconnect()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(aiBroadcastReceiver)
        super.onDestroyView()
    }

    private fun startAIService(action: String) {
        requireContext().startService(Intent(requireContext(), AIService::class.java).apply {
            this.action = action
        })
    }

    private fun firebaseConfig() {
        FirebaseDatabase.getInstance().getReference(Constant.RealtimeDBKey.ALCOHOL_CONCENTRATION)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(String::class.java) ?: return
                    alcoholConcentration = value
                    updateInformation()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseDatabase.getInstance().getReference(Constant.RealtimeDBKey.CAR_NAME)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(String::class.java) ?: return
                    carName = value
                    updateInformation()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseDatabase.getInstance().getReference(Constant.RealtimeDBKey.PHONE_NUMBER)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(String::class.java) ?: return
                    phoneNumber = value
                    if (phoneNumber.isNotBlank()) {
                        binding.ivCall.apply {
                            alpha = 1F
                            isEnabled = true
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun updateInformation() {
        if (carName.isNotBlank() && alcoholConcentration.isNotBlank()) {
            binding.tvInformation.visibility = View.VISIBLE
            binding.tvInformation.text =
                getString(R.string.information, carName, alcoholConcentration)
        }
    }

}