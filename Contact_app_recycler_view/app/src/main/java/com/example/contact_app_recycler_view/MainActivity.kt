package com.example.contact_app_recycler_view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), ContactAdapter.OnContactActionListener {
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSave: Button
    private lateinit var btnLoadContacts: Button
    private lateinit var recyclerViewContacts: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var btnSortAsc: Button
    private lateinit var btnSortDesc: Button
    private lateinit var ivAddImage: ImageView
    private lateinit var cvAddImage: CardView

    private lateinit var contactAdapter: ContactAdapter
    private val contactListEntries = mutableListOf<Contact>()
    private lateinit var contactListFull: MutableList<Contact>
    private var currentSortAsc: Boolean = true

    private var selectedImageUri: String? = null
    private var tempEditImageUri: String? = null
    private var dialogImageView: ImageView? = null

    // for image picking when adding
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it.toString()
            ivAddImage.setImageURI(it)
        }
    }

    // for image picking when editing
    private val editImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            tempEditImageUri = it.toString()
            dialogImageView?.setImageURI(it)
        }
    }

    // for contact loading permission request
    private val requestContactsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                loadContactsFromPhone()
            } else {
                Toast.makeText(this, "Contacts permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize UI components
        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        btnSave = findViewById(R.id.btnSave)
        btnLoadContacts = findViewById(R.id.btnLoadContacts)
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts)
        searchView = findViewById(R.id.searchView)
        btnSortAsc = findViewById(R.id.btnSortAsc)
        btnSortDesc = findViewById(R.id.btnSortDesc)
        ivAddImage = findViewById(R.id.ivAddImage)
        cvAddImage = findViewById(R.id.cvAddImage)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setup RecyclerView
        contactAdapter = ContactAdapter(contactListEntries, this)
        recyclerViewContacts.layoutManager = LinearLayoutManager(this)
        recyclerViewContacts.adapter = contactAdapter
        
        // initialize the master list AFTER the adapter is created
        contactListFull = contactAdapter.contactListFull

        // Button Click Listeners
        btnSave.setOnClickListener {
            saveContact()
        }

        btnLoadContacts.setOnClickListener {
            checkPermissionAndLoadContacts()
        }

        cvAddImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Setup search View
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                contactAdapter.filter(newText ?: "")
                return true
        }
        })

        // Setup Sorting buttons
        btnSortAsc.setOnClickListener {
            sortContactsByName(true)
        }

        btnSortDesc.setOnClickListener {
            sortContactsByName(false)
        }
    }

    private fun refreshList() {
        contactAdapter.filter(searchView.query.toString())
    }

    private fun sortContactsByName(ascending: Boolean) {
        if (ascending){
            currentSortAsc = true
            contactListFull.sortBy { it.name.lowercase() }
        } else {
            currentSortAsc = false
            contactListFull.sortByDescending { it.name.lowercase() }
        }
        refreshList()
    }


    private fun saveContact() {
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        if (!validateInputs(name, phone, etName, etPhone)) {
            return
        }

        val newContact = Contact(name, phone, selectedImageUri)
        contactListFull.add(newContact)
        refreshList()
        sortContactsByName(currentSortAsc)
        
        // Scroll to the new contact
        val index = contactListEntries.indexOf(newContact)
        if (index != -1) {
            recyclerViewContacts.scrollToPosition(index)
        }

        Toast.makeText(this, "Contact saved successfully", Toast.LENGTH_SHORT).show()

        // Reset inputs
        etName.text.clear()
        etPhone.text.clear()
        selectedImageUri = null
        ivAddImage.setImageResource(android.R.drawable.ic_menu_camera)
        etName.requestFocus()
    }

    private fun validateInputs(name: String, phone: String, nameInput: EditText, phoneInput: EditText): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            nameInput.error = "Name is required"
            isValid = false
        }

        if (phone.isEmpty()) {
            phoneInput.error = "Phone number is required"
            isValid = false
        } else if (phone.length < 10 || !phone.all { it.isDigit() || it == '+' }) {
            phoneInput.error = "Enter valid phone number"
            isValid = false
        }

        return isValid
    }

    override fun onItemClick(position: Int) {
        val contact = contactListEntries[position]
        Toast.makeText(this, "Contact: ${contact.name}\nPhone: ${contact.phone}", Toast.LENGTH_SHORT).show()
    }

    override fun onEditClick(position: Int) {
        showEditDialog(position)
    }

    override fun onDeleteClick(position: Int) {
        showDeleteDialog(position)
    }

    private fun showDeleteDialog(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete this contact?")
            .setPositiveButton("Yes") { _, _ ->
                val contactToDelete = contactListEntries[position]
                contactListFull.remove(contactToDelete)
                refreshList()
                Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun checkPermissionAndLoadContacts() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadContactsFromPhone()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("This app needs permission to read your contacts to display them.")
                    .setPositiveButton("Grant") { _, _ ->
                        requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
                    }
                    .setNegativeButton("Deny", null)
                    .show()
            }
            else -> {
                requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun loadContactsFromPhone() {
        val loadedContacts = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex) ?: ""
                val phone = it.getString(phoneIndex) ?: ""
                val photoUri = it.getString(photoIndex)

                if (name.isNotBlank() && phone.isNotBlank()) {
                    loadedContacts.add(Contact(name, phone, photoUri))
                }
            }
        }

        if (loadedContacts.isEmpty()) {
            Toast.makeText(this, "No contacts found on your phone", Toast.LENGTH_SHORT).show()
            return
        }

        contactListFull.clear()
        contactListFull.addAll(loadedContacts)
        refreshList()

        Toast.makeText(this, "${loadedContacts.size} contacts loaded", Toast.LENGTH_SHORT).show()
    }

    private fun showEditDialog(position: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_dialog_edit_item, null)
        val etEditName = dialogView.findViewById<EditText>(R.id.etEditName)
        val etEditPhone = dialogView.findViewById<EditText>(R.id.etEditPhone)
        val ivEditImage = dialogView.findViewById<ImageView>(R.id.ivEditImage)
        val cvEditImage = dialogView.findViewById<CardView>(R.id.cvEditImage)

        val contact = contactListEntries[position]
        etEditName.setText(contact.name)
        etEditPhone.setText(contact.phone)
        
        tempEditImageUri = contact.imageUri
        dialogImageView = ivEditImage

        if (tempEditImageUri != null) {
            ivEditImage.setImageURI(Uri.parse(tempEditImageUri))
        } else {
            ivEditImage.setImageResource(android.R.drawable.ic_menu_camera)
        }

        cvEditImage.setOnClickListener {
            editImageLauncher.launch("image/*")
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Contact")
            .setView(dialogView)
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val updatedName = etEditName.text.toString().trim()
            val updatedPhone = etEditPhone.text.toString().trim()

            if (validateInputs(updatedName, updatedPhone, etEditName, etEditPhone)) {
                contact.name = updatedName
                contact.phone = updatedPhone
                contact.imageUri = tempEditImageUri
                refreshList()
                Toast.makeText(this, "Contact updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }
}
