package com.invitekaro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    Voucher voucharInterface = null;

    ArrayList<Card> cardList = new ArrayList<>();
    private String voucherCode = "";
    private double checkoutPrize = 0.0;
    private double offerDiscount = 0.0;
    private double toPayTotal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here

        }

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
                Card cardItem = new Card();
                cardItem.setCardType(jo_inside.getString("cardType"));
                cardItem.setCardName(jo_inside.getString("cardName"));
                cardItem.setCardDetail(jo_inside.getString("cardDetail"));
                cardItem.setCardPrice(jo_inside.getString("cardPrice"));

                cardList.add(cardItem);

                checkoutPrize += cardItem.getCardPriceValue();
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LinearLayout parentLayout = (LinearLayout)findViewById(R.id.parent);
        RelativeLayout checkout = (RelativeLayout)findViewById(R.id.checkout);
        LinearLayout applyVoucher = (LinearLayout)findViewById(R.id.applyVoucher);
        LayoutInflater layoutInflater = getLayoutInflater();
        View view;

        for (int i=0; i<cardList.size(); i++){
            view = layoutInflater.inflate(R.layout.item_view, parentLayout, false);

            TextView cardType = (TextView)view.findViewById(R.id.cardType);
            TextView cardName = (TextView)view.findViewById(R.id.cardName);
            TextView cardDetail = (TextView)view.findViewById(R.id.cardDetail);
            TextView cardPrice = (TextView)view.findViewById(R.id.cardPrice);
            cardType.setText(cardList.get(i).getCardType());
            cardName.setText(cardList.get(i).getCardName());
            cardDetail.setText(cardList.get(i).getCardDetail());
            cardPrice.setText(getPriceDisplayValue(cardList.get(i).getCardPriceValue()));

            parentLayout.addView(view);
        }

        toPayTotal = checkoutPrize - offerDiscount;

        TextView priceValueText = (TextView)findViewById(R.id.itemTotal);
        priceValueText.setText(getPriceDisplayValue(checkoutPrize));
        priceValueText = (TextView)findViewById(R.id.offerDiscount);
        priceValueText.setText(getPriceDisplayValue(offerDiscount));
        priceValueText = (TextView)findViewById(R.id.toPayTotal);
        priceValueText.setText(getPriceDisplayValue(toPayTotal));

        Checkout.preload(getApplicationContext());
        initializeVoucharAPI();

        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPayment();
            }
        });

        applyVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Apply Coupon");

            final EditText input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                voucherCode = input.getText().toString();
                verifyAndStoreVoucherCode(voucherCode);
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
            options.put("amount", toPayTotal * 100);

            checkout.open(activity, options);
        } catch(Exception e) {
            Log.e("TAG", "Error in starting Razorpay Checkout", e);
        }


    }

    public String getPriceDisplayValue(double price){
        return "Rs. " + String.valueOf(price);
    }

    @Override
    public void onPaymentSuccess(String s) {
        Log.e("TAG", "Success in starting Razorpay Checkout" + s);
        applyVoucher();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Log.e("TAG", "Error in starting Razorpay Checkout" + s);
    }

    public void verifyAndStoreVoucherCode(String voucherCode){
        if (voucharInterface != null){
            voucharInterface.checkValidity(voucherCode);
        }
    }

    public void applyVoucher() {
        if (voucherCode != null && voucherCode.isEmpty()) {
            voucharInterface.redeemVoucher(voucherCode, "-1");
        }
    }

    public void initializeVoucharAPI(){
        String apiKey     = "8531589b490dffc8e298a8b1faeae0f2";
        String merchantID = "ef993b6b-2a30-4211-a9a5-c375db7a8a15";

        voucharInterface = Voucher.getInstance();
        voucharInterface.initialize(merchantID, apiKey);
        voucharInterface.setOnVoucherResultListener(new Voucher.OnVoucherResultListener() {
            @Override
            public void onResult(int requestType, int responseCode, String response) {

                Log.d("TAG", "onResult: " + requestType);
                switch(requestType){

                    case Voucher.CREATE_VOUCHER:

                        break;

                    case Voucher.CHECK_VOUCHER_VALIDITY:
                        handleVoucharValidation(response);
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
            public void onError(int requestType, int responseCode, String errorMessage) {
                switch(requestType){
                    case Voucher.CHECK_VOUCHER_VALIDITY:
                        handleVoucharValidation("");
                        break;
                }
            }

            @Override
            public void onException(int requestType, Exception e, String message) {
                switch(requestType){
                    case Voucher.CHECK_VOUCHER_VALIDITY:
                        handleVoucharValidation("");
                        break;
                }
            }
        });
    }

    private void handleVoucharValidation(String response) {
        boolean isValidVoucharCode = false;
        Log.d("TAG", "handleVoucharValidation: " + response);
        //Parse response json and take out the validations
        if(!isValidVoucharCode) {
            voucherCode = "";
            Toast.makeText(this, "Apply coupon failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Coupon applied successfully", Toast.LENGTH_SHORT).show();
        }
    }
}
