package pti.biro.fikti.com.presensionline;

import android.content.SharedPreferences;

/**
 * Created by Jibril Hartri Putra on 01/12/2017.
 */

public class Penyimpanan  extends  LoginActivity{
    public void simpan (String id_kegiatan, String nama_kegiatan, String tgl_kegiatan, String ket_kegiatan) {
        try {
            SharedPreferences atur =  this.getApplicationContext().getSharedPreferences("Absenon",0);
            SharedPreferences.Editor editor = atur.edit();
            editor.putString("id_kegiatan", id_kegiatan);
            editor.putString("nama_kegiatan", nama_kegiatan);
            editor.putString("tgl_kegiatan", tgl_kegiatan);
            editor.putString("ket_kegiatan", ket_kegiatan);

            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void sip (String id_kegiatan, String nama_kegiatan, String tgl_kegiatan, String ket_kegiatan) {
      Penyimpanan oop = new Penyimpanan();
        oop.simpan(id_kegiatan,nama_kegiatan,tgl_kegiatan,ket_kegiatan);
    }
}
