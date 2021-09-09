package com.nishantdev961.whatsappclone.auth

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.nishantdev961.whatsappclone.R
import com.nishantdev961.whatsappclone.auth.LoginActivity
import com.nishantdev961.whatsappclone.auth.SignUpActivity
import kotlinx.android.synthetic.main.activity_verification_code.*
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER = "PHONE_NUMBER"
class VerificationCodeActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var progressDialog: ProgressDialog
    var mCounterDown: CountDownTimer?=null
    var phoneNumber: String?= null
    var mVerificationId : String?=null
    var mResendToken: PhoneAuthProvider.ForceResendingToken?=null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification_code)

        initViews()
        startVerify()

    }

    private fun startVerify() {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber!!,
                60,
                TimeUnit.SECONDS,
                this,
                callbacks
        )
        progressDialog = createProgressDialog("Sending verification code", false)
        progressDialog.show()
        showTimer(60000)
    }

    private fun showTimer(timeInMills: Long) {

        resendBtn.isEnabled = false
        counterText.isVisible = true
        mCounterDown = object : CountDownTimer(timeInMills, 1000){
            override fun onTick(millisUntilFinished: Long) {
                counterText.text = getString(com.nishantdev961.whatsappclone.R.string.seconds_remaining, (millisUntilFinished/1000).toString())
            }

            override fun onFinish() {
                resendBtn.isEnabled = true
                counterText.isVisible = false
            }

        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCounterDown?.cancel()
    }

    private fun initViews(){

        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        verifyText.text = getString(com.nishantdev961.whatsappclone.R.string.verify_number, phoneNumber)
        setSpannableString()

        verifyBtn.setOnClickListener(this)
        resendBtn.setOnClickListener(this)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                val smsCode = credential.smsCode
                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }
                if(!smsCode.isNullOrBlank()){
                    sendCodeText.setText(smsCode)
                }
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {

                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }
                if(e is FirebaseAuthInvalidCredentialsException){
                    Toast.makeText(applicationContext, "Invalid Phone Number, try again", Toast.LENGTH_LONG).show()
                }
                else if(e is FirebaseTooManyRequestsException){

                }

            }

            override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken) {
                Log.d("FIRERR", "CODE SEND HUA HAI")
                mVerificationId = verificationId
                mResendToken = token
                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener{
                    if(::progressDialog.isInitialized){
                        progressDialog.dismiss()
                    }
                    if(it.isSuccessful){
                        startActivity(Intent(this , SignUpActivity::class.java))
                        finish()
                    }
                    else{
                        notifyUser("Phone Number verification failed")

                    }
                }
                .addOnFailureListener{

                }
    }

    private fun setSpannableString() {

        val span = SpannableString(getString(com.nishantdev961.whatsappclone.R.string.waiting_text, phoneNumber))
        val clickableSpan = object : ClickableSpan(){

            override fun onClick(view: View) {
                //go to the previous ie LoginActivity
                showLoginActivity()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ds.linkColor
            }
        }
        span.setSpan(clickableSpan, span.length - 14, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingText.movementMethod = LinkMovementMethod.getInstance()
        waitingText.text = span
    }

    override fun onBackPressed() {
    }

    private fun showLoginActivity(){
        val intent:Intent = Intent(this, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun notifyUser(message: String){

        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton("Ok"){_,_ ->

            }
            setNegativeButton("Cancel"){dialog, _ ->

            }
            setCancelable(false)
            create()
            show()
        }
    }

    override fun onClick(v: View) {

        when(v){
            verifyBtn ->{

                val code = sendCodeText.text.toString()
                if(code.isNotEmpty() && !mVerificationId.isNullOrBlank()) {
                    progressDialog = createProgressDialog("Please wait...", false)
                    progressDialog.show()
                    val credential:PhoneAuthCredential = PhoneAuthProvider.getCredential(mVerificationId!!, code)
                    signInWithPhoneAuthCredential(credential)
                }
                else{
                    notifyUser("You haven't written the code + $sendCodeText")
                }
            }
            resendBtn ->{
                val code: String = sendCodeText.text.toString()
                if(mResendToken!= null){
                    showTimer(60000)
                    progressDialog = createProgressDialog("Sending a verfication code again...", false)
                    progressDialog.show()

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber!!,
                            60,
                            TimeUnit.SECONDS,
                            this,
                            callbacks,
                            mResendToken
                    )
                }
            }
        }
    }
}

fun Context.createProgressDialog(message:String, isCancellable: Boolean): ProgressDialog{

    return ProgressDialog(this).apply{
        setMessage(message)
        setCancelable(isCancellable)
        setCanceledOnTouchOutside(false)
    }
}

