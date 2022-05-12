package com.example.baseproject.ui.home

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.baseproject.R
import com.example.baseproject.databinding.FragmentHomeBinding
import com.example.baseproject.navigation.AppNavigation
import com.example.core.base.BaseFragment
import com.example.core.service.AIService
import com.example.core.utils.toastMessage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>(R.layout.fragment_home) {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: HomeViewModel by viewModels()

    private val audioPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
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
                        binding.ivMic.isActivated = false
                        Timber.tag("hihi").e("message: $message")
                    }

                    AIService.ResultAction.ERROR.value -> {
                        val errorCode = intent.getStringExtra(AIService.ARG_DATA)!!
                        binding.ivMic.isActivated = false
                        Timber.tag("hihi").e("error: $errorCode")
                    }

                    AIService.ResultAction.BEGIN.value -> {
                        binding.ivMic.isActivated = true
                    }
                }
            }
        }
    }

    override fun getVM() = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        viewModel.connect()

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
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(aiBroadcastReceiver, intentFilter)
    }

    override fun setOnClick() {
        super.setOnClick()

        binding.ivMic.setOnClickListener {
            startAIService(AIService.Action.START_RECORD.value)
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

}