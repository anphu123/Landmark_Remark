package com.example.landmarkremark

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var firebaseHelper: FirebaseHelper // Khởi tạo FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo FirebaseHelper
        firebaseHelper = FirebaseHelper()

        // Lấy SupportMapFragment và thiết lập bản đồ
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Thiết lập thanh tìm kiếm
        val searchView = findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchNotes(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchNotes(it) }
                return false
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Yêu cầu quyền truy cập vị trí nếu chưa có
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        // Tải tất cả ghi chú từ Firebase và hiển thị trên bản đồ
        loadAllNotes()

        // Xử lý sự kiện long touch trên bản đồ để thêm ghi chú
        mMap.setOnMapLongClickListener { latLng ->
            showAddNoteDialog(latLng)
        }
    }

    // Hiển thị hộp thoại để thêm ghi chú
    private fun showAddNoteDialog(latLng: LatLng) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.edit_text_name)
        val noteEditText = dialogView.findViewById<EditText>(R.id.edit_text_note)

        AlertDialog.Builder(this)
            .setTitle("Add Note")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = nameEditText.text.toString()
                val note = noteEditText.text.toString()
                saveNoteToFirebase(name, note, latLng)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Lưu ghi chú vào Firebase
    private fun saveNoteToFirebase(name: String, note: String, latLng: LatLng) {
        val locationNote = LocationNote(name, note, latLng.latitude, latLng.longitude)
        firebaseHelper.saveNote(locationNote, {
            mMap.addMarker(MarkerOptions().position(latLng).title(name).snippet(note))
        }, {
            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show()
        })
    }

    // Tải tất cả ghi chú từ Firebase
    private fun loadAllNotes() {
        firebaseHelper.loadAllNotes { notes ->
            mMap.clear()
            for (note in notes) {
                val latLng = LatLng(note.latitude, note.longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title(note.name).snippet(note.note))
            }
        }
    }

    // Tìm kiếm ghi chú trong Firebase
    private fun searchNotes(query: String) {
        firebaseHelper.searchNotes(query) { notes ->
            mMap.clear()
            var firstLocation: LatLng? = null
            for (note in notes) {
                val latLng = LatLng(note.latitude, note.longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title(note.name).snippet(note.note))

                if (firstLocation == null) {
                    firstLocation = latLng
                }
            }
            // Di chuyển camera tới vị trí ghi chú đầu tiên
            firstLocation?.let {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
            }
        }
    }

    // Xử lý kết quả yêu cầu quyền truy cập vị trí
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                mMap.isMyLocationEnabled = true
            }
        }
    }
}

