package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessagesActivity : AppCompatActivity() {

    companion object{  // globaldir burada oluşturulan bu objeye Chatlogactivityde kullanacağız
        var currentUser : UserClass? = null
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        //  setDummyRows()
        latestrecyclerView.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        latestrecyclerView.adapter = adapter

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this,ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.INTENT_KEY,row.chatPartnerUser)
            startActivity(intent)

        }

        fetchCurrentUser()
        listenForLatestMessages()

    }



    private class LatestMessageRow(val chatmessage : ChatMessageModelClass) : Item<GroupieViewHolder>(){
        var chatPartnerUser : UserClass? = null

        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
          viewHolder.itemView.latest_message.text = chatmessage.text
            val chatPartnerId:String
            if(chatmessage.fromId == FirebaseAuth.getInstance().uid){
                chatPartnerId = chatmessage.toId!!
            }else{
                chatPartnerId = chatmessage.fromId!!
            }

            val ref = FirebaseDatabase.getInstance().reference.child("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(datasnapshot: DataSnapshot) {
                    chatPartnerUser = datasnapshot.getValue(UserClass::class.java) ?: return
                            viewHolder.itemView.latest_user_name.text = chatPartnerUser?.userName
                            Picasso.get().load(chatPartnerUser?.userImageUrl).into(viewHolder.itemView.latest_imageview)
                }

            })

        }

    }


    val latestMessageMap = HashMap<String,ChatMessageModelClass>()
    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessageMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }


    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().reference.child("/latest-messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener{

            override fun onChildAdded(datasnapshot: DataSnapshot, p1: String?) {
                val message = datasnapshot.getValue(ChatMessageModelClass::class.java) ?:return
                latestMessageMap[datasnapshot.key!!] = message
                refreshRecyclerViewMessages()

            }

            override fun onChildChanged(datasnapshot: DataSnapshot, p1: String?) {
                val message = datasnapshot.getValue(ChatMessageModelClass::class.java)?: return
                latestMessageMap[datasnapshot.key!!] = message
                refreshRecyclerViewMessages()
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}

        })

    }

    /*private fun setDummyRows(){
        // sadece test için yapılmış bir metottur kukla row build edilir
        val adapter = GroupAdapter<GroupieViewHolder>()

        adapter.add(LatestMessageRow("Ayşe","naber","https://cdn.psychologytoday.com/sites/default/files/styles/image-article_inline_full/public/field_blog_entry_images/2018-09/shutterstock_648907024.jpg?itok=ji6Xj8tv"))
        adapter.add(LatestMessageRow("Fatma","hey","https://vdz2wnf1.rocketcdn.com/upload/img/680x0/20-05/12/mursel-hakkinda-bilgi-620x350.jpg?0.052035440293835444"))


        latestrecyclerView.adapter = adapter


    } */


    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(datasnapshot: DataSnapshot) {
                currentUser = datasnapshot.getValue(UserClass::class.java)
            }

        })

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       if(item.itemId == R.id.sign_out){
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this,RegistrationActivity::class.java)
            startActivity(intent)
            finish()
       }
       else{
           val intent = Intent(this,NewMessageActivity::class.java)
           startActivity(intent)
       }
        return super.onOptionsItemSelected(item)
    }
}
