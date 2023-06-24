package com.progetto.nomeprogetto.Fragments.MainActivity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.progetto.nomeprogetto.Activities.LoginRegisterActivity
import com.progetto.nomeprogetto.R
import com.progetto.nomeprogetto.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(inflater)

        binding.logoutButton.setOnClickListener{
            val sharedPref = requireContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("IS_LOGGED_IN", false).putInt("ID",-1)
            editor.apply()
            val i = Intent(requireContext(), LoginRegisterActivity::class.java)
            startActivity(i)

            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()

            requireActivity().finish()
        }

        return binding.root
    }
}
