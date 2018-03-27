package com.example.josera.cepprob;

import android.app.Dialog;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText txtD;
    private EditText txtK;
    private EditText txtH;
    private EditText txtP;
    private EditText txtE;
    private EditText txtA;
    private EditText txtB;
    private TextView txtResult;
    private TextView txtDesc;

    // Para calculos
    double d, k, h, p, e, a, b;
    double yiOptimo, RiOptimo;
    private List<String> listR = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtD = (EditText) findViewById(R.id.txtD);
        txtK = (EditText) findViewById(R.id.txtK);
        txtH = (EditText) findViewById(R.id.txtH);
        txtP = (EditText) findViewById(R.id.txtP);
        txtE = (EditText) findViewById(R.id.txtE);
        txtA = (EditText) findViewById(R.id.txtA);
        txtB = (EditText) findViewById(R.id.txtB);
        txtResult = (TextView) findViewById(R.id.txtResult);
        txtDesc = (TextView) findViewById(R.id.txtDesc);
        Button btnCal = (Button) findViewById(R.id.btnCal);

        btnCal.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (haySolFactibles()) {
            correrPasos();
            String yi = String.valueOf(yiOptimo);
            String ri = String.valueOf(RiOptimo);

            txtResult.setText("Yi*: " + yi + " Ri: " + ri);
            txtDesc.setText("Pedir " +yi+ " unidades cuando el inventario baje a "+ri);
            txtDesc.setVisibility(View.VISIBLE);

            for (String r : listR) {
                Log.i("R: ", r);
            }
        }
        else {
            Toast.makeText(this,"No hay soluciones Factibles.",Toast.LENGTH_LONG).show();
            txtResult.setText(String.valueOf(calHYsobrePD(316.23)));
        }
    }

    public void getDatos() {
        d = Float.parseFloat(String.valueOf(txtD.getText()));
        k = Float.parseFloat(String.valueOf(txtK.getText()));
        h = Float.parseFloat(String.valueOf(txtH.getText()));
        p = Float.parseFloat(String.valueOf(txtP.getText()));
        e = Float.parseFloat(String.valueOf(txtE.getText()));
        a = Float.parseFloat(String.valueOf(txtA.getText()));
        b = Float.parseFloat(String.valueOf(txtB.getText()));
    }

    private double calOptimo(double s) {
        getDatos();
        double raiz = Math.sqrt((2*d*(k + p*s))/h);
        return Math.round(raiz * 1000000.0) / 1000000.0;
    }

    private double calYChapo() {
        getDatos();

        return Math.sqrt((2*d*(k + p*e))/h);
    }

    private double calYRaya() {
        getDatos();

        return (p*d)/h;
    }

    private double calHYsobrePD(double y) {
        getDatos();
        return (h*y)/(p*d);
    }

    private double calR(double y) {
        getDatos();
        double R = ((calHYsobrePD(y) - 1)*b)*(-1);
        return Math.round(R * 1000000.0) / 1000000.0;
    }

    private double calS(double R) {
        getDatos();
        double S = ((Math.pow(R,2)) / (b*2) - (R) + (b/2));
        return Math.round(S * 1000000.0) / 1000000.0;
    }

    // Hace mal el calculo
    private boolean aproximacion(double Ri, double RiAnt) {
        double diff = Math.round((RiAnt - Ri)*1000000.0)/ 1000000.0;

        DecimalFormat df = new DecimalFormat("###.######");

        Log.i("RiAnt: ", Double.toString(RiAnt));
        Log.i("Ri: ", Double.toString(Ri));
        Log.i("Diff: ",df.format(diff));
        return diff == 0.000001;
    }

    private void correrPasos() {
        int i = 0;
        double Yi = 0;
        double Ri = 0;
        double RiAnt = 0;
        double S;
        boolean seguir = true;

        while (seguir && i < 20) {
            if (i == 0) {
                Yi = calOptimo(0);
                Ri = calR(Yi);
                listR.add(Double.toString(Ri));
                RiAnt = Ri;
            }
            else {
                S = calS(RiAnt);
                Yi = calOptimo(S);
                Ri = calR(Yi);
                listR.add(Double.toString(Ri));
                if (aproximacion(Ri, RiAnt)) {
                    Log.i("Sali por Aprox." , Integer.toString(i));
                    seguir = false;
                }
                else {
                    RiAnt = Ri;
                }
            }
            i++;
        }
        yiOptimo = Yi;
        RiOptimo = Ri;
    }

    private boolean haySolFactibles() {
        double yChapo = calYChapo();
        double yRaya = calYRaya();

        return yRaya > yChapo;
    }
}
