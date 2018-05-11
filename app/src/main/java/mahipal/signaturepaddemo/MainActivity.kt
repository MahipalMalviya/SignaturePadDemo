package mahipal.signaturepaddemo

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.github.gcacace.signaturepad.views.SignaturePad
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.dialog.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import android.R.attr.data
import android.util.Base64
import com.github.gcacace.signaturepad.utils.SvgBuilder
import java.nio.charset.StandardCharsets


class MainActivity : AppCompatActivity(), View.OnClickListener, SignaturePad.OnSignedListener {

    private var signaturePath: String? = null
    private val REQUEST_CODE_EXT_STORAGE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requestStoragePermission()
        }
        setContentView(R.layout.activity_main)

        clear_btn.isEnabled = false
        save_btn.isEnabled = false
        signatureView_btn.isEnabled = false

        clear_btn.setOnClickListener(this)
        save_btn.setOnClickListener(this)
        signatureView_btn.setOnClickListener(this)
        signaturePad.setOnSignedListener(this)
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_EXT_STORAGE)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.clear_btn -> {
                if (deleteSignatureImage(signaturePath)){
                    Toast.makeText(this,"Signature image deleted.",Toast.LENGTH_SHORT).show()
                }
                signaturePad.clear()
                signatureView_btn.isEnabled = false
            }
            R.id.save_btn -> {
                saveSignature()
                signatureView_btn.isEnabled = true
            }
            R.id.signatureView_btn -> {
//                getSignatureSvg()
                getSignatureImage()
            }
        }
    }

    private fun saveSignature() {
        val bitmap = signaturePad.signatureBitmap

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream)
        val rand = Random()
        val randomValue = rand.nextInt(9999)

        val file = File(Environment.getExternalStorageDirectory().absolutePath + "/" +
                randomValue.toString() + "capturedSignature" + randomValue + ".jpg")

        try {
            if (file.createNewFile()) {
                file.createNewFile()
            }
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(byteArrayOutputStream.toByteArray())
            fileOutputStream.close()

            signaturePath = file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteSignatureImage(signaturePath: String?): Boolean{
        if (signaturePath != null) {
            var deleteFile = File(signaturePath)
            if (deleteFile.delete()) {
                return true
            }
        }
        return false
    }

    private fun getSignatureSvg(): String{
        return signaturePad.signatureSvg
    }

    private fun getSignatureImage(){
        val dialog=  Dialog(this)
        dialog.setContentView(R.layout.dialog)
        dialog.setCanceledOnTouchOutside(true)

        val imageSignature = dialog.findViewById<ImageView>(R.id.iv_signature)
        val closeButton = dialog.findViewById<Button>(R.id.close_button)
        dialog.show()
        if (signaturePath != null) {
            imageSignature.setImageURI(Uri.parse("file://$signaturePath"))
        }

        if (closeButton != null) {
            closeButton.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    if (deleteSignatureImage(signaturePath)){
                        Toast.makeText(applicationContext,"Signature image deleted.",Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
            })
        }

    }

    override fun onStartSigning() {

    }

    override fun onClear() {
        clear_btn.isEnabled = false
        save_btn.isEnabled = false
    }

    override fun onSigned() {
        clear_btn.isEnabled = true
        save_btn.isEnabled = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            REQUEST_CODE_EXT_STORAGE -> {
                if (grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED){

                } else {
                    Toast.makeText(this,"Oops you just denied the permission",Toast.LENGTH_LONG).show();
                    finish()
                }
            }
        }
    }
}
