package com.and

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.and.alarm.AlarmFunctions
import com.and.databinding.FragmentMypageBinding
import com.and.datamodel.DrugDataModel
import com.and.datamodel.FirebaseDbAlarmDataModel
import com.and.datamodel.RoomDbAlarmDataModel
import com.and.dialogfragment.WriteUserInfoDialogFragment
import com.and.setting.Setting
import com.and.viewModel.UserDataViewModel
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyPageFragment : Fragment() {
    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!
    private val userDataViewModel: UserDataViewModel by activityViewModels()

    private lateinit var loginInfo: android.content.SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        binding.apply {
            viewModel = userDataViewModel
            lifecycleOwner = requireActivity()

            ManageUserInfoBtn.setOnClickListener {
                val writeUserInfoDialogFragment = WriteUserInfoDialogFragment()
                writeUserInfoDialogFragment.show(requireActivity().supportFragmentManager, "writeUserInfo")
            }

            GetAlarmInfoBtn.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    if (userDataViewModel.getAlarmList().isEmpty() && (userDataViewModel.drugInfos.value ?: mutableListOf()).isNotEmpty()) {
                        settingAlarms(userDataViewModel.drugInfos.value ?: mutableListOf())
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "알람을 불러 왔어요!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "재 로그인 시 클릭해주세요!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            InquiryBtn.setOnClickListener {

            }

            PrivacyPolicyBtn.setOnClickListener {

            }

            LogoutBtn.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("로그아웃 하시겠습니까?")
                val listener = DialogInterface.OnClickListener { _, ans ->
                    when (ans) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            if (binding.userInfoEmail.text.contains("naver.com")) { // 네이버로 로그인 했을 경우
                                try {
                                    NaverIdLoginSDK.logout()
                                    goLogin()
                                    Toast.makeText(requireContext(), "로그아웃 성공", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(requireContext(), "로그아웃 실패 $e", Toast.LENGTH_SHORT).show()
                                }
                            } else if (binding.userInfoEmail.text.contains("kakao.com")) { // 카카오로 로그인 했을 경우
                                UserApiClient.instance.logout { error ->
                                    if (error != null) {
                                        Toast.makeText(requireContext(), "로그아웃 실패 $error", Toast.LENGTH_SHORT).show()
                                    } else {
                                        goLogin()
                                        Toast.makeText(requireContext(), "로그아웃 성공", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
                builder.setPositiveButton("네", listener)
                builder.setNegativeButton("아니오", null)
                builder.show()
            }

            RemoveAccountBtn.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("정말로 탈퇴 하시겠습니까?")
                val listener = DialogInterface.OnClickListener { _, ans ->
                    when (ans) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            if (binding.userInfoEmail.text.contains("naver.com")) { // 네이버로 로그인 했을 경우
                                NidOAuthLogin().callDeleteTokenApi(object : OAuthLoginCallback {
                                    override fun onError(errorCode: Int, message: String) {
                                        onFailure(errorCode, message)
                                    }

                                    override fun onFailure(httpStatus: Int, message: String) {
                                        Toast.makeText(requireContext(), "탈퇴 실패...", Toast.LENGTH_SHORT).show()
                                    }

                                    override fun onSuccess() {
                                        try {
                                            userDataViewModel.deleteInfo()
                                            goLogin()
                                            Toast.makeText(requireContext(), "탈퇴 성공...", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(requireContext(), "탈퇴 실패...", Toast.LENGTH_SHORT).show()
                                            goLogin()
                                        }
                                    }
                                })
                            } else if (binding.userInfoEmail.text.contains("kakao.com")) { // 카카오로 로그인 했을 경우
                                UserApiClient.instance.unlink { error ->
                                    if (error != null) {
                                        Toast.makeText(requireContext(), "탈퇴 실패 $error", Toast.LENGTH_SHORT).show()
                                    } else {
                                        try {
                                            userDataViewModel.deleteInfo()
                                            goLogin()
                                            Toast.makeText(requireContext(), "탈퇴 성공", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(requireContext(), "탈퇴 실패...", Toast.LENGTH_SHORT).show()
                                            goLogin()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                builder.setPositiveButton("네", listener)
                builder.setNegativeButton("아니오", null)
                builder.show()
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun goLogin() {
        removeLoginInfo()
        removeAlarms()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun removeLoginInfo() {
        loginInfo = requireActivity().getSharedPreferences("setting", AppCompatActivity.MODE_PRIVATE)
        val edit = loginInfo.edit()
        edit.remove("email")
        edit.apply()
        Setting.email = ""
    }

    private fun settingAlarms(alarms: MutableList<DrugDataModel>) {
        for (alarm in alarms) {
            settingAlarm(alarm.firstAlarm)
            settingAlarm(alarm.secondAlarm)
            settingAlarm(alarm.thirdAlarm)
        }
    }

    private fun settingAlarm(firebaseDbAlarmDataModel: FirebaseDbAlarmDataModel) {
        val alarmFunctions = AlarmFunctions(requireContext())
        val roomDbAlarmDataModel = RoomDbAlarmDataModel(firebaseDbAlarmDataModel.alarmCode, firebaseDbAlarmDataModel.code, firebaseDbAlarmDataModel.time, firebaseDbAlarmDataModel.week)
        alarmFunctions.callAlarm(roomDbAlarmDataModel.time, roomDbAlarmDataModel.alarmCode, roomDbAlarmDataModel.week.toTypedArray())
        userDataViewModel.addAlarm(roomDbAlarmDataModel)
    }

    private fun removeAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            for (alarm in userDataViewModel.getAlarmList()) {
                removeAlarm(alarm.alarmCode)
            }
        }
    }

    private fun removeAlarm(alarmCode: Int) {
        val alarmFunctions = AlarmFunctions(requireContext())
        alarmFunctions.cancelAlarm(alarmCode)
        userDataViewModel.deleteAlarm(alarmCode)
    }
}