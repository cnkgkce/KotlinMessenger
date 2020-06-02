package com.example.kotlinmessenger

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_registration.*
import java.util.*

class RegistrationActivity : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth
    private var selectedPictureUri : Uri?  = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        auth = FirebaseAuth.getInstance()


    }


    fun register(view:View){
        var email = loginEmailText.text.toString()
        var password = loginPasswordText.text.toString()

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                applicationContext,
                "Please Enter your email/password",
                Toast.LENGTH_SHORT
            ).show()
            return // burayı durdurup tekrar yukarı çıkar
        }
        else{
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if(task.isSuccessful && task.isComplete){
                        uploadImageToStorage()

            }
        }.addOnFailureListener { exception ->
            if(exception != null){
                Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_SHORT).show()
                return@addOnFailureListener
            }
        }
        }
    }

    private fun uploadImageToStorage() {
        if(selectedPictureUri != null){
            val fileName = UUID.randomUUID().toString() // tanımlayıcı bir uuid
            Log.d("Main","Image is not null")
            var reference = FirebaseStorage.getInstance().reference.child("/images/$fileName")
            reference.putFile(selectedPictureUri!!).addOnCompleteListener { task->
                reference.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(it.toString()) // Bu şekilde imagein url ini yani indirme yolunu da databasee string olarka kaydediyoruz
                    Log.d("Main","Image işlemi başarılı")
                }
            }.addOnFailureListener { exception ->
                if(exception != null){
                    Log.d("Main","Image işlemi başarısız")

                }
            }
        }else{
            Log.d("Main","image null")
            return }
    }

    private fun saveUserToFirebaseDatabase(userImageUrl: String) {
    val uid = FirebaseAuth.getInstance().uid ?:""
    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = UserClass(uid,loginUserNameText.text.toString(),userImageUrl)
    ref.setValue(user).addOnCompleteListener { task ->
        if(task.isComplete && task.isSuccessful){
            Toast.makeText(applicationContext,"Welome ${auth.currentUser?.email}",Toast.LENGTH_SHORT).show()
            val intent = Intent(this,LatestMessagesActivity::class.java)
            startActivity(intent)
        }
    }}


    fun selectImage(view:View){
        if(ContextCompat.checkSelfPermission(applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
        }
        else{
            val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent,2)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1 && permissions.size > 0){
            val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent,2)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){
          selectedPictureUri= data.data
            if(selectedPictureUri != null ){
                if(Build.VERSION.SDK_INT > 28){
                    val source = ImageDecoder.createSource(this.contentResolver,selectedPictureUri!!)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    profile_image.setImageBitmap(bitmap)
                }
                else{
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,selectedPictureUri)
                    profile_image.setImageBitmap(bitmap)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun toLoginActivity(view:View){
         val intent = Intent(this,LoginActivity::class.java)
         startActivity(intent)
    }

}
