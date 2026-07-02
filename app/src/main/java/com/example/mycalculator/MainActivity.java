package com.example.mycalculator;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private TextView displayResult;
    private boolean lastNumeric = false;
    private boolean stateError = false;
    private boolean lastDot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        displayResult = findViewById(R.id.displayResult);

        setNumericOnClickListener();
        setOperatorOnClickListener();
    }

    private void setNumericOnClickListener() {
        View.OnClickListener listener = v -> {
            MaterialButton button = (MaterialButton) v;
            if (stateError) {
                displayResult.setText(button.getText());
                stateError = false;
            } else {
                displayResult.append(button.getText());
            }
            lastNumeric = true;
        };

        findViewById(R.id.btn0).setOnClickListener(listener);
        findViewById(R.id.btn1).setOnClickListener(listener);
        findViewById(R.id.btn2).setOnClickListener(listener);
        findViewById(R.id.btn3).setOnClickListener(listener);
        findViewById(R.id.btn4).setOnClickListener(listener);
        findViewById(R.id.btn5).setOnClickListener(listener);
        findViewById(R.id.btn6).setOnClickListener(listener);
        findViewById(R.id.btn7).setOnClickListener(listener);
        findViewById(R.id.btn8).setOnClickListener(listener);
        findViewById(R.id.btn9).setOnClickListener(listener);
        findViewById(R.id.btnDot).setOnClickListener(v -> {
            if (lastNumeric && !stateError && !lastDot) {
                displayResult.append(".");
                lastNumeric = false;
                lastDot = true;
            }
        });
    }

    private void setOperatorOnClickListener() {
        View.OnClickListener listener = v -> {
            if (lastNumeric && !stateError) {
                MaterialButton button = (MaterialButton) v;
                displayResult.append(button.getText());
                lastNumeric = false;
                lastDot = false;
            }
        };

        findViewById(R.id.btnPlus).setOnClickListener(listener);
        findViewById(R.id.btnMinus).setOnClickListener(listener);
        findViewById(R.id.btnMultiply).setOnClickListener(listener);
        findViewById(R.id.btnDivide).setOnClickListener(listener);
        findViewById(R.id.btnPercent).setOnClickListener(v -> {
            if (lastNumeric && !stateError) {
                displayResult.append("%");
                lastNumeric = false;
                lastDot = false;
            }
        });

        findViewById(R.id.btnC).setOnClickListener(v -> {
            displayResult.setText("");
            lastNumeric = false;
            stateError = false;
            lastDot = false;
        });

        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            String text = displayResult.getText().toString();
            if (text.length() > 0) {
                displayResult.setText(text.substring(0, text.length() - 1));
                String newText = displayResult.getText().toString();
                if (newText.length() > 0) {
                    char lastChar = newText.charAt(newText.length() - 1);
                    lastNumeric = Character.isDigit(lastChar);
                    lastDot = (lastChar == '.');
                } else {
                    lastNumeric = false;
                    lastDot = false;
                }
            }
        });

        findViewById(R.id.btnEquals).setOnClickListener(v -> onEqual());
    }

    private void onEqual() {
        if (lastNumeric && !stateError) {
            String txt = displayResult.getText().toString();
            try {
                // Replacing operators for evaluation
                String expression = txt.replace('×', '*').replace('÷', '/');
                // Percent is handled as /100 for simplicity here
                expression = expression.replace("%", "/100");
                
                double result = eval(expression);
                displayResult.setText(new DecimalFormat("0.######").format(result));
                lastNumeric = true;
                lastDot = displayResult.getText().toString().contains(".");
            } catch (Exception ex) {
                displayResult.setText("Error");
                stateError = true;
                lastNumeric = false;
            }
        }
    }

    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                return x;
            }
        }.parse();
    }
}