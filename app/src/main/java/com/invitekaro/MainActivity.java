package com.invitekaro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import java.util.List;

public class MainActivity extends AppCompatActivity implements PaymentResultListener {

    Voucher voucherInterface = null;

    ArrayList<Card> cardList = new ArrayList<>();
    private List<String> cardTypesList = new ArrayList<String>();
    private String voucherCode = "";
    private double checkoutPrize = 0.0;
    private double offerDiscount = 0.0;
    private double toPayTotal = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String json = null;
        try {
            InputStream is = getAssets().open(getString(R.string.dataFile));
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

            JSONObject obj = new JSONObject(json);
            JSONArray m_jArry = obj.getJSONArray(getString(R.string.cards));

            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject jo_inside = m_jArry.getJSONObject(i);
                Card cardItem = new Card();
                cardItem.setCardType(jo_inside.getString(getString(R.string.cardType)));
                cardItem.setCardName(jo_inside.getString(getString(R.string.cardName)));
                cardItem.setCardDetail(jo_inside.getString(getString(R.string.cardDetail)));
                cardItem.setCardPrice(jo_inside.getString(getString(R.string.cardPrice)));

                cardList.add(cardItem);

                checkoutPrize += cardItem.getCardPriceValue();

                cardTypesList.add(cardItem.getCardType().toLowerCase());
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

        setDefaultDiscount();
        updateBillDetailValues();

        Checkout.preload(getApplicationContext());
        initializeVoucherAPI();

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

    private void setDefaultDiscount(){
        int discountPercentage = 0;
        if (cardList.size() >= 4) {
            discountPercentage = 20;
        } else if (cardList.size() >= 3) {
            discountPercentage = 15;
        } else if (cardList.size() >= 2) {
            discountPercentage = 10;
        }

        if (cardList.size() >= 4) {
            View view = findViewById(R.id.discount_card);
            view.setVisibility(View.GONE);
        } else {
            View view = findViewById(R.id.discount_card);
            view.setVisibility(View.VISIBLE);
        }

        offerDiscount = (checkoutPrize * discountPercentage) / 100;
    }

    private void updateBillDetailValues(){

        toPayTotal = checkoutPrize - offerDiscount;

        TextView priceValueText = (TextView)findViewById(R.id.itemTotal);
        priceValueText.setText(getPriceDisplayValue(checkoutPrize));
        priceValueText = (TextView)findViewById(R.id.offerDiscount);
        priceValueText.setText(getPriceDisplayValue(offerDiscount));
        priceValueText = (TextView)findViewById(R.id.toPayTotal);
        priceValueText.setText(getPriceDisplayValue(toPayTotal));
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
        if (voucherInterface != null){
            //voucherInterface.checkValidity(voucherCode);
            LinearLayout progressBar = (LinearLayout) findViewById(R.id.progressbar);
            progressBar.setVisibility(View.VISIBLE);
            voucherInterface.checkValidityAsync(voucherCode);
        }
    }

    public void applyVoucher() {
        if (voucherCode != null && voucherCode.isEmpty()) {
            voucherInterface.redeemVoucher(voucherCode, "-1");
        }
    }

    public void initializeVoucherAPI(){
        String apiKey     = "8531589b490dffc8e298a8b1faeae0f2";
        String merchantID = "ef993b6b-2a30-4211-a9a5-c375db7a8a15";

        voucherInterface = Voucher.getInstance();
        voucherInterface.initialize(merchantID, apiKey);
        voucherInterface.setOnVoucherResultListener(new Voucher.OnVoucherResultListener() {
            @Override
            public void onResult(int requestType, int responseCode, String response) {

                Log.d("TAG", "onResult: " + requestType);
                switch(requestType){

                    case Voucher.CHECK_VOUCHER_VALIDITY:
                        handleVoucherValidation(response);
                        break;

                    case Voucher.REDEEM_VOUCHER:
                        break;
                }
            }
            @Override
            public void onError(int requestType, int responseCode, String errorMessage) {
                switch(requestType){
                    case Voucher.CHECK_VOUCHER_VALIDITY:
                        handleVoucherValidation(null);
                        break;

                    case Voucher.REDEEM_VOUCHER:
                        break;
                }
            }

            @Override
            public void onException(int requestType, Exception e, String message) {
                switch(requestType){
                    case Voucher.CHECK_VOUCHER_VALIDITY:
                        handleVoucherValidation(null);
                        break;

                    case Voucher.REDEEM_VOUCHER:
                        break;
                }
            }
        });
    }

    private void handleVoucherValidation(String response) {
        Log.d("TAG", "handleVoucherValidation: " + response);

        String message = getString(R.string.coupon_success);
        double responseDiscount = 0;

        setDefaultDiscount();
        if (response == null){
            voucherCode = "";
            responseDiscount = 0.0;
            message = getString(R.string.coupon_generic_error);
        } else {
            try {
                JSONObject respObj = new JSONObject(response);
                String statusString = respObj.getString(getString(R.string.validationStatus));
                JSONObject statusJson = new JSONObject(statusString);
                if (statusJson.getString(getString(R.string.status)).equals(Voucher.VOUCHERVALID)) {
                    String dataString = respObj.getString(getString(R.string.data));
                    JSONObject dataJson = new JSONObject(dataString);

                    JSONArray voucherForCardType = dataJson.getJSONArray(getString(R.string.tag));
                    String respVoucherCode = dataJson.getString(getString(R.string.voucherCode));
                    String redeemed = dataJson.getString(getString(R.string.redeemed));

                    if (respVoucherCode.equals(voucherCode)
                            && hasRedeemableCardTypeInCart(voucherForCardType)
                            && redeemed.equals(Voucher.NOT_REDEEMED)) {
                        responseDiscount = dataJson.getDouble(getString(R.string.value));
                    } else {
                        voucherCode = "";
                        responseDiscount = 0.0;
                        message = getString(R.string.coupon_generic_error);
                    }
                } else {
                    voucherCode = "";
                    responseDiscount = 0.0;
                    message = statusJson.getString(getString(R.string.message));
                }
            } catch (JSONException e) {
                voucherCode = "";
                responseDiscount = 0.0;
                message = getString(R.string.coupon_generic_error);
                e.printStackTrace();
            }
        }
        offerDiscount += responseDiscount;

        final String _message = message;
        final Context that = this;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateBillDetailValues();
                LinearLayout progressBar = (LinearLayout) findViewById(R.id.progressbar);
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(that, _message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean hasRedeemableCardTypeInCart(JSONArray voucherForCardType){
        try {
            for (int i=0; i<voucherForCardType.length(); i++) {
                if (cardTypesList.contains(voucherForCardType.getString(i))) {
                    return true;
                }
            }
        } catch (JSONException e){}

        return false;
    }
}
