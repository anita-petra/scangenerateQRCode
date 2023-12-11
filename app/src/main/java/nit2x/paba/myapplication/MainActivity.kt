package nit2x.paba.myapplication

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.util.EnumMap

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val _btnScan = findViewById<Button>(R.id.btnScan)
        _btnScan.setOnClickListener {
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.setBeepEnabled(true)
            integrator.setPrompt("Arahkan ke QR Code untuk Scan")
            integrator.setOrientationLocked(true)
            integrator.initiateScan()
        }

        val _btnGenerate = findViewById<Button>(R.id.btnGenerate)
        val _ivHasil = findViewById<ImageView>(R.id.ivHasil)

        _btnGenerate.setOnClickListener {
            val _etData = findViewById<EditText>(R.id.etData)
            var hasilGenerate = generateQR(_etData.text.toString())
            _ivHasil.setImageBitmap(hasilGenerate)
            saveToGallery(this,hasilGenerate!!, "QR1")
        }
    }

    fun saveToGallery(context : Context, bitmap: Bitmap, fileName: String) {
        val contentVal = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentVal)
        uri.let {
            if (uri != null) {
                resolver.openOutputStream(uri).use { outputStream ->
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG,100, outputStream)
                    }
                }
            }
        }
    }

    fun generateQR(isiData : String) : Bitmap? {
        val bitMatrix : BitMatrix = try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            MultiFormatWriter().encode(
                isiData,
                BarcodeFormat.QR_CODE,
                600,600,
                hints
            )
        } catch (e : Exception) {
            e.printStackTrace()
            return null
        }

        val qrCodeWidth = bitMatrix.width
        val qrCodeHeight = bitMatrix.height
        val datapixels = IntArray(qrCodeWidth*qrCodeHeight)

        for (y in 0 until qrCodeHeight) {
            val offset = y * qrCodeWidth
            for (x in 0 until qrCodeWidth) {
                datapixels[offset + x] = if (bitMatrix[x,y]) {
                    resources.getColor(R.color.black, theme)
                } else {
                    resources.getColor(R.color.white, theme)
                }
            }
        }

        val bitmap = Bitmap.createBitmap(qrCodeWidth, qrCodeHeight, Bitmap.Config.RGB_565)
        bitmap.setPixels(datapixels,0,qrCodeWidth,0,0,qrCodeWidth,qrCodeHeight)
        return bitmap
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            var hasil = result.contents
            val _tvHasil = findViewById<TextView>(R.id.tvHasil)
            _tvHasil.setText(hasil.toString())
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}