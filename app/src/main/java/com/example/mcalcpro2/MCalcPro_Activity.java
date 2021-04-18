package com.example.mcalcpro2;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;
import ca.roumani.i2c.MPro;

public class MCalcPro_Activity extends AppCompatActivity implements TextToSpeech.OnInitListener, SensorEventListener
{
    private TextToSpeech tts;
    final int ACCELERATION_THRESHOLD = 20;
    final int INITIAL_MONTHS = 4;
    public final int MORTGAGE_YEARS_PAID = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mcalcpro_layout);
        this.tts = new TextToSpeech(this, this);
        SensorManager sn = (SensorManager) getSystemService(SENSOR_SERVICE);
        sn.registerListener(this, sn.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

    }
    //Listening method for text to speach
    public void onInit(int initStatus)
    {
        this.tts.setLanguage(Locale.US);
    }
    //Listening method for speed sensor on device
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }
    //This method is invoked whenever the device is shaken
    //It will empty the 3 input box310dpes and the output box if the device is moving
    //over 20 m/s^2
    public void onSensorChanged(SensorEvent event){
        double ax = event.values[0];
        double ay = event.values[1];
        double az = event.values[2];
        double a = Math.sqrt(Math.pow(ax,2)+Math.pow(ay,2)+Math.pow(az,2));
        //If the device is accelerated beyond 20 m/s^2 thus boolean expression will evaluate to true
        if(a > ACCELERATION_THRESHOLD){
            ((EditText) findViewById(R.id.pBox)).setText("");
            ((EditText) findViewById(R.id.aBox)).setText("");
            ((EditText) findViewById(R.id.iBox)).setText("");
            ((TextView) findViewById(R.id.output)).setText("");
        }
    }
    public void buttonClicked(View V){
        try
        {
            EditText principleView = (EditText) findViewById(R.id.pBox);
            String principle = principleView.getText().toString();
            EditText amoritizationView = (EditText) findViewById(R.id.aBox);
            String amoritization = amoritizationView.getText().toString();
            EditText interestView = (EditText) findViewById(R.id.iBox);
            String interest = interestView.getText().toString();
            MPro mp = new MPro();
            mp.setPrinciple(principle);
            mp.setAmortization(amoritization);
            mp.setInterest(interest);
            String s = "Monthly Payment = " + mp.computePayment("%,.2f");
            s += "\n\nBy making this payment monthly for\n20 years, the motgage will be paid in full.But if";
            s += "you terminate the morgate on its nth\nanniversary, the balance still owing depends";
            s += "on n as shown below";
            s += "\n\n\t\t n      Balance";
            for(int i = 0; i<=INITIAL_MONTHS; i++){
                s += "\n\n";
                s += String.format("%8d", i) + mp.outstandingAfter(i, "%,16.0f");
            }
            for(int i = 5; i<= MORTGAGE_YEARS_PAID; i += 5){
                s += "\n\n";
                s += String.format("%8d", i) + mp.outstandingAfter(i, "%,16.0f");
            }
            tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
            ((TextView) findViewById(R. id.output)).setText(s);
        }
        catch (Exception e){
            Toast label = Toast.makeText(this,e.getMessage(), Toast.LENGTH_SHORT);
            label.show();
        }
    }
}