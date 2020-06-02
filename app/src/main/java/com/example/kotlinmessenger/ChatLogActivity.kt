package com.example.kotlinmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_message_from_row.view.*
import kotlinx.android.synthetic.main.chat_message_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()
    var toUser : UserClass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerView_chat_log.adapter = adapter

        supportActionBar?.title = "Chat Log"
        toUser = intent.getParcelableExtra<UserClass>(NewMessageActivity.INTENT_KEY)
        supportActionBar?.title = toUser?.userName


        listenForMessages()

        button_send_chat_log.setOnClickListener {
            performSendMessage()
        }

    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val reference = FirebaseDatabase.getInstance().reference.child("/user-messages/$fromId/$toId")


        reference.addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(datasnapshot: DataSnapshot, p1: String?) {
                val chatMessage = datasnapshot.getValue(ChatMessageModelClass::class.java)
                if(chatMessage != null){
                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = LatestMessagesActivity.currentUser ?: return // burayı eklemezsek hata alırız illa önceden mesaj olmak zorunda değil
                        adapter.add(ChatFromItem(chatMessage.text!!,currentUser))
                    }else{
                        val toUser = intent.getParcelableExtra<UserClass>(NewMessageActivity.INTENT_KEY)
                        adapter.add(ChatToItem(chatMessage.text!!,toUser!!))
                    }
                }

                recyclerView_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }

    class ChatFromItem(val text: String,val user:UserClass) : Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
          return R.layout.chat_message_from_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.chatTextFromRow.setText(text)
            Picasso.get().load(user.userImageUrl).into(viewHolder.itemView.chat_profile_from_imageview)
        }
    }

    class ChatToItem(val text:String,val user:UserClass) : Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.chat_message_to_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.chatTextToRow.setText(text)
            Picasso.get().load(user.userImageUrl).into(viewHolder.itemView.chat_profile_to_imageview)
        }

    }

    private fun performSendMessage() {
        val text = editText_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<UserClass>(NewMessageActivity.INTENT_KEY)
        val toId= user?.uid
        val reference = FirebaseDatabase.getInstance().reference.child("/user-messages/$fromId/$toId").push()


        val chatMessage = ChatMessageModelClass(reference.key.toString(),text,fromId,toId,System.currentTimeMillis() / 1000)

        val toReference = FirebaseDatabase.getInstance().reference.child("/user-messages/$toId/$fromId").push()


        reference.setValue(chatMessage).addOnCompleteListener {
            if(it.isSuccessful && it.isComplete){
                Log.d("Tag","Database e yükleme başarılı")
                editText_chat_log.text.clear()
                recyclerView_chat_log.scrollToPosition(adapter.itemCount - 1) // Mesaj yazdıktan sonra otomatik olarak aşağı kayması için
            }
        }
        toReference.setValue(chatMessage)

        val latestmessageref = FirebaseDatabase.getInstance().reference.child("/latest-messages/$fromId/$toId")
        latestmessageref.setValue(chatMessage)

        val latestmessageToref = FirebaseDatabase.getInstance().reference.child("/latest-messages/$toId/$fromId")
        latestmessageToref.setValue(chatMessage)

    }
}
