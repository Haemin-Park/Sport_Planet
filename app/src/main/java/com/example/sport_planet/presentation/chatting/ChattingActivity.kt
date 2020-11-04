package com.example.sport_planet.presentation.chatting

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sport_planet.R
import com.example.sport_planet.databinding.ActivityChattingBinding
import com.example.sport_planet.model.ChattingRoomListResponse
import com.example.sport_planet.model.enums.SeparatorEnum
import com.example.sport_planet.presentation.base.BaseActivity
import com.example.sport_planet.presentation.chatting.adapter.ChattingAdapter
import com.example.sport_planet.presentation.chatting.viewmodel.ChattingActivityViewModel
import kotlinx.android.synthetic.main.activity_chatting.*
import kotlinx.android.synthetic.main.item_custom_toolbar.view.*

class ChattingActivity : BaseActivity<ActivityChattingBinding>(R.layout.activity_chatting) {

    private val chattingAdapter = ChattingAdapter()
    private val chattingActivityViewModel: ChattingActivityViewModel
         by lazy {
             ViewModelProvider(this).get(ChattingActivityViewModel::class.java)
         }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val chatRoomInfo = intent.getParcelableExtra<ChattingRoomListResponse.Data>("chattingRoomInfo")

        if (chatRoomInfo != null) {
            ChattingInfo.settingChattingInfo(2, chatRoomInfo.id)
        }

        this.runOnUiThread {
            binding.toolbarActivityChatting.run {
                if (chatRoomInfo != null) {
                    if (chatRoomInfo.hostId != ChattingInfo.USER_ID)
                        binding.toolbarActivityChatting.setSeparator(SeparatorEnum.HOST)
                    else
                        binding.toolbarActivityChatting.setSeparator(SeparatorEnum.GUEST)
                    binding.toolbarActivityChatting.title.text = "달리자 88"
                }
            }
        }

        rv_activity_chatting_recyclerview.run {
            adapter = chattingAdapter
            layoutManager = LinearLayoutManager(this@ChattingActivity)
            setHasFixedSize(true)
        }

        chattingActivityViewModel.settingChattingMessageList(ChattingInfo.CHATROOM_ID)
        chattingActivityViewModel.ChattingMessageListResponseLiveData.observe(this,
            Observer {
                for(chattingMessage in it.data!!){
                    chattingAdapter.addChattingMessage(chattingMessage)
                    rv_activity_chatting_recyclerview.smoothScrollToPosition(chattingAdapter.itemCount)
                    // TODO: 추후 마지막으로 읽은 메세지 위치로(firstUnreadMessageId) 수정
                }
            }
        )

        chattingActivityViewModel.settingStomp()
        chattingActivityViewModel.ChattingMessageLiveData.observe(this,
                Observer {
                    chattingAdapter.addChattingMessage(it)
                    rv_activity_chatting_recyclerview.smoothScrollToPosition(chattingAdapter.itemCount)
                }
        )

        bt_activity_chatting_send.setOnClickListener{
            if(et_activity_chatting_message_content.length() > 0) {
                chattingActivityViewModel.sendMessage(et_activity_chatting_message_content.text.toString())
                et_activity_chatting_message_content.text.clear()
            }
        }

    }
}