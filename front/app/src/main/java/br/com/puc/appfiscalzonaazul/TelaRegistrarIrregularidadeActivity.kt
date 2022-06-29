package br.com.puc.appfiscalzonaazul

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import br.com.puc.appfiscalzonaazul.databinding.ActivityMainBinding
import br.com.puc.appfiscalzonaazul.functions.FunctionsGenericResponse
import br.com.puc.appfiscalzonaazul.functions.GenericInsertResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.gson.GsonBuilder
import okhttp3.internal.Util
import java.io.ByteArrayOutputStream


class TelaRegistrarIrregularidadeActivity : AppCompatActivity() {
    private lateinit var functions: FirebaseFunctions
    private var images: Array<ShapeableImageView?> = arrayOf(null, null, null, null)
    private var imageUrls: Array<String?> = arrayOf(null, null, null, null)
    private lateinit var image: ShapeableImageView
    // usando o logcat (ferramenta do Android studio para verificar a saída)
    private val logEntry = "Registrar Irregularidade";

    // instanciando um objeto gson
    private val gson = GsonBuilder().enableComplexMapKeySerialization().create()

    private var storage : FirebaseStorage? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inicializar o uso dos functions NA REGIAO DESEJADA
        functions = Firebase.functions("southamerica-east1")
        //inicializar storage
        storage = Firebase.storage
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraProviderResult.launch(android.Manifest.permission.CAMERA)
        binding.imageFrontal.setOnClickListener {
            //solicitir permissao de CAMERA
            //irPARACAMERA()
            image = binding.imageFrontal
            images[0] = image
            startForResult.launch(Intent(this, CameraPreviewActivity::class.java))
        }
        binding.imageTraseira.setOnClickListener {
            //solicitir permissao de CAMERA
            image = binding.imageTraseira
            images[1] = image
            startForResult.launch(Intent(this, CameraPreviewActivity::class.java))

        }
        binding.imageEsquerda.setOnClickListener {
            //solicitir permissao de CAMERA
            image = binding.imageEsquerda
            images[2] = image
            startForResult.launch(Intent(this, CameraPreviewActivity::class.java))


        }
        binding.imageDireita.setOnClickListener {
            //solicitir permissao de CAMERA
            image = binding.imageDireita
            images[3] = image
            startForResult.launch(Intent(this, CameraPreviewActivity::class.java))
        }
        binding.btnRegistro.setOnClickListener {
            val placa = intent.getStringExtra("placa")
            binding.tvPlaca.setText(placa)
            val p = Veiculo(binding.tvPlaca.text.toString(),"Irregular")
            RegistrarIrregularidade(p)
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {

                        val e = task.exception
                        if (e is FirebaseFunctionsException) {
                            val code = e.code
                            val details = e.details
                        }
                        // tratar a exceção...

                    }else{
                        // convertendo.
                        val genericResp = gson.fromJson(task.result, FunctionsGenericResponse::class.java)


                        // abra a aba Logcat e selecione "INFO" e filtre por
                        Log.i(logEntry, genericResp.status.toString())
                        Log.i(logEntry, genericResp.message.toString())
                        Log.i(logEntry, genericResp.payload.toString())
                        val insertInfo = gson.fromJson(genericResp.payload.toString(), GenericInsertResponse::class.java)
                        Snackbar.make(binding.btnRegistro, "Irregularidade registrada com sucesso: " + insertInfo.docId,
                            Snackbar.LENGTH_LONG).show();
                    }
                })
            retornaTelaConsulta()
            //uploadImange1()
        }
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uriImage = result.data?.data
                image.setImageURI(uriImage)
                val storageRef = storage!!.reference
                val mountainsRef = storageRef.child("imagem")

            }
        }

    private fun retornaTelaConsulta()
    {
        val intentVoltar = Intent(this,TelaConsultaActivity::class.java)
        startActivity(intentVoltar)
    }

   /*private fun uploadImange1(){
       val bitmap =(binding.imageFrontal.drawable as BitmapDrawable).bitmap //
       val baos = ByteArrayOutputStream()
       bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
       val data = baos.toByteArray()
       val reference = storage!!.reference.child("image/uploadImage1.jpeg")//estamos compressando a a imagem e atribuindo uma qualidade de 20 ou diminuido o tamamnho
       var uploadTask = reference.putBytes(data)
       uploadTask.addOnSuccessListener {
           // Handle unsuccessful uploads
           Snackbar.make(binding.root,"upload sucesso",Snackbar.LENGTH_INDEFINITE).show()
       }.addOnFailureListener { taskSnapshot ->
           // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
           Snackbar.make(binding.root,"upload sucesso",Snackbar.LENGTH_INDEFINITE).show()
       }
   }*/

    private val cameraProviderResult = registerForActivityResult(ActivityResultContracts.RequestPermission())
    {
        if(!it)
        {
            Snackbar.make(binding.root,"Voce não concedeu permissões para usar a camera",Snackbar.LENGTH_INDEFINITE).show()
        }
    }

    private fun RegistrarIrregularidade(p: Veiculo): Task<String> {
        val data = hashMapOf(
            "placa" to p.placa,
            "situacao" to p.situacao
        )
        return functions
            .getHttpsCallable("addIrregularidade")
            .call(data)
            .continueWith { task ->
                // convertendo o resultado em string Json válida
                val res = gson.toJson(task.result?.data)
                res
            }
    }


}