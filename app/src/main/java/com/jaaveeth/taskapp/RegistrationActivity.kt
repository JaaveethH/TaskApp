package com.jaaveeth.taskapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.jaaveeth.taskapp.R

class RegistrationActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    private var RegEmail: EditText? = null
    private var RegPwd: EditText? = null
    private var RegBtn: Button? = null
    private var RegnQn: TextView? = null
    private var mAuth: FirebaseAuth? = null
    private var loader: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_registration)
        toolbar = findViewById(R.id.RegistrationToolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Registration"
        mAuth = FirebaseAuth.getInstance()
        loader = ProgressDialog(this)
        RegEmail = findViewById(R.id.RegistrationEmail)
        RegPwd = findViewById(R.id.RegistrationPassword)
        RegBtn = findViewById(R.id.RegistrationButton)
        RegnQn = findViewById(R.id.RegistrationPageQuestion)
        RegnQn?.setOnClickListener({
            val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
            startActivity(intent)
        })
        RegBtn?.setOnClickListener(View.OnClickListener {
            val email = RegEmail?.getText().toString().trim { it <= ' ' }
            val password = RegPwd?.getText().toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(email)) {
                RegEmail?.error = "email is required"
                return@OnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                RegPwd?.error = "Password required"
                return@OnClickListener
            } else {
                loader!!.setMessage("Registration in progress")
                loader!!.setCanceledOnTouchOutside(false)
                loader!!.show()
                mAuth!!.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this@RegistrationActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                            loader!!.dismiss()
                        } else {
                            val error = task.exception.toString()
                            Toast.makeText(
                                this@RegistrationActivity,
                                "Registration failed$error",
                                Toast.LENGTH_SHORT
                            ).show()
                            loader!!.dismiss()
                        }
                    }
            }
        })
    }
}
