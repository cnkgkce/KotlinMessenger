package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

    }


    fun login(view:View){
        val email = mEmailText.text.toString()
        var password = mPasswordText.text.toString()
        val auth = FirebaseAuth.getInstance()

        if(email.isEmpty() || password.isEmpty()){
            return
        }else{
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if(task.isComplete && task.isSuccessful){
                    Toast.makeText(applicationContext,"Welcome",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this,LatestMessagesActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }.addOnFailureListener { exception ->
                if(exception != null){
                    Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
                }
            }

        }

    }

    fun backToRegister(view:View){
        val intent = Intent(this,RegistrationActivity::class.java)
        startActivity(intent)
    }

}
