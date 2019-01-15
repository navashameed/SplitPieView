package navas.com.slidingpiewidget;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import navas.com.slidingpiewidget.view.SplitPieView;


public class MainActivity extends AppCompatActivity {

    RelativeLayout containerRel;
    SplitPieView splitPieView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        containerRel = findViewById(R.id.widgetRelative);
        splitPieView = findViewById(R.id.splitPieView);

        //splitPieView = new SplitPieView(this, null, 100.00);
        //containerRel.addView(splitPieView);
        splitPieView.setNumberOfPortions(5);
    }

    public void add(View v) {
        splitPieView.add();
    }

    public void remove(View v) {
        splitPieView.remove();
    }

    public void showValues(View v) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Values");
        String values = getValuesFromChart();
        alertDialog.setMessage(values);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private String getValuesFromChart() {
        StringBuilder stringBuilder = new StringBuilder();
        double[] values = splitPieView.getAmountValueList();
        int[] isSelected = splitPieView.getSelectedStatus();
        int numberOfPortions = splitPieView.getNumberOfPortions();

        for (int i = 0; i < numberOfPortions; i++) {
            stringBuilder.append("Value" + (i + 1) + ": " + (int)values[i] + ", isSelected: " + (isSelected[i] == 0 ? "false" : "true") + ",\n");
        }

        return stringBuilder.toString();

    }
}
