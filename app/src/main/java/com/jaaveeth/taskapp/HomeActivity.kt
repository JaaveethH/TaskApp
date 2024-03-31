package com.jaaveeth.taskapp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.jaaveeth.taskapp.R
import java.text.DateFormat
import java.util.Date

class HomeActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    private var recyclerView: RecyclerView? = null
    private var floatingActionButton: FloatingActionButton? = null
    private var reference: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var mUser: FirebaseUser? = null
    private var onlineUserID: String? = null
    private var loader: ProgressDialog? = null
    private var key: String? = ""
    private var task: String? = null
    private var description: String? = null
    private var status: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        toolbar = findViewById(R.id.homeToolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Todo List App"
        mAuth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recyclerView)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.setReverseLayout(true)
        linearLayoutManager.setStackFromEnd(true)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.setLayoutManager(linearLayoutManager)
        loader = ProgressDialog(this)
        mUser = mAuth!!.currentUser
        onlineUserID = mUser!!.uid
        reference =
            FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID!!)
        floatingActionButton = findViewById(R.id.fab)
        floatingActionButton?.setOnClickListener { addTask() }
    }

    private fun addTask() {
        val myDialog = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val myView = inflater.inflate(R.layout.input_file, null)
        myDialog.setView(myView)
        val dialog = myDialog.create()
        dialog.setCancelable(false)
        val task = myView.findViewById<EditText>(R.id.task)
        val description = myView.findViewById<EditText>(R.id.description)
        val save = myView.findViewById<Button>(R.id.saveBtn)
        val cancel = myView.findViewById<Button>(R.id.CancelBtn)
        cancel.setOnClickListener { dialog.dismiss() }
        save.setOnClickListener(View.OnClickListener {
            val mTask = task.getText().toString().trim { it <= ' ' }
            val mDescription = description.getText().toString().trim { it <= ' ' }
            val id = reference!!.push().getKey()
            val date = DateFormat.getDateInstance().format(Date())
            if (TextUtils.isEmpty(mTask)) {
                task.error = "Task Required"
                return@OnClickListener
            }
            if (TextUtils.isEmpty(mDescription)) {
                description.error = "Description Required"
                return@OnClickListener
            } else {
                loader!!.setCanceledOnTouchOutside(false)
                loader!!.show()
                val model = Model(mTask, mDescription, id, date)
                Log.e("Referencce", reference!!.child(id!!).toString())
                reference!!.child(id).setValue(model).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@HomeActivity,
                            "Task has been inserted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        loader!!.dismiss()
                    } else {
                        val error = task.exception.toString()
                        Log.e("Error", error)
                        Toast.makeText(this@HomeActivity, "Failed: $error", Toast.LENGTH_SHORT)
                            .show()
                        loader!!.dismiss()
                    }
                }
            }
            dialog.dismiss()
        })
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        val options = FirebaseRecyclerOptions.Builder<Model>()
            .setQuery(reference!!, Model::class.java)
            .build()
        val adapter: FirebaseRecyclerAdapter<Model, MyViewHolder> =
            object : FirebaseRecyclerAdapter<Model, MyViewHolder>(options) {
                override fun onBindViewHolder(
                    holder: MyViewHolder,
                    @SuppressLint("RecyclerView") position: Int,
                    model: Model
                ) {
                    holder.setDate(model.date)
                    holder.setTask(model.task)
                    holder.setDesc(model.description)
                    holder.mView.setOnClickListener {
                        key = getRef(position).getKey()
                        task = model.task
                        description = model.description
                        status = model.status ?: 0
                        updateTask()
                    }
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.retrieved_layout, parent, false)
                    return MyViewHolder(view)
                }
            }
        recyclerView!!.setAdapter(adapter)
        adapter.startListening()
    }

    class MyViewHolder(var mView: View) : RecyclerView.ViewHolder(mView) {
        fun setTask(task: String?) {
            val taskTectView = mView.findViewById<TextView>(R.id.taskTv)
            taskTectView.text = task
        }

        fun setDesc(desc: String?) {
            val descTectView = mView.findViewById<TextView>(R.id.descriptionTv)
            descTectView.text = desc
        }

        fun setDate(date: String?) {
            val dateTextView = mView.findViewById<TextView>(R.id.dateTv)
            dateTextView?.text = date
        }
    }

    private fun updateTask() {
        val myDialog = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.update_data, null)
        myDialog.setView(view)
        val dialog = myDialog.create()
        val mTask = view.findViewById<EditText>(R.id.mEditTextTask)
        val mDescription = view.findViewById<EditText>(R.id.mEditTextDescription)
        val radioGroup = view.findViewById<RadioGroup>(R.id.rdGroup)
        mTask.setText(task)
        mTask.setSelection(task!!.length)
        mDescription.setText(description)
        mDescription.setSelection(description!!.length)
        val radioButton: RadioButton = radioGroup.getChildAt(status) as RadioButton
        radioButton.isChecked = true
        val delButton = view.findViewById<Button>(R.id.btnDelete)
        val updateButton = view.findViewById<Button>(R.id.btnUpdate)
        var index = 0
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            index =  when (checkedId) {
                R.id.rdToDo -> 0
                R.id.inProgress -> 1
                R.id.rdDone -> 2
                else -> 0
            }
        }
        updateButton.setOnClickListener {
            task = mTask.getText().toString().trim { it <= ' ' }
            description = mDescription.getText().toString().trim { it <= ' ' }
            val date = DateFormat.getDateInstance().format(Date())
            val model = Model(task, description, key, date, index)
            reference!!.child(key!!).setValue(model).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@HomeActivity,
                        "Data has been updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val err = task.exception.toString()
                    Toast.makeText(this@HomeActivity, "update failed $err", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            dialog.dismiss()
        }
        delButton.setOnClickListener {
            reference!!.child(key!!).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@HomeActivity,
                        "Task deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val err = task.exception.toString()
                    Toast.makeText(
                        this@HomeActivity,
                        "Failed to delete task $err",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                mAuth!!.signOut()
                val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}