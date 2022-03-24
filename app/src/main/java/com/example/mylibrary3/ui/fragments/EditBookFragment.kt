package com.example.mylibrary3.ui.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.mylibrary3.model.entity.NotificationData
import com.example.mylibrary3.model.entity.PushNotification
import com.example.mylibrary3.R
import com.example.mylibrary3.model.remote.RetrofitInstance
import com.example.mylibrary3.databinding.FragmentEditBookBinding
import com.example.mylibrary3.model.entity.Book
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.shasin.notificationbanner.Banner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class EditBookFragment : Fragment() {
    private lateinit var books: Book
    private var mRate: Float = 1.5f
    private lateinit var database: DatabaseReference

    private lateinit var binding: FragmentEditBookBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEditBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // region Change actionBar color
        val colorDrawable = ColorDrawable(Color.parseColor("#FFB300"))
        (activity as AppCompatActivity?)!!.supportActionBar!!.setBackgroundDrawable(colorDrawable)
        //endregion
        database = Firebase.database.reference

        //region Fill in the book information
        val arg = this.arguments
        books = arg?.getParcelable("books")!!
        binding.edNewBookName.setText(books.bookName)
        binding.edNewBookAuthor.setText(books.bookAuthor)
        binding.edNewPrice.setText(books.price.toString())
        binding.rbNewRatingBar.rating = books.rate!!
        val milliSeconds = books.launchYear?.time!!
        val mLaunchYear = DateFormat.format("yyyy", Date(milliSeconds)).toString()
        binding.edNewLaunchYear.setText(mLaunchYear)
        //endregion

        //region Book review
        binding.rbNewRatingBar.stepSize = .5f
        binding.rbNewRatingBar.setOnRatingBarChangeListener { _, rating, _ ->
            mRate = rating
        }
        //endregion
//region Update Button
        binding.btnEditBook.setOnClickListener {

            val mNewBookMap = getNewBooksData()

            MaterialAlertDialogBuilder(
                requireActivity(),
                R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog

            )
                .setTitle("Edit")
                .setMessage("Do you want to Edit this book?")
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { _, _ ->
                    updateBookDate(mNewBookMap)
                }
                .setIcon(R.drawable.ic_edit_book)

                .show()


        }
        //endregion

        //region Delete Button
        binding.btnDeleteBook.setOnClickListener {

            MaterialAlertDialogBuilder(
                requireActivity(),
                R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog
            )
                .setTitle("Delete")
                .setMessage("Do you want to delete this book?")
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { _, _ ->
                    binding.progressBar.visibility = View.VISIBLE
                    deleteBookData()
                }
                .setIcon(R.drawable.ic_delete_book)

                .show()
        }
        //endregion

    }

    private fun getNewBooksData(): Map<String, Any?> {
        val idBook = books.bookId
        val bookName = binding.edNewBookName.text.toString()
        val bookAuthor = binding.edNewBookAuthor.text.toString()
        val launchYear = binding.edNewLaunchYear.text.toString()
        val formatter = SimpleDateFormat("yyyy", Locale.UK)
        val mLaunchYear = formatter.parse(launchYear) as Date
        val price = binding.edNewPrice.text.toString()
        val rate = mRate

        val map = mutableMapOf<String, Any?>()

        if (bookName.isNotEmpty() && bookAuthor.isNotEmpty() && launchYear.isNotEmpty() && price.isNotEmpty()) {
            map["bookId"] = idBook!!
            map["bookName"] = bookName
            map["bookAuthor"] = bookAuthor
            map["launchYear"] = mLaunchYear
            map["price"] = price.toDouble()
            map["rate"] = rate
        } else {
            Banner.make(
                binding.root, requireActivity(), Banner.WARNING,
                "Fill in all fields !!", Banner.TOP, 3000
            ).show()
        }

        return map
    }

    private fun updateBookDate(newBookMap: Map<String, Any?>) {
        database = FirebaseDatabase.getInstance().getReference("books")

        database.child(books.bookId!!).updateChildren(newBookMap)
            .addOnSuccessListener {

                val title = "Edit Book"
                val message = "Book data has been modified (${books.bookName})"

                PushNotification(
                    NotificationData(title, message),
                    AddBookFragment.TOPIC

                ).also {
                    sendNotification(it)
                }
                Banner.make(
                    binding.root, requireActivity(), Banner.SUCCESS,
                    "Edit succeeded:)", Banner.TOP, 3000
                ).show()

                val navOptions =
                    NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()
                findNavController().navigate(
                    R.id.action_editBookFragment_to_homeFragment,
                    null,
                    navOptions
                )

            }

            .addOnFailureListener {
                Banner.make(
                    binding.root, requireActivity(), Banner.ERROR,
                    "Edit failed :(", Banner.TOP, 3000
                ).show()
            }


    }

    private fun deleteBookData() {
        database = FirebaseDatabase.getInstance().getReference("books")

        database.child(books.bookId!!).removeValue()
            .addOnSuccessListener {

                val title = "Delete Book"
                val message = "Book deleted (${books.bookName})"

                PushNotification(
                    NotificationData(title, message),
                    AddBookFragment.TOPIC

                ).also {
                    sendNotification(it)
                }
                Banner.make(
                    binding.root, requireActivity(), Banner.SUCCESS,
                    "Delete succeeded:)", Banner.TOP, 3000
                ).show()
                val navOptions =
                    NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()
                findNavController().navigate(
                    R.id.action_editBookFragment_to_homeFragment,
                    null,
                    navOptions
                )

            }

            .addOnFailureListener {
                Banner.make(
                    binding.root, requireActivity(), Banner.ERROR,
                    "Delete failed :(", Banner.TOP, 3000
                ).show()
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
            true // default to enabled
        ) {
            override fun handleOnBackPressed() {

                val navOptions = NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()

                findNavController().navigate(
                    R.id.action_editBookFragment_to_homeFragment,
                    null,
                    navOptions
                )
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,  // LifecycleOwner
            callback
        )
    }
}