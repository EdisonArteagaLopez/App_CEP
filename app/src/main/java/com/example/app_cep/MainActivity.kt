package com.example.app_cep

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val et_Cep = findViewById<EditText>(R.id.et_Cep)
        val btn_Consultar = findViewById<Button>(R.id.btn_consultar)

        btn_Consultar.setOnClickListener {
            val cep = et_Cep.text.toString()
            if (cep.isNotEmpty()) {
                consultarApi(cep)
            } else {
                et_Cep.error = "Por favor, ingressar um CEP"
            }
        }
    }

    fun consultarApi(cep: String) {
        val url = "https://brasilapi.com.br/api/cep/v2/$cep"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    runOnUiThread {
                        actualizarInterfaz(body) // Volvemos al hilo principal
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "CEP no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun actualizarInterfaz(jsonString: String?) {
        if (jsonString == null) return
        try {
            val json = JSONObject(jsonString)

            findViewById<TextView>(R.id.tv_logradouro).text = "Calle: ${json.optString("street")}"
            findViewById<TextView>(R.id.tv_cidade).text = "Ciudad: ${json.optString("city")}"
            findViewById<TextView>(R.id.tv_estado).text = "Estado: ${json.optString("state")}"
        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar datos", Toast.LENGTH_SHORT).show()
        }
    }
}