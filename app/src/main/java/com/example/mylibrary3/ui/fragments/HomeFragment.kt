package com.example.mylibrary3.ui.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mylibrary3.R
import com.example.mylibrary3.adapter.BookRvAdapter
import com.example.mylibrary3.databinding.FragmentHomeBinding
import com.example.mylibrary3.model.entity.Book
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class HomeFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private var booksItem: MutableList<Book> = ArrayList()
    private val bookRvAdapter: BookRvAdapter by lazy {
        BookRvAdapter()
    }

    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = Firebase.database.reference
        binding.progressBar.visibility = View.VISIBLE

        setUpRecycler()

        readData()

        // region Change actionBar color
        val colorDrawable = ColorDrawable(Color.parseColor("#FFB300"))
        (activity as AppCompatActivity?)!!.supportActionBar!!.setBackgroundDrawable(colorDrawable)
        //endregion

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addBookFragment)
        }


    }

    private fun readData() {

        database = FirebaseDatabase.getInstance().getReference("books")

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.let {
                    booksItem.clear()
                    for (bookSnapshot in snapshot.children) {
                        val book = bookSnapshot.getValue(Book::class.java)
                        booksItem.add(book!!)
                    }
                    booksItem.distinct()
                    bookRvAdapter.setList(booksItem)
                    binding.progressBar.visibility = View.GONE
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }


        })
    }

    private fun setUpRecycler() {
        val lm = LinearLayoutManager(requireActivity())
        binding.rvBooks.layoutManager = lm
        binding.rvBooks.adapter = bookRvAdapter
    }



}