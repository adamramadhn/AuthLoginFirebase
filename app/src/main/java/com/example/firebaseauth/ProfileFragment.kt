package com.example.firebaseauth

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.firebaseauth.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream


@Suppress("DEPRECATION")
class ProfileFragment : Fragment() {
    private lateinit var profileBinding: FragmentProfileBinding
    private lateinit var imageUri: Uri
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        profileBinding = FragmentProfileBinding.inflate(inflater, container, false)
        return profileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val userData = auth.currentUser
        if (userData != null) {
            profileBinding.apply {
                if (userData.photoUrl != null) {
                    Picasso.get().load(userData.photoUrl).into(ivProfile)
                } else {
                    Picasso.get().load("https://picsum.photos/id/316/200").into(ivProfile)
                }
                etName.setText(userData.displayName)
                etEmail.setText(userData.email)

                if (userData.isEmailVerified) {
                    icVerified.visibility = View.VISIBLE
                    icUnverified.visibility = View.GONE
                } else {
                    icUnverified.visibility = View.VISIBLE
                    icVerified.visibility = View.GONE
                }
                if (userData.phoneNumber.isNullOrEmpty()) {
                    etPhone.setText(getString(R.string.required_phone))
                } else {
                    etPhone.setText(userData.phoneNumber)
                }

            }

        }
        profileBinding.apply {
            ivProfile.setOnClickListener {
                intentCamera()
            }
        }
        profileBinding.btnUpdate.setOnClickListener {
            val image = when {
                //kondisi upload foto baru
                ::imageUri.isInitialized -> imageUri
                //kondisi jika kita tdk upload foto = default foto
                userData?.photoUrl == null -> Uri.parse("https://picsum.photos/id/316/200")
                //kondidi jika sudah ada foto sebelumnya
                else -> userData.photoUrl
            }
            profileBinding.apply {
                val name = etName.text.toString().trim()
                if (name.isEmpty()) {
                    etName.error = getString(R.string.required_name)
                    etName.requestFocus()
                    return@setOnClickListener
                }

                UserProfileChangeRequest.Builder().setDisplayName(name)
                    .setPhotoUri(image).build().also { changeReq ->
                        userData?.updateProfile(changeReq)?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(activity, "Profile Updated", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(activity, it.exception?.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
            }
        }

    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun intentCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            activity?.packageManager?.let {
                intent.resolveActivity(it).also {
                    startActivityForResult(intent, REQUEST_CAMERA)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            val imgBitmap = data?.extras?.get("data") as Bitmap
            uploadImage(imgBitmap)
        }
    }

    private fun uploadImage(imgBitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        val ref =
            FirebaseStorage.getInstance().reference.child("img/${FirebaseAuth.getInstance().currentUser?.uid}")
        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        ref.putBytes(image).addOnCompleteListener {
            if (it.isSuccessful) {
                ref.downloadUrl.addOnCompleteListener {
                    it.result?.let {
                        imageUri = it
                        profileBinding.ivProfile.setImageBitmap(imgBitmap)
                    }
                }
            }
        }
    }

    companion object {
        const val REQUEST_CAMERA = 100
    }

}