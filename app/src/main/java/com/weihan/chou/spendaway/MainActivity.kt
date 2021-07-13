package com.weihan.chou.spendaway

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.View.VISIBLE
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast


const val CREA_ACCOUNT = 1000

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance();
        val currentUser = mAuth.currentUser

        object : CountDownTimer(2500, 1000) {
            override fun onFinish() {
                if (currentUser == null) {
                    bookITextView.visibility = View.GONE
                    loadingProgressBar.visibility = View.GONE
                    rootView.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorSplashText))
                    starzIconView.setImageResource(R.drawable.starz)
                    startAnimation()
                } else {
                    startApp()
                }
            }

            override fun onTick(p0: Long) {}
        }.start()
    }


    fun login(v: View) {

        var email = emailEditText.text.toString()
        var password = passwordEditText.text.toString()
        var errorMsg: String = ""
        var error = false


        if (email == "" || password == "") {
            errorMsg = resources.getString(R.string.ErrorEmpty)
            error = true
        }

        if (error) {
            emptyDialog(errorMsg)
        } else {
            loadingProgressBar.visibility = VISIBLE
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        startApp()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                    loadingProgressBar.visibility = View.GONE
                }
            // [END sign_in_with_email]
        }
    }

    fun signUp(v: View) {
        val intent = Intent(this@MainActivity, SignUp::class.java)
        startActivityForResult(intent, CREA_ACCOUNT)
    }

    fun startApp() {
        runOnUiThread {
            val intent = Intent(this@MainActivity, MainPage::class.java)
            startActivity(intent)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREA_ACCOUNT) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    val msg = it.getStringExtra("msgCompleted")
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                }
            } else {
                data?.let {
                    val msg = it.getStringExtra("msgCancelled")
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun emptyDialog(msg: String) {
        val errorDialog = DialogEmpty.newInstance(msg)
        errorDialog.show(supportFragmentManager, "errorFragment")
    }

    private fun startAnimation() {
        starzIconView.animate().apply {
            x(50f)
            y(100f)
            duration = 1000
        }.setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                afterAnimationView.visibility = VISIBLE
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }
        })
    }
}


