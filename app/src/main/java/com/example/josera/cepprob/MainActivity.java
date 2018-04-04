package com.example.josera.cepprob;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
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
    private TextView txtYi;
    private TextView txtRi;
    private TextView txtDesc;

    // Para calculos
    double d, k, h, p, e, a, b, fx;
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
        txtYi = (TextView) findViewById(R.id.txtYi);
        txtRi = (TextView) findViewById(R.id.txtRi);
        txtDesc = (TextView) findViewById(R.id.txtDesc);
        FloatingActionButton btnCal = (FloatingActionButton) findViewById(R.id.btnCal);

        btnCal.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        DialogSalir dialog = new DialogSalir();
        dialog.show(fm, "TagSalir");
    }

    @Override
    public void onClick(View view) {
        if (emptyFields(txtD, txtK, txtH, txtP, txtE, txtA, txtB)) {
            dialogEmpty();
        }
        else {
            calFx();
            if (isZero(h, d, fx)) {
                snackZeroValues();
            }
            else {
                if (haySolFactibles()) {
                    calFx();
                    correrPasos();
                    mostrarResultados();

                    for (String r : listR) {
                        Log.i("R: ", r);
                    }
                }
                else {
                    Toast.makeText(this,"No hay soluciones Factibles.",Toast.LENGTH_LONG).show();
                }
            }
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

    private void mostrarResultados() {
        // Truncando el resultado a seis decimales.
        String yi = String.valueOf(Math.round(yiOptimo*1000000.0)/1000000.0);
        String ri = String.valueOf(Math.round(RiOptimo*1000000.0)/1000000.0);

        txtYi.setText("Yi*: " + yi);
        txtRi.setText("Ri*: " + ri);
        txtDesc.setText("Pedir " +yi+ " unidades siempre que el nivel de existencias baje a "+ri);
        txtDesc.setVisibility(View.VISIBLE);
    }

    private void calFx() {
        getDatos();
        if (b > a)
            fx = b - a;
        else
            fx = 0;
    }

    private double calOptimo(double s) {
        return Math.sqrt((2*d*(k + p*s))/h);
    }

    private double calYChapo() {
        return Math.sqrt((2*d*(k + p*e))/h);
    }

    private double calYRaya() {
        return (p*d)/h;
    }

    private double calHYsobrePD(double y) {
        return (h*y)/(p*d);
    }

    private double calR(double y) {
        return ((calHYsobrePD(y) - 1)*fx)*(-1);
    }

    private double calS(double R) {
        return ((Math.pow(R,2)) / (fx*2) - (R) + (fx/2));
    }

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

        getDatos();

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
        getDatos();
        double yChapo = calYChapo();
        double yRaya = calYRaya();

        return yRaya > yChapo;
    }

    private boolean emptyFields(EditText... edt) {
        for(EditText textField : edt) {
            if(textField.getText().length() == 0) {
                return true;
            }
        }
        return  false;
    }

    private boolean isZero(double... args) {
        for(double val : args) {
            if (val == 0)
                return true;
        }
        return false;
    }

    public void dialogEmpty() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DialogEmpty empty = new DialogEmpty();
        empty.show(fragmentManager, "TagEmpty");
    }

    public void snackZeroValues() {
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.coordinatorLayout), "Hay datos incorrectos.", Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
