package com.example.assignment2

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.example.assignment2.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.UUID

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val PICK_PDF_REQUEST = 1
    private var pdfUri: Uri? = null
    lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    binding.selectFileButton.setOnClickListener {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(intent, PICK_PDF_REQUEST)
        uploadFile()
    }



        binding.downloadFileButton.setOnClickListener {
            val folder = File(applicationContext.filesDir, "Ass2")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Downloading...")
            showProgressDialog()


            val storageRef = Firebase.storage.reference
                .child("PDF")
            storageRef.listAll().addOnSuccessListener { listResult ->
                listResult.items.forEach { item ->
                    val file = File(folder, item.name)
                    item.getFile(file).addOnSuccessListener {
                        // File downloaded successfully
                        hideProfressDialog()
                        Toast.makeText(applicationContext, "Download and save Success", Toast.LENGTH_SHORT).show()

                    }.addOnFailureListener { exception ->
                        // Handle download failure
                        hideProfressDialog()
                        Toast.makeText(applicationContext, "Save fail", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener { exception ->
                // Handle listAll() failure
                hideProfressDialog()
                Toast.makeText(applicationContext, "Download fail", Toast.LENGTH_SHORT).show()
            }
        }
//    downloadFile("")

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null) {
            pdfUri = data.data
           // binding.uploadFileButton.isEnabled = true
            uploadFile()
//            downloadFile("")
        }
    }

    private fun uploadFile() {
        pdfUri?.let { uri ->
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            val storageRef = FirebaseStorage.getInstance().reference.child("pdfs/${uri.lastPathSegment}")
            val uploadTask = storageRef.putFile(uri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    // Save the download URL to Firebase Firestore or use it to download the file later
                } else {
                    // Handle errors
                }
                progressDialog.dismiss()
            }
        }
    }

    private fun downloadFile(downloadUrl: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Downloading...")
        progressDialog.show()

        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrl)
        val localFile = File.createTempFile("pdf", "pdf")

        storageRef.getFile(localFile).addOnSuccessListener {
            // Open the downloaded file using an Intent
        }
    }

    fun showProgressDialog() {
        progressDialog.show()
    }

    fun hideProfressDialog() {
        progressDialog.dismiss()
    }



    }

