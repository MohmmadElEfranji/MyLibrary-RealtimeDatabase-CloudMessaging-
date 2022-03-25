package com.example.mylibrary3.ui.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.mylibrary3.model.entity.NotificationData
import com.example.mylibrary3.model.entity.PushNotification
import com.example.mylibrary3.R
import com.example.mylibrary3.model.remote.RetrofitInstance
import com.example.mylibrary3.databinding.FragmentAddBookBinding
import com.example.mylibrary3.model.entity.Book
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.shasin.notificationbanner.Banner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class AddBookFragment : Fragment() {
    private var mRate: Float = 1.5f
    private lateinit var database: DatabaseReference

    private lateinit var binding: FragmentAddBookBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddBookBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //region Get a DatabaseReference
        database = Firebase.database.reference
        //endregion


        //region Book review
        binding.rbRatingBar.stepSize = .5f

        binding.rbRatingBar.setOnRatingBarChangeListener { _, rating, _ ->
            mRate = rating
        }

        //endregion

        //region Add Button
        binding.btnAddBook.setOnClickListener {
            val idBook = database.push().key
            val bookName = binding.edBookName.text.toString()
            val bookAuthor = binding.edBookAuthor.text.toString()
            val launchYear = binding.edLaunchYear.text
            val price = binding.edPrice.text.toString()
            val rate = mRate

            if (bookName.isNotEmpty() && bookAuthor.isNotEmpty() && launchYear.isNotEmpty() && price.isNotEmpty()) {
                val dateString = launchYear.toString()
                val formatter = SimpleDateFormat("yyyy", Locale.UK)
                val mLaunchYear = formatter.parse(dateString) as Date
                val book = Book(idBook, bookName, bookAuthor, mLaunchYear, price.toDouble(), rate)
                val bookValues = book.toMap()
                binding.progressBar.visibility = View.VISIBLE

                addBook(idBook, bookValues)


            } else {
                Banner.make(
                    binding.root, requireActivity(), Banner.WARNING,
                    "Fill in all fields !!", Banner.TOP, 3000
                ).show()
            }


        }

        //endregion


        // region Change actionBar color
        val colorDrawable = ColorDrawable(Color.parseColor("#FFB300"))
        (activity as AppCompatActivity?)!!.supportActionBar!!.setBackgroundDrawable(colorDrawable)
        //endregion
    }

    private fun addBook(idBook: String?, bookValues: Map<String, Any?>) {

        database.child("books/${idBook}").setValue(bookValues)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val title = "Add Book"
                    val message = "Book added (${bookValues["bookName"]})"

                    PushNotification(
                        NotificationData(title, message),
                        TOPIC

                    ).also {
                        sendNotification(it)
                    }

                    Banner.make(
                        binding.root, requireActivity(), Banner.SUCCESS,
                        "Addition succeeded :)", Banner.TOP, 3000
                    ).show()

                    val navOptions =
                        NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()
                    findNavController().navigate(
                        R.id.action_addBookFragment_to_homeFragment,
                        null,
                        navOptions
                    )
                    binding.progressBar.visibility = View.GONE

                } else {
                    Banner.make(
                        binding.root, requireActivity(), Banner.ERROR,
                        "Addition failed :(", Banner.TOP, 3000
                    ).show()
                }

            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failure", Toast.LENGTH_SHORT).show()
            }

    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.pushNotification(notification)
                if (response.isSuccessful) {
                    Log.d("AddBookFragment", "Response isSuccessful }")
                } else {
                    Log.e("AddBookFragment", response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e("AddBookFragment", e.toString())
            }

        }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(
            true
        ) {
            override fun handleOnBackPressed() {
                val navOptions =
                    NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()
                findNavController().navigate(
                    R.id.action_addBookFragment_to_homeFragment,
                    null,
                    navOptions
                )
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    companion object {
        const val TOPIC = "/topics/myTopic"
    }
}