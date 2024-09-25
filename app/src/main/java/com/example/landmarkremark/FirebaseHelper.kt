package com.example.landmarkremark

import com.google.firebase.database.*

/**
 * Lớp FirebaseHelper để xử lý các tương tác với Firebase Realtime Database.
 */
class FirebaseHelper {

    // Tham chiếu đến node "location_notes" trong Firebase
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("location_notes")

    /**
     * Lưu một ghi chú vào Firebase.
     * @param note Đối tượng LocationNote chứa thông tin ghi chú.
     * @param onSuccess Callback khi lưu thành công.
     * @param onFailure Callback khi có lỗi xảy ra.
     */
    fun saveNote(note: LocationNote, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val noteId = database.push().key // Tạo ID ngẫu nhiên cho ghi chú
        if (noteId != null) {
            database.child(noteId).setValue(note)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
        }
    }

    /**
     * Tải tất cả các ghi chú từ Firebase và trả về qua callback.
     * @param onNotesLoaded Callback khi các ghi chú đã được tải.
     */
    fun loadAllNotes(onNotesLoaded: (List<LocationNote>) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = mutableListOf<LocationNote>()
                for (noteSnapshot in snapshot.children) {
                    val note = noteSnapshot.getValue(LocationNote::class.java)
                    if (note != null) {
                        notes.add(note)
                    }
                }
                onNotesLoaded(notes)
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu tải dữ liệu bị hủy
            }
        })
    }

    /**
     * Tìm kiếm ghi chú theo từ khóa trong Firebase.
     * @param query Từ khóa tìm kiếm.
     * @param onSearchCompleted Callback khi tìm kiếm hoàn tất.
     */
    fun searchNotes(query: String, onSearchCompleted: (List<LocationNote>) -> Unit) {
        database.orderByChild("name").startAt(query).endAt(query + "\uf8ff")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notes = mutableListOf<LocationNote>()
                    for (noteSnapshot in snapshot.children) {
                        val note = noteSnapshot.getValue(LocationNote::class.java)
                        if (note != null) {
                            notes.add(note)
                        }
                    }
                    onSearchCompleted(notes)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý lỗi nếu tìm kiếm thất bại
                }
            })
    }

}
