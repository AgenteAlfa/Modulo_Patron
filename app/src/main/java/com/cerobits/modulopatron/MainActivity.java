package com.cerobits.modulopatron;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cerobits.patron.ControladorPatron;
import com.cerobits.patron.Patron;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch Editar;
    Patron mPatron;
    ControladorPatron CPViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button Aceptar = findViewById(R.id.btnAceptar), Reiniciar = findViewById(R.id.btnReiniciar);
        mPatron = findViewById(R.id.patron);
        TextView txtMarcado = findViewById(R.id.txtMarcado), txtClave = findViewById(R.id.txtClave);
        Editar = findViewById(R.id.swcEditar);

        CPViewModel = new ControladorPatron(getApplication());

        CPViewModel.getClave().observe(this,integers -> {txtClave.setText(integers.size() > 0? IdsATxt(integers) : "-"); Editar.setEnabled(integers.size() != 0);});
        CPViewModel.getMarcado().observe(this,integers -> txtMarcado.setText(integers.size() > 0? IdsATxt(integers) : "-"));
        CPViewModel.getIsEditable().observe(this,esEditable -> {
            mPatron.setModo(esEditable? Patron.MODO.ESPERA : Patron.MODO.SIMPLE);
            Log.d(TAG, "onCreate: Se cambio de modo a " + mPatron.getModo());
            Aceptar.setEnabled(esEditable);
            Reiniciar.setEnabled(esEditable);
            Resetear();
        });

        Editar.setOnCheckedChangeListener((compoundButton, b) -> CPViewModel.ActualizarEsEditable(b));

        mPatron.setOnPatternListener(new Patron.OnPatternListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(ArrayList<Integer> ids) {
                CPViewModel.ActualizarMarcado(ids);
            }

            @Override
            public boolean onComplete(ArrayList<Integer> ids) {

                CPViewModel.ActualizarMarcado(new ArrayList<>());
                if (mPatron.getModo() == Patron.MODO.SIMPLE)
                    try {
                        assert  CPViewModel.getClave().getValue() != null;
                        boolean resp = CPViewModel.getClave().getValue().equals(ids);
                        Toast.makeText(MainActivity.this, "Clave " + (resp? "correcta" : "equivocada"), Toast.LENGTH_SHORT).show();
                        return resp;

                    }catch (Exception e)
                    {
                        Toast.makeText(MainActivity.this, "Ocurrio un error", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onComplete: " + e.getMessage());
                        return false;
                    }
                else return false;
            }
        });



        Reiniciar.setOnClickListener(view -> {
            Resetear();
        });
        Aceptar.setOnClickListener(view -> {
            CPViewModel.ActualizarClave(mPatron.generateSelectedIds());
            Resetear();
        });

    }

    private void Resetear()
    {
        mPatron.reset();
        CPViewModel.ActualizarMarcado(mPatron.generateSelectedIds());
    }

    private String IdsATxt(List<Integer> arr){
        StringBuilder builder = new StringBuilder();
        for (Integer integer : arr) {
            builder.append(integer).append(" ");
        }
        return builder.toString();
    }




}