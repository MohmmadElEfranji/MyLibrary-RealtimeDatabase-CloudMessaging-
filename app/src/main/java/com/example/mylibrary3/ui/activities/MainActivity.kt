package com.example.mylibrary3.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mylibrary3.R
import com.example.mylibrary3.ui.fragments.AddBookFragment
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseMessaging.getInstance().subscribeToTopic(AddBookFragment.TOPIC)
    }
}