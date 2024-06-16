package com.and

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnChildScrollUpCallback
import com.and.adpater.TimeLineListAdapter
import com.and.databinding.FragmentCalendarBinding
import com.and.datamodel.TimeLineDataModel
import com.and.dialogfragment.WriteDialogFragment
import com.and.setting.DayDecorator
import com.and.setting.NetworkManager
import com.and.setting.SaturdayDecorator
import com.and.setting.SelectedMonthDecorator
import com.and.setting.SundayDecorator
import com.and.setting.TimeLineDayDecorator
import com.and.viewModel.UserDataViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private var selectedDay = CalendarDay.today()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.currentPage = "Calendar"
        if (!NetworkManager.checkNetworkState(requireContext())) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("인터넷을 연결해주세요!")
            builder.setPositiveButton("네", null)
            builder.setCancelable(false)
            builder.show()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)

        val dayDecorator = DayDecorator(requireContext())
        val sundayDecorator = SundayDecorator()
        val saturdayDecorator = SaturdayDecorator()
        var selectedMonthDecorator = SelectedMonthDecorator(CalendarDay.today().month)
        var timeLineDayDecorator = TimeLineDayDecorator(getTimeLineDay())


        binding.apply {
            refresh.apply {
                this.setOnRefreshListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        userDataViewModel.observeUser()
                    }
                }
                userDataViewModel.successGetData.observe(requireActivity()) {
                    refresh.isRefreshing = false
                }
            }

            TimeLineCalendar.isPagingEnabled = false

            val timeLineListAdapter = TimeLineListAdapter()
            timeLineListAdapter.setOnLongClickListener = TimeLineListAdapter.SetOnLongClickListener {
                if (!NetworkManager.checkNetworkState(requireContext())) {
                    return@SetOnLongClickListener
                }

                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("삭제하시겠습니까?")
                val listener = DialogInterface.OnClickListener { _, ans ->
                    when (ans) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val day = "${selectedDay.year}-${String.format("%02d", selectedDay.month)}-${String.format("%02d", selectedDay.day)}"
                            userDataViewModel.removeTimeLine(day, it)
                        }
                    }
                }
                builder.setPositiveButton("네", listener)
                builder.setNegativeButton("아니오", null)
                builder.show()
            }

            val today = getDayText(selectedDay)
            var timeLineOfToday = userDataViewModel.getTimeLine(today)
            TimeLineRecyclerview.adapter = timeLineListAdapter
            timeLineListAdapter.submitList(timeLineOfToday)

            addTimeLineBtn.setOnClickListener {
                if (!NetworkManager.checkNetworkState(requireContext())) {
                    return@setOnClickListener
                }
                val writeDialogFragment = WriteDialogFragment()
                writeDialogFragment.clickYesListener = WriteDialogFragment.OnClickYesListener {
                    selectedDay = CalendarDay.today()
                    TimeLineCalendar.selectedDate = selectedDay
                    val day = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                    val timeLineDataModel = TimeLineDataModel(time, System.currentTimeMillis(), it)
                    userDataViewModel.addTimeLine(day, timeLineDataModel)
                }
                writeDialogFragment.show(requireActivity().supportFragmentManager, "timeLine")
            }

            TimeLineCalendar.apply {
                selectedDate = selectedDay
                setWeekDayFormatter(ArrayWeekDayFormatter(resources.getTextArray(R.array.custom_weekdays)))
                state().edit().setMaximumDate(CalendarDay.today()).commit() // 최대 날짜 설정

                addDecorators(
                    dayDecorator,
                    saturdayDecorator,
                    sundayDecorator,
                    selectedMonthDecorator,
                    timeLineDayDecorator
                )

                setTitleFormatter { day -> // 년 월 표시 변경
                    val inputText = day.date
                    val calendarHeaderElements = inputText.toString().split("-").toMutableList()
                    val calendarHeaderBuilder = StringBuilder()
                    if (calendarHeaderElements[1][0] == '0') {
                        calendarHeaderElements[1] = calendarHeaderElements[1].replace("0", "")
                    }
                    calendarHeaderBuilder.append(calendarHeaderElements[0]).append("년 ")
                        .append(calendarHeaderElements[1]).append("월")
                    calendarHeaderBuilder.toString()
                }

                setOnMonthChangedListener { _, date -> // 달 바꿀때
                    val adapter = TimeLineRecyclerview.adapter as TimeLineListAdapter
                    TimeLineCalendar.removeDecorators()
                    TimeLineCalendar.invalidateDecorators() // 데코 초기화
                    if (date.month == CalendarDay.today().month) {
                        TimeLineCalendar.selectedDate =
                            CalendarDay.today() // 현재 달로 바꿀 때 마다 현재 날짜 표시
                        timeLineOfToday = userDataViewModel.getTimeLine(today)
                        adapter.submitList(timeLineOfToday)
                    } else {
                        TimeLineCalendar.selectedDate = null
                        adapter.submitList(mutableListOf())
                    }

                    selectedMonthDecorator = SelectedMonthDecorator(date.month)
                    timeLineDayDecorator = TimeLineDayDecorator(getTimeLineDay())
                    TimeLineCalendar.addDecorators(
                        dayDecorator,
                        saturdayDecorator,
                        sundayDecorator,
                        selectedMonthDecorator,
                        timeLineDayDecorator
                    ) //데코 설정
                }

                setOnDateChangedListener { _, date, _ -> // 날짜 킅릭시
                    selectedDay = date
                    val day = getDayText(selectedDay)
                    val timeLineOfDay = userDataViewModel.getTimeLine(day)
                    val adapter = TimeLineRecyclerview.adapter as TimeLineListAdapter
                    adapter.submitList(timeLineOfDay.toMutableList())
                }
            }

            userDataViewModel.timeLineInfos.observe(requireActivity()) {
                val day = getDayText(selectedDay)
                val adapter = TimeLineRecyclerview.adapter as TimeLineListAdapter
                adapter.submitList(it[day]?.toMutableList() ?: mutableListOf())
                TimeLineCalendar.removeDecorator(timeLineDayDecorator)
                timeLineDayDecorator = TimeLineDayDecorator(getTimeLineDay())
                TimeLineCalendar.addDecorators(timeLineDayDecorator)
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getTimeLineDay(): MutableList<CalendarDay> {
        val keys = userDataViewModel.timeLineInfos.value?.keys ?: mutableSetOf()
        val timeLineDays = mutableListOf<CalendarDay>()
        for (key in keys) {
            val dayInfo = key.split("-")
            val calendarDay = CalendarDay.from(dayInfo[0].toInt(), dayInfo[1].toInt(), dayInfo[2].toInt())
            timeLineDays.add(calendarDay)
        }

        return timeLineDays
    }

    private fun getDayText(selectedDay: CalendarDay) : String {
        return "${selectedDay.year}-${String.format("%02d", selectedDay.month)}-${String.format("%02d", selectedDay.day)}"
    }
}