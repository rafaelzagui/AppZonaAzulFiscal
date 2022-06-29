package br.com.puc.appfiscalzonaazul

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import br.com.puc.appfiscalzonaazul.databinding.ActivityTelaConsultaBinding
import br.com.puc.appfiscalzonaazul.functions.FunctionsGenericResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder

class TelaConsultaActivity : AppCompatActivity() {


    private lateinit var binding : ActivityTelaConsultaBinding

    private lateinit var tvStatus : AppCompatTextView


    private lateinit var functions: FirebaseFunctions

    // usando o logcat (ferramenta do Android studio para verificar a saída)
    private val logEntry = "Consulta Veiculo";

    // instanciando um objeto gson
    private val gson = GsonBuilder().enableComplexMapKeySerialization().create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTelaConsultaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // inicializar o uso dos functions NA REGIAO DESEJADA
        functions = Firebase.functions("southamerica-east1")

        binding.btnItinerario.setOnClickListener {
            val intent = Intent(this,TelaMapsActivity ::class.java)
            startActivity(intent)


        }

        binding.btnConsultar.setOnClickListener {
            val p = Veiculo(binding.etPlaca.text.toString(),"")
            ConsultaVeiculo(p)
                .addOnCompleteListener(OnCompleteListener { task ->
                    if(binding.etPlaca.text.isNullOrEmpty())
                    {
                        Toast.makeText(binding.root.context, "Campo da placa vazio ", Toast.LENGTH_SHORT).show()
                    }
                     if(binding.etPlaca.length() !=8)
                    {
                        Toast.makeText(binding.root.context,"Digite uma placa valida",Toast.LENGTH_SHORT).show()
                    }
                    else if (!task.isSuccessful) {

                        val e = task.exception
                        if (e is FirebaseFunctionsException) {
                            val code = e.code
                            val details = e.details
                        }
                        // tratar a exceção...
                    }else{
                        // convertendo.
                        val genericResp = gson.fromJson(task.result, FunctionsGenericResponse::class.java)
                        val resultado = genericResp.payload.toString()
                        if(resultado =="Veiculo nao encontrado")
                        {
                            val texto = "Resultado:\nPlaca:${binding.etPlaca.text}\n está Irregular"
                            binding.tvStatus.text=texto
                        }else{

                            val texto = "Resultado:\n Placa: ${binding.etPlaca.text} Regular"
                            binding.tvStatus.text=texto

                        }

                    }
                })
        }

        binding.btnRegistrarIrregularidade.setOnClickListener{
            if(binding.etPlaca.text.isNullOrEmpty())
            {
                Snackbar.make(binding.root,"Placa Nula",Snackbar.LENGTH_SHORT).show()
            }
            if(binding.etPlaca.length() !=8) {
            Toast.makeText(binding.root.context, "Placa Digitada Invalida", Toast.LENGTH_SHORT).show()
        }
        else {
                abrirTelaIrregularidade()
            }

        }
    }

    private fun abrirTelaIrregularidade()
    {
        val placa = binding.etPlaca.text.toString().toUpperCase()
        val intentCamera = Intent(this,TelaRegistrarIrregularidadeActivity::class.java)
        intentCamera.putExtra("placa",placa)
        startActivity(intentCamera)
    }
    private fun ConsultaVeiculo(p: Veiculo): Task<String> {
        val data = hashMapOf(
            "placa" to p.placa,
            "situacao" to p.situacao
        )
        return functions
            .getHttpsCallable("todosVeiculosBD")
            .call(data)
            .continueWith { task ->
                // se faz necessario transformar a string de retorno como uma string Json valida.
                val res = gson.toJson(task.result?.data)
                res
            }
    }



}