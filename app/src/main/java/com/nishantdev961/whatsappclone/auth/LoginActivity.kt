package com.nishantdev961.whatsappclone.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nishantdev961.whatsappclone.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var phoneNumber: String
    private lateinit var countryCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        phoneNumberText.addTextChangedListener {
            nextBtn.isEnabled = !(it.isNullOrEmpty()) && it.length >=10
        }

        nextBtn.setOnClickListener{
            checkNumber()
        }
    }

    private fun checkNumber() {
        countryCode = countryCodeText.selectedCountryCodeWithPlus
        phoneNumber = countryCode + phoneNumberText.text.toString()

        notifyUser()
    }

    private fun notifyUser() {

        MaterialAlertDialogBuilder(this).apply {
            setMessage(
                """ A verification code will be sent on this number: $phoneNumber
                    Recheck if the number is correct
                """.trimIndent())
            setPositiveButton("Next"){_,_ ->
                gotoVerificationActivity()
            }
            setNegativeButton("Edit Number"){dialog, which ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun gotoVerificationActivity() {

        val intent = Intent(this, VerificationCodeActivity::class.java)
        intent.putExtra(PHONE_NUMBER, phoneNumber)
        startActivity(intent)
        finish()
    }
}