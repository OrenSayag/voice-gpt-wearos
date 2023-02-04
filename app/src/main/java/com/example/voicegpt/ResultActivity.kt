package com.example.voicegpt

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import com.example.voicegpt.R.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.*


class ResultActivity : Activity() {
    private var client: OkHttpClient = OkHttpClient()
    private var request = OkHttpRequest(client)

    var tts: TextToSpeech? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        if (tts == null) {
            tts = TextToSpeech(
                this
            ) { status ->
                if (status != TextToSpeech.ERROR) {
                    tts?.setLanguage(Locale.US)
                    tts?.setPitch(0F)
                    tts?.setSpeechRate(1F)
                }
            }
        }

        val extras = intent.extras
        var spokenText = ""
        if (extras != null) {
            spokenText = extras.getString("spokenText") ?: ""
        }
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_result_loading)
        queryGPT(spokenText)
    }

    override fun onDestroy() {
        tts?.shutdown()
        println("Destroyed result")
        super.onDestroy()
    }

    private fun queryGPT(spokenText: String) {
        val requestUrl =
            getString(string.backend_base) + getString(string.query_gpt_endpoint) + spokenText

        request.GET(requestUrl, MyCallback(::changeLayout))
    }

    class MyCallback(private val changeLayout: (Int, String?) -> Unit) : Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("http request to backend query-gpt failed")
            changeLayout(1, null)
        }

        override fun onResponse(call: Call, response: Response) {
            println("successful response from backend query-gpt")
            changeLayout(2, response.body?.string())
        }
    }

    private fun changeLayout(layout: Int, resultText: String? = null) {
        // 1 - error
        // 2 - result
        // 2 - main

        runOnUiThread {
            if (layout == 1) {
                setContentView(R.layout.activity_error)
                val goBackButton = findViewById<Button>(id.back_to_main_activity_button)
                goBackButton.setOnClickListener { changeLayout(3) }
            }
            if (layout == 2) {
                setContentView(R.layout.activity_result)
                val goBackButton = findViewById<Button>(id.back_to_main_activity_button)
                goBackButton.setOnClickListener { changeLayout(3) }
                val resultContainer = findViewById<TextView>(id.result_container)
                resultContainer.text = resultText
                if (resultText?.isNotEmpty() == true) {
                    tts?.speak(resultText, TextToSpeech.QUEUE_FLUSH, null, "test")
                }
            }
            if (layout == 3) {
                tts?.stop()
                val mainActivityIntent = Intent(this, MainActivity::class.java)
                startActivity(mainActivityIntent)
            }
        }

    }

    private fun restartActivity() {
        val intent = intent
        finish()
        startActivity(intent)
    }

}