package app.haikelilhamhakim.suratdispensasi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    private lateinit var txtInputNamaLengkap: EditText
    private lateinit var txtInputMataKuliah: EditText
    private lateinit var txtInputKelompok: EditText
    private lateinit var btnPilihPdf: Button
    private lateinit var btnAjukan: Button
    private lateinit var btnPilihGambar: Button
    private lateinit var txtShowPdfFilename: TextView
    private lateinit var txtShowImageFilename: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range", "Recycle")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        txtShowImageFilename = findViewById<TextView>(R.id.show_jpg_name)
        txtShowPdfFilename = findViewById<TextView>(R.id.show_pdf_name)

        if (requestCode == 1212 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data!!
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)

            assert(cursor !== null)
            val nameIndex: Int? = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor?.moveToFirst()
            val name: String? = nameIndex?.let { cursor?.getString(it) }
            cursor?.close()

            txtShowPdfFilename.text = name
        } else if (requestCode == 1010 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data!!
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)

            assert(cursor !== null)
            val nameIndex: Int? = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor?.moveToFirst()
            val name: String? = nameIndex?.let { cursor?.getString(it) }
            cursor?.close()

            txtShowImageFilename.text = name
        } else {
            Toast.makeText(this, "Tidak ada data yang diambil!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtInputNamaLengkap = findViewById<EditText>(R.id.text_input_namalengkap)
        txtInputMataKuliah = findViewById<EditText>(R.id.text_input_matakuliah)
        txtInputKelompok = findViewById<EditText>(R.id.text_input_kelompok)
        btnPilihPdf = findViewById<Button>(R.id.button_pilihpdf)
        btnPilihGambar = findViewById<Button>(R.id.button_pilihgambar)
        btnAjukan = findViewById<Button>(R.id.button_ajukan)

        btnPilihPdf.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, 1212)
        }

        btnPilihGambar.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/jpeg"
            startActivityForResult(intent, 1010)
        }

        btnAjukan.setOnClickListener {
            saveDataToMySQL()
        }
    }

    private fun saveDataToMySQL() {
        val namaLengkap = txtInputNamaLengkap.text.toString().trim()
        val mataKuliah = txtInputMataKuliah.text.toString().trim()
        val kelompok = txtInputKelompok.text.toString().trim()
        val showPdfFilename = txtShowPdfFilename.text.toString().trim()
        val showImageFilename = txtShowImageFilename.text.toString().trim()

        if (namaLengkap.isEmpty() || mataKuliah.isEmpty() || kelompok.isEmpty() || kelompok.isEmpty()) {
            Toast.makeText(this, "Data yang dimasukkan masih kurang lengkap!", Toast.LENGTH_SHORT)
                .show()
            return
        } else {
            SaveDataAsyncTask(this).execute(
                namaLengkap,
                mataKuliah,
                kelompok,
                showPdfFilename,
                showImageFilename
            )
        }
    }

    class SaveDataAsyncTask(context: Context) : AsyncTask<String, Void, String>() {
        private val ctx = context

        override fun doInBackground(vararg params: String): String {
            val namaLengkap = params[0]
            val mataKuliah = params[1]
            val kelompok = params[2]
            val showPdfFilename = params[3]
            val showImageFilename = params[4]

            try {
                val url = URL("http://192.168.1.7/matakuliah_ti5a/simpan_2111500068.php")
                val httpURLConnection = url.openConnection() as HttpURLConnection
                httpURLConnection.requestMethod = "POST"
                httpURLConnection.doOutput = true

                val outputStream = httpURLConnection.outputStream
                val bufferedWriter = BufferedWriter(OutputStreamWriter(outputStream, "UTF-8"))

                val data = URLEncoder.encode(
                    "nama_lengkap",
                    "UTF-8"
                ) + "=" + URLEncoder.encode(
                    namaLengkap,
                    "UTF-8"
                ) + "&" + URLEncoder.encode("mata_kuliah", "UTF-8") + "=" + URLEncoder.encode(
                    mataKuliah,
                    "UTF-8"
                ) + "&" + URLEncoder.encode("kelompok", "UTF-8") + "=" + URLEncoder.encode(
                    kelompok,
                    "UTF-8"
                ) + "&" + URLEncoder.encode("pdf_file_name", "UTF-8") + "=" + URLEncoder.encode(
                    showPdfFilename,
                    "UTF-8"
                ) + "&" + URLEncoder.encode("image_file_name", "UTF-8") + "=" + URLEncoder.encode(
                    showImageFilename,
                    "UTF-8"
                )

                bufferedWriter.write(data)
                bufferedWriter.flush()
                bufferedWriter.close()
                outputStream.close()

                val inputStream = httpURLConnection.inputStream
                val bufferedReader = BufferedReader(InputStreamReader(inputStream, "ISO-8859-1"))

                val result = StringBuilder()
                var line: String?

                while (bufferedReader.readLine().also { line = it } != null) {
                    result.append(line)
                }

                bufferedReader.close()
                inputStream.close()
                httpURLConnection.disconnect()

                return result.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                return "Error: ${e.message}"
            }
        }

        override fun onPostExecute(result: String?) {
            Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show()
        }
    }
}
