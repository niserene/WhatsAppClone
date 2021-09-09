package com.nishantdev961.whatsappclone.auth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.nishantdev961.whatsappclone.MainActivity
import com.nishantdev961.whatsappclone.R
import com.nishantdev961.whatsappclone.models.User
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity(), View.OnClickListener{

    val storage by lazy{
        FirebaseStorage.getInstance()
    }

    val auth by lazy{
        FirebaseAuth.getInstance()
    }

    val database by lazy {
        FirebaseFirestore.getInstance()
    }

    private lateinit var downLoadUrl: String
    private lateinit var uploadUrl: Uri
    private lateinit var showProgress: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        userImageview.setOnClickListener{
            checkPermissionForImage()
        }
        nextBtn.setOnClickListener(this)
        uploadBtn.setOnClickListener(this)
    }

    private fun uploadUserData(name: String){

        nextBtn.isEnabled = false
        showProgress = this.createProgressDialog("Creating the new user Profile",false)
        showProgress.show()

        val user = User(name, downLoadUrl, downLoadUrl, auth.uid.toString())

        database.collection("users")
                .document(auth.uid!!).set(user)
                .addOnSuccessListener {

                    showProgress.dismiss()
                    startActivity(
                            Intent(this, MainActivity::class.java)
                    )
                    finish()
                }
                .addOnFailureListener{
                    showProgress.dismiss()
                    nextBtn.isEnabled = true
                }
    }

    private fun uploadUserImage(imageUri: Uri) {

        nextBtn.isEnabled = false

        showProgress = this.createProgressDialog("Uploading the Image...", false)
        showProgress.show()

        val ref = storage.reference.child("uploads/"+auth.uid.toString())

        val uploadTask = ref.putFile(imageUri)

        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>>{ task->
            if(!task.isSuccessful){
                task.exception?.let {
                    showProgress.dismiss()
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener{task->

            if(task.isSuccessful){
                downLoadUrl = task.result.toString()
                Toast.makeText(this,"Image Uploaded successfully :)", Toast.LENGTH_LONG).show()
                Log.d("UPLOAD", downLoadUrl)
            }
            nextBtn.isEnabled = true
            showProgress.dismiss()

        }.addOnFailureListener{task->
            showProgress.dismiss()
            Log.d("UPLOAD", "fail ho gya")
        }
    }

    @SuppressLint("NewApi")
    private fun checkPermissionForImage() {

        val readPermission = checkSelfPermission( Manifest.permission.READ_EXTERNAL_STORAGE)
        val writePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if(PackageManager.PERMISSION_GRANTED == readPermission && PackageManager.PERMISSION_GRANTED == writePermission){
            Toast.makeText(this, "hello user", Toast.LENGTH_LONG).show()
            pickImageFromGallery()
        }
        else {
            val rPermission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

            requestPermissions(
                    rPermission,
                    1231
            )
        }
    }

    private fun pickImageFromGallery() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
                intent,
                1000
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if(resultCode == Activity.RESULT_OK && requestCode == 1000){
            intent?.data?.let {
                userImageview.setImageURI(it)
                uploadUrl = it
            }
        }
    }

    override fun onClick(view: View) {

        when(view.id){
            R.id.nextBtn ->{
                val name = usernameText.text.toString()
                if(name.isEmpty()){
                    Toast.makeText(this, "Name cannot be Empty", Toast.LENGTH_LONG).show()
                }else if(!::downLoadUrl.isInitialized){
                    Toast.makeText(this, "Image cannot be empty", Toast.LENGTH_LONG).show()
                }
                else{
                    uploadUserData(name)
                }
            }
            R.id.uploadBtn ->{
                if(!::uploadUrl.isInitialized){
                    Toast.makeText(this, "Image not chosen", Toast.LENGTH_LONG).show()
                }
                else{
                    uploadUserImage(uploadUrl)
                }
            }
        }
    }

    fun Context.createProgressDialog(message:String, isCancellable: Boolean): ProgressDialog {

        return ProgressDialog(this).apply{
            setMessage(message)
            setCancelable(isCancellable)
            setCanceledOnTouchOutside(false)
        }
    }
}