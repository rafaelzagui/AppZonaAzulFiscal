package br.com.puc.appfiscalzonaazul

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import br.com.puc.appfiscalzonaazul.databinding.ActivityCameraPreviewBinding
import br.com.puc.appfiscalzonaazul.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraPreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraPreviewBinding
    //processamento de imagem (nao permitir ou controlar o melhor drive )
    private lateinit var cameraProviderFuture:ListenableFuture<ProcessCameraProvider>
    //selecionar se deseja a camera frontal ou traseira
    private lateinit var cameraSelector: CameraSelector
    //insere a captura
    private  var imageCapture: ImageCapture?=null


    //executor de thread
    private lateinit var imageCaptureExecutor : ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imageCaptureExecutor = Executors.newSingleThreadExecutor()
        startCamera()
        //evento click no botao
        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            {
                blinkPreview()
            }
        }
    }

    private fun startCamera()
    {
        cameraProviderFuture.addListener({
            imageCapture = ImageCapture.Builder().build()

            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also{
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }
            try {
                //abrir o preview
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageCapture)

            }catch (e:Exception)
            {
                Log.e("CameraPreview","Falha ao abrir a camera")
            }
        }, ContextCompat.getMainExecutor(this))
    }
    private fun takePhoto(){
        imageCapture?.let {

            val fileName ="FOTO_JPEG_${System.currentTimeMillis()}"
            val file = File(externalMediaDirs[0],fileName)

            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

            it.takePicture(
                outputFileOptions,
                imageCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback{
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.i("CameraPreview","a imagem foi salva no diretorio: ${file.toUri()}")
                        val intent = Intent()
                        intent.data = Uri.parse(file.toString())
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(binding.root.context,"Erro ao salvar foto",Toast.LENGTH_LONG).show()
                        Log.e("Camerapreview","Execeção ao gravar arquivo da foto:$exception")
                    }


                }
            )


        }
    }
    private fun blinkPreview()
    {
        binding.root.postDelayed({
            binding.root.foreground=ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            },50)
        },100)
    }
}