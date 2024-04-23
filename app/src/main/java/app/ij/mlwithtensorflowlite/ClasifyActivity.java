package app.ij.mlwithtensorflowlite;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class ClasifyActivity extends AppCompatActivity {

    public ImageView fruitIcon;
    public TextView textCond, fruitTV;
    public CountDownTimer count;
    public ConstraintLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clasify);

        mainLayout = findViewById(R.id.mainLayout);
        fruitIcon = findViewById(R.id.fruitIcon);
        textCond = findViewById(R.id.textCond);
        fruitTV = findViewById(R.id.fruitTV);
        SharedPreferences preferences = getSharedPreferences(
                "app.ij.mlwithtensorflowlite", Context.MODE_PRIVATE);
        String name = preferences.getString("Type", "");
        String confidence = preferences.getString("Confidence", "");
        String condition = preferences.getString("Condition", "");
        String conditionconf = preferences.getString("Condition Confidence", "");


        if(!name.equalsIgnoreCase(""))
        {
            count = new CountDownTimer(4000, 1000) {

                public void onTick(long millisUntilFinished) {
                    if (millisUntilFinished / 1000 == 3)
                    {
                        textCond.setText("Detecting Condition.");
                    }
                    else if (millisUntilFinished / 1000 == 2)
                    {
                        textCond.setText("Detecting Condition..");
                    }
                    else if (millisUntilFinished / 1000 == 1)
                    {
                        textCond.setText("Detecting Condition...");
                    }
                }

                public void onFinish() {
                    Intent i = new Intent(ClasifyActivity.this, FruitConditionResult.class);
                    startActivity(i);
                    finish();
                }

            }.start();
            switch (name){
                case "Apple":
                    mainLayout.setBackground(ContextCompat.getDrawable(ClasifyActivity.this, R.drawable.applebg));
                    fruitTV.setText("Apple");
                    fruitTV.setTextColor(ContextCompat.getColor(ClasifyActivity.this, R.color.red));
                    break;
                case "Orange":
                    mainLayout.setBackground(ContextCompat.getDrawable(ClasifyActivity.this, R.drawable.orangebg));
                    fruitTV.setText("Orange");
                    fruitTV.setTextColor(ContextCompat.getColor(ClasifyActivity.this, R.color.colorPrimary));

                    break;
                case "Banana":
                    mainLayout.setBackground(ContextCompat.getDrawable(ClasifyActivity.this, R.drawable.bananabg));
                    fruitTV.setText("Banana");
                    fruitTV.setTextColor(ContextCompat.getColor(ClasifyActivity.this, R.color.colorAccent));

                    break;
                case "Onion":
                    mainLayout.setBackground(ContextCompat.getDrawable(ClasifyActivity.this, R.drawable.onionbg));
                    fruitTV.setText("Onion");
                    fruitTV.setTextColor(ContextCompat.getColor(ClasifyActivity.this, R.color.lightbrown));

                    break;
                default:break;
            }
            Log.d("Type", name);
        }
        if(!confidence.equalsIgnoreCase(""))
        {
            Log.d("confidence", confidence);

        }
        if(!condition.equalsIgnoreCase(""))
        {
            Log.d("condition", condition);

        }
        if(!conditionconf.equalsIgnoreCase(""))
        {
            Log.d("conditionconf", conditionconf);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        count.cancel();
        finish();
    }
}