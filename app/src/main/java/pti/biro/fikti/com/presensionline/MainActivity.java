package pti.biro.fikti.com.presensionline;

/**
 * Created by Jibril Hartri Putra on 01/12/2017.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.Result;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private String id_kegiatan,nama_kegiatan, tgl_kegiatan, ket_kegiatan;
    private  String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences atur = getSharedPreferences("AbsenOn", 0);
        id_kegiatan = atur.getString("id_kegiatan", "DEFAULT");
        nama_kegiatan = atur.getString("nama_kegiatan","DEFAULT");
        tgl_kegiatan = atur.getString("tgl_kegiatan","DEFAULT");
        ket_kegiatan = atur.getString("ket_keterangan","DEFAULT");
    }
    public void QrScanner(View view){


        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);

        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();         // Start camera

    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here

        Log.e("handler", rawResult.getText()); // Prints scan results
        Log.e("handler", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode)

        // show the scanner result into dialog box.
        VerifikasiProses oke = new VerifikasiProses(id_kegiatan,rawResult.getText());
        oke.execute((Void) null);


        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }


    private class VerifikasiProses extends AsyncTask<Void,Void,String> {

        private final String mKeg;
        private final String mKey;

        VerifikasiProses (String Keg, String Key){
            mKeg = Keg;
            mKey = Key;
        }

        private String resp;
        ProgressDialog progressDialog;




        @Override
        protected String doInBackground(Void... params) {

            try {
                // Simulate network access.
                URL url = new URL("http://belajar.ccug.gunadarma.ac.id/absenon/handler.php");

                JSONObject data_post =  new JSONObject();
                data_post.put("kegiatan",mKeg);
                data_post.put("key",mKey);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(data_post));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(
                            new InputStreamReader(
                                    conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    //resp = sb.toString();
                    return sb.toString();

                }
                else {
                    return "haduh";
                }
            }
            catch(Exception e){
                // resp = e.getMessage();
                return e.getMessage();
            }
        }


        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Memproses Presensi",
                    "Tunggu sebentar.. " );
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation

            progressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            try {
                JSONObject jso = new JSONObject(result);
                String now_login = jso.getString("status");

                if (now_login.equalsIgnoreCase("galat")) {
                    builder.setTitle("Verifikasi gagal");
                    builder.setMessage("Silahkan periksa kembali QR Code yang anda 'scan'");

                } else {
                    builder.setTitle("Verifikasi berhasil");
                    String nama = jso.getString("nama");
                    String pukul = jso.getString("pukul");
                    builder.setMessage(
                    "Nama :" + nama + "\n" +
                    "Pukul : " + pukul
                    );

                }
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(),"Galat " + ex.getMessage(),Toast.LENGTH_SHORT);
            }



            AlertDialog alert1 = builder.create();
            alert1.show();
            mScannerView.resumeCameraPreview(MainActivity.this);

        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

}
