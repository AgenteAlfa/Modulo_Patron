package com.cerobits.patron;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class ControladorPatron extends AndroidViewModel {

    private final MutableLiveData<List<Integer>> Clave;
    private final MutableLiveData<List<Integer>> Marcado;
    private final MutableLiveData<Boolean> isEditable;
    private final Repositorio mRepositorio;


    public ControladorPatron(@NonNull Application application) {
        super(application);
        mRepositorio = new Repositorio(application);
        Marcado = new MutableLiveData<>(new ArrayList<>());
        Clave = mRepositorio.getClave();
        isEditable = new MutableLiveData<>(true);


    }
    public MutableLiveData<Boolean> getIsEditable() {
        return isEditable;
    }

    public MutableLiveData<List<Integer>> getClave() {
        return Clave;
    }

    public MutableLiveData<List<Integer>> getMarcado() {
        return Marcado;
    }

    public void ActualizarMarcado(List<Integer> arr){
        Marcado.postValue(arr);
    }

    public void ActualizarClave(List<Integer> arr)
    {
        mRepositorio.setClave(arr);
        Clave.postValue(arr);
    }

    public void ActualizarEsEditable(boolean b)
    {
        isEditable.postValue(b);
    }

    private static class Repositorio {
        private static final String TAG = "Repositorio";
        private final SharedPreferences Configuracion;
        public Repositorio(@NonNull Application application) {
             Configuracion = PreferenceManager.getDefaultSharedPreferences(application);
        }
        protected MutableLiveData<List<Integer>> getClave(){
            ArrayList<Integer> arr = new ArrayList<>();
            for (char s : Configuracion.getString("Clave","").toCharArray()) {
                Log.d(TAG, "getClave: -" + s + "-" );
                arr.add(Integer.valueOf("" + s));
            }
            return new MutableLiveData<>(arr);
        }
        protected void setClave(List<Integer> arr){
            StringBuilder builder = new StringBuilder();
            for (Integer integer : arr) {
                builder.append(integer);
            }
            Configuracion.edit().putString("Clave",builder.toString()).apply();
        }
    }
}
