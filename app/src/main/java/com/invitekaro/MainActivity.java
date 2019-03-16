package com.invitekaro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.invitekaro.model.Card;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PaymentResultListener {

    ArrayList<Card> cardList = new ArrayList<>();
    private String coupon_code = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String json = null;
        try {
            InputStream is = getAssets().open("data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

            JSONObject obj = new JSONObject(json);
            JSONArray m_jArry = obj.getJSONArray("cards");

            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                Card location = new Card();
                location.setCardType(jo_inside.getString("cardType"));
                location.setCardName(jo_inside.getString("cardName"));
                location.setCardDetail(jo_inside.getString("cardDetail"));


                //Add your values in your `ArrayList` as below:
                cardList.add(location);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.parent);
        RelativeLayout checkout = (RelativeLayout)findViewById(R.id.checkout);
        LinearLayout applyCoupon = (LinearLayout)findViewById(R.id.applyCoupon);
        LayoutInflater layoutInflater = getLayoutInflater();
        View view;

        for (int i=0;i<cardList.size();i++){
            view = layoutInflater.inflate(R.layout.item_view, parentLayout, false);

            TextView cardType = (TextView)view.findViewById(R.id.cardType);
            TextView cardName = (TextView)view.findViewById(R.id.cardName);
            TextView cardDetail = (TextView)view.findViewById(R.id.cardDetail);
            cardType.setText(cardList.get(i).getCardType());
            cardName.setText(cardList.get(i).getCardName());
            cardDetail.setText(cardList.get(i).getCardDetail());


            parentLayout.addView(view);
        }

        Checkout.preload(getApplicationContext());
        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPayment();
            }
        });

        applyCoupon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Add Coupon");

                // Set up the input
                final EditText input = new EditText(MainActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        coupon_code = input.getText().toString();
                        applyVoucher(coupon_code);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });


    }

    public void startPayment() {
        /**
         * Instantiate Checkout
         */
        Checkout checkout = new Checkout();

        /**
         * Set your logo here
         */
        checkout.setImage(R.drawable.pink);

        /**
         * Reference to current activity
         */
        final Activity activity = this;

        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {
            JSONObject options = new JSONObject();

            /**
             * Merchant Name
             * eg: ACME Corp || HasGeek etc.
             */
            options.put("name", "Merchant Name");

            /**
             * Description can be anything
             * eg: Order #123123
             *     Invoice Payment
             *     etc.
             */
            options.put("description", "Order #123456");

            options.put("currency", "INR");

            /**
             * Amount is always passed in PAISE
             * Eg: "500" = Rs 5.00
             */
            options.put("amount", "500");

            checkout.open(activity, options);
        } catch(Exception e) {
            Log.e("TAG", "Error in starting Razorpay Checkout", e);
        }


    }

    @Override
    public void onPaymentSuccess(String s) {
        Log.e("TAG", "Success in starting Razorpay Checkout" + s);
    }

    @Override
    public void onPaymentError(int i, String s) {
        Log.e("TAG", "Error in starting Razorpay Checkout" + s);
    }

    public void applyVoucher(String coupon_code) {

        String apiKey     = "8531589b490dffc8e298a8b1faeae0f2";
        String merchantID = "ef993b6b-2a30-4211-a9a5-c375db7a8a15";

        Voucher voucher = Voucher.getInstance();
        voucher.initialize(merchantID, apiKey);
        voucher.setOnVoucherResultListener(new Voucher.OnVoucherResultListener() {
            @Override
            public void onResult(int requestType, int responseCode, String response) {

                switch(requestType){

                    case Voucher.CREATE_VOUCHER:

                        break;

                    case Voucher.CHECK_VOUCHER_VALIDITY:

                        break;

                    case Voucher.LOGIN:

                        break;

                    case Voucher.SIGN_UP:

                        break;

                    case Voucher.REDEEM_VOUCHER:

                        break;

                    case Voucher.VERIFY_API_CREDENTIAL:

                        break;
                }
            }
            @Override
            public void onError(int requestType, int responseCode, String errorMessage) {}

            @Override
            public void onException(int requestType, Exception e, String message) {}
        });

        try{
            voucher.login("email.Id@domain.com", "password");
            voucher.redeemVoucher("voucherId", "userId");
        }catch (Exception e){}

        //uncomment following code after credentials
        //voucher.login("email.Id@domain.com", "password");
        //voucher.redeemVoucher("voucherId", "userId");


    }
}
