package com.weihan.chou.spendaway

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.regex.Pattern
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class SignUp : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        loadingProgressBarSU.visibility = View.GONE
        mAuth = FirebaseAuth.getInstance();


    }

    fun confirm(v: View) {
        loadingProgressBarSU.visibility = View.VISIBLE
        var email = emailLoginEditText.text.toString()
        var password = PWEditText.text.toString()
        var cpassword = confirmPWEditText.text.toString()
        var name = nameLoginEditText.text.toString()
        var errorMsg = ""
        var error = false

        if (name == "" || email == "" || password == "") {
            errorMsg = resources.getString(R.string.ErrorEmpty)
            error = true
        } else if (password != cpassword) {
            errorMsg = resources.getString(R.string.ErrorPassword)
            error = true
        } else if (!isEmailValid(email)) {
            errorMsg = resources.getString(R.string.ErrorEmail)
            error = true
        } else if (password.length < 6) {
            errorMsg = getString(R.string.passwordlengtherror)
            error = true
        }


        if (error) {
            loadingProgressBarSU.visibility = View.GONE
            emptyDialog(errorMsg)
        } else {

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth.currentUser

                        //updates user name
                        val profileUpdates = Builder()
                            .setDisplayName(name)
                            .build()

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("tag", "User name updated.")
                                }
                            }

                        //create new user in database
                        database = FirebaseDatabase.getInstance().reference
                        val newUser = User(name, user?.uid)
                        val userValues = newUser.toMap()
                        val childUpdates = HashMap<String, Any>()
                        childUpdates["/users/${user?.uid}"] = userValues
                        database.updateChildren(childUpdates)

                        loadingProgressBarSU.visibility = View.GONE

                        AuthUI.getInstance()
                            .signOut(this)
                            .addOnCompleteListener {
                                val intent = Intent(this@SignUp, MainActivity::class.java)
                                intent.putExtra("msgCompleted", "Account Created")
                                setResult(Activity.RESULT_OK, intent)
                                finish()
                            }
                    } else {
                        //sign in fail
                        loadingProgressBarSU.visibility = View.GONE
                        emptyDialog(resources.getString(R.string.miscError))

                    }

                }
        }


    }

    fun cancel(v: View) {
        val intent = Intent(this@SignUp, MainActivity::class.java)
        intent.putExtra("msgCancelled", "Cancelled")
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    private fun isEmailValid(email: String): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    private fun emptyDialog(msg: String) {
        val errorDialog = DialogEmpty.newInstance(msg)
        errorDialog.show(supportFragmentManager, "errorFragment")
    }

}
