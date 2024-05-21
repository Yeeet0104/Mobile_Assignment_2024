package util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.google.firebase.firestore.Blob
import java.io.ByteArrayOutputStream

fun ImageView.cropToBlobNullable(width: Int, height: Int): Blob? {
    val drawable = this.drawable ?: return null
    val bitmap = (drawable as BitmapDrawable).bitmap
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
    val byteArrayOutputStream = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    return Blob.fromBytes(byteArrayOutputStream.toByteArray())
}