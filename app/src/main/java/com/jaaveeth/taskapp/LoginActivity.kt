package com.jaaveeth.taskapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Message
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
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.jaaveeth.taskapp.R

class LoginActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    private var loginEmail: EditText? = null
    private var loginPwd: EditText? = null
    private var loginBtn: Button? = null
    private var loginQn: TextView? = null
    private var mAuth: FirebaseAuth? = null
    private var loader: ProgressDialog? = null
    private var authStateListener: AuthStateListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()
        authStateListener = AuthStateListener {
            val user = mAuth!!.currentUser
            if (user != null) {
                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        toolbar = findViewById(R.id.loginToolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("Login")
        loader = ProgressDialog(this)
        loginEmail = findViewById(R.id.loginEmail)
        loginPwd = findViewById(R.id.loginPassword)
        loginBtn = findViewById(R.id.loginButton)
        loginQn = findViewById(R.id.loginPageQuestion)
        loginQn?.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
            startActivity(intent)
        })
        loginBtn?.setOnClickListener(View.OnClickListener {
            val email = loginEmail?.getText().toString().trim { it <= ' ' }
            val password = loginPwd?.getText().toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(email)) {
                loginEmail?.error = "Email is required"
                return@OnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                loginPwd?.error = "Password is required"
                return@OnClickListener
            } else {
                loader!!.setCanceledOnTouchOutside(false)
                loader!!.show()
                mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                        loader!!.dismiss()
                    } else {
                        val error = task.exception.toString()
                        Toast.makeText(this@LoginActivity, "Login failed$error", Toast.LENGTH_SHORT)
                            .show()
                        loader!!.dismiss()
                    }
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(authStateListener!!)
    }

    override fun onStop() {
        super.onStop()
        mAuth!!.removeAuthStateListener(authStateListener!!)
    }
}
