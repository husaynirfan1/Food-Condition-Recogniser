package app.ij.mlwithtensorflowlite;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class FruitConditionResult extends AppCompatActivity {

    public ImageView fruitIcon;
    public TextView fruitConditionTV, fruitNameTV, fruitCalorieTV, accuracyTV;

    public ImageView homeBtn;
    public SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_condition_result);
        preferences = getSharedPreferences(
                "app.ij.mlwithtensorflowlite", Context.MODE_PRIVATE);

        fruitIcon = findViewById(R.id.fruitIcon);
        accuracyTV = findViewById(R.id.accuracyTV);
        fruitCalorieTV = findViewById(R.id.fruitCalorieTV);
        fruitNameTV = findViewById(R.id.fruitNameTV);
        fruitConditionTV = findViewById(R.id.fruitConditionTV);
        homeBtn = findViewById(R.id.homeBtn);
        homeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            FruitConditionResult.this.finish();
            preferences.edit().clear().commit();

        });
        SharedPreferences preferences = getSharedPreferences(
                "app.ij.mlwithtensorflowlite", Context.MODE_PRIVATE);
        String name = preferences.getString("Type", "");
        String confidence = preferences.getString("Confidence", "");
        String condition = preferences.getString("Condition", "");
        String conditionconf = preferences.getString("Condition Confidence", "");

        if (!name.equalsIgnoreCase("")){
            switch (name){
                case "Apple":
                    fruitIcon.setImageResource(R.drawable.greenapple);
                    fruitNameTV.setText("Fruit  :   Apple ");
                    fruitCalorieTV.setText("Calorie :   52");
                    break;
                case "Orange":
                    fruitIcon.setImageResource(R.drawable.orange);
                    fruitNameTV.setText("Fruit  :   Orange ");
                    fruitCalorieTV.setText("Calorie :   47");
                    break;
                case "Banana":
                    fruitIcon.setImageResource(R.drawable.banana);
                    fruitNameTV.setText("Fruit  :   Banana ");
                    fruitCalorieTV.setText("Calorie  :   89");
                    break;
                case "Onion":
                    fruitIcon.setImageResource(R.drawable.onion);
                    fruitNameTV.setText("Fruit  :   Onion ");
                    fruitCalorieTV.setText("Calorie :   40");
                    break;
                default:break;
            }
        }
        if (!condition.equalsIgnoreCase("")){
            fruitConditionTV.setText("Condition :   "+condition+" ");
        }
        if (!confidence.equalsIgnoreCase("")){
            accuracyTV.setText(confidence);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        FruitConditionResult.this.finish();
        preferences.edit().clear().commit();

    }
}