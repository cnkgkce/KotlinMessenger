package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_messages.view.*

class NewMessageActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"
        fetchUsers()
    }

    companion object{
        val INTENT_KEY = "Intent key "
    }

    private fun fetchUsers() {
        val reference = FirebaseDatabase.getInstance().reference.child("/users")
        reference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(datasnapshot :  DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()
                datasnapshot.children.forEach{ // it diye verilen şey datasnapshot yani tüm verilerdir 
                    //datasnapshot.children() --> metodu bir liste verir..forEach() metodu ise hepsinin üstünden geçer
                    Log.d("Datasnapshot",it.toString())
                    //Şimdi bunları listede göstermemiz gerekiyor
                    val user = it.getValue(UserClass::class.java)
                    if(user != null && user.uid != FirebaseAuth.getInstance().uid){
                        adapter.add(UserItem(user))
                    }
                }
                adapter.setOnItemClickListener { item, view ->
                    val intent = Intent(this@NewMessageActivity,ChatLogActivity::class.java)
                    val userItem = item as UserItem
                    intent.putExtra(INTENT_KEY,userItem.user)
                    startActivity(intent)
                }
                newMessageRecycler.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        })
    }

    class UserItem(val user: UserClass) : Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
           return R.layout.user_row_new_messages
        }

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.userNameText.setText(user.userName)
            Picasso.get().load(user.userImageUrl).into(viewHolder.itemView.new_messag_imageView)
        }

    }
}
