package com.example.sport_planet.presentation.chatting

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sport_planet.R
import com.example.sport_planet.databinding.ActivityChattingBinding
import com.example.sport_planet.model.ChattingRoomListResponse
import com.example.sport_planet.model.enums.ApprovalStatusButtonEnum
import com.example.sport_planet.model.enums.SeparatorEnum
import com.example.sport_planet.presentation.base.BaseActivity
import com.example.sport_planet.presentation.chatting.adapter.ChattingAdapter
import com.example.sport_planet.presentation.chatting.viewmodel.ChattingActivityViewModel
import kotlinx.android.synthetic.main.activity_chatting.*
import kotlinx.android.synthetic.main.item_custom_approval_button.*
import kotlinx.android.synthetic.main.item_custom_toolbar.view.*

class ChattingActivity : BaseActivity<ActivityChattingBinding>(R.layout.activity_chatting) {

    private lateinit var chatRoomInfo: ChattingRoomListResponse.Data
    private lateinit var chattingAdapter: ChattingAdapter
    private val chattingActivityViewModel: ChattingActivityViewModel
         by lazy {
             ViewModelProvider(this).get(ChattingActivityViewModel::class.java)
         }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatRoomInfo = intent.getParcelableExtra("chattingRoomInfo")!!
        ChattingInfo.settingChattingInfo(chatRoomInfo.id)

        chattingAdapter = ChattingAdapter(chatRoomInfo)

        this.runOnUiThread {
            binding.toolbarActivityChatting.run {
                if (chatRoomInfo.hostId != ChattingInfo.USER_ID)
                    this.setSeparator(SeparatorEnum.HOST)
                else
                    this.setSeparator(SeparatorEnum.GUEST)
                this.title.text = chatRoomInfo.opponentNickname
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
                this.runOnUiThread {
                    tv_activity_chatting_board_title.text = it.boardTitle
                    bt_activity_chatting_approval_status.setApprovalStatusButton(approvalStatus(it.appliedStatus))
                    for (chattingMessage in it.data) {
                        chattingAdapter.addChattingMessage(chattingMessage)
                    }
                }
                if(it.firstUnreadMessageId == -1)
                    rv_activity_chatting_recyclerview.scrollToPosition(chattingAdapter.itemCount - 1)
                else
                    rv_activity_chatting_recyclerview.scrollToPosition(it.firstUnreadMessageId -1)

            }
        )

        chattingActivityViewModel.settingStomp()
        chattingActivityViewModel.ChattingMessageLiveData.observe(this,
                Observer {
                    chattingAdapter.addChattingMessage(it)
                    rv_activity_chatting_recyclerview.smoothScrollToPosition(chattingAdapter.itemCount -1)
                }
        )

        bt_custom_approval_button.setOnClickListener {
            when(ChattingInfo.USER_ID){
                chatRoomInfo.guestId -> chattingActivityViewModel.applyBoard(chatRoomInfo.boardId, chatRoomInfo.id)
                chatRoomInfo.hostId  -> chattingActivityViewModel.approveBoard(chatRoomInfo.boardId, chatRoomInfo.id, chatRoomInfo.guestId)
            }
        }

        bt_activity_chatting_send.setOnClickListener{
            if(et_activity_chatting_message_content.length() > 0) {
                chattingActivityViewModel.sendMessage(et_activity_chatting_message_content.text.toString())
                et_activity_chatting_message_content.text.clear()
            }
        }

    }

    override fun onDestroy() {
        chattingActivityViewModel.disposeStomp()
        super.onDestroy()
    }

    fun approvalStatus(status: String): ApprovalStatusButtonEnum{
        when(ChattingInfo.USER_ID){
            chatRoomInfo.guestId -> return when(status){
                "APPROVED" -> return ApprovalStatusButtonEnum.GUEST_APPROVE_SUCCESS
                "APPLIED"  -> ApprovalStatusButtonEnum.GUEST_APPROVE_AWAIT
                "PENDING"  -> ApprovalStatusButtonEnum.GUEST_APPLY
                else -> throw IllegalArgumentException("적절하지 않은 Guest AppliedStatus")
            }
            chatRoomInfo.hostId  -> return when(status){
                "APPROVED" -> ApprovalStatusButtonEnum.HOST_APPROVE_CANCLE
                "APPLIED"  -> ApprovalStatusButtonEnum.HOST_APPROVE
                "PENDING"  -> ApprovalStatusButtonEnum.HOST_NONE
                else -> throw IllegalArgumentException("적절하지 않은 Host AppliedStatus")
            }
            else -> throw IllegalArgumentException("적절하지 않은 User Id")
        }
    }
}