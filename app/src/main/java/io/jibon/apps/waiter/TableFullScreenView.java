package io.jibon.apps.waiter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goodcom.gcprinter.GcPrinterUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import io.jibon.apps.waiter.Adapters.OrderedItemsAdapter;

public class TableFullScreenView extends AppCompatActivity {
    public Activity activity;
    public Button openTableButton, closeTableButton, addItemTableButton, printItemsButton;
    public EditText customerName, customerPhone;
    public String ipAddress = "127.0.0.1", connectorCode = "0", connectionUsername = "";
    protected String table_id, order_id, customer_name, customer_phone, order_taker_name, order_status, order_time, orderTaxPercent;
    protected TextView pageTitle, orderIDTextShow, bookingTime, customerNameTextView, customerPhoneTextView, servedByTextView, itemOnlyTotal, totalVat, orderTaxPercentage, inTotalPrice;
    private CustomTools customTools;
    protected RelativeLayout bookTableView, openTableView;
    protected RecyclerView orderedItemsRecyclerView;
    public OrderedItemsAdapter orderedItemsAdapter;
    public JSONArray orderedItem = new JSONArray();

    public String TITLE = CustomTools.TITLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        customTools = new CustomTools(activity);
        setContentView(R.layout.activity_table_full_screen_view);

        // find view by id
        bookTableView = activity.findViewById(R.id.bookTableView);
        openTableView = activity.findViewById(R.id.openTableView);
        pageTitle = activity.findViewById(R.id.pageTitle);
        openTableButton = activity.findViewById(R.id.openTableButton);
        customerName = activity.findViewById(R.id.customerName);
        customerPhone = activity.findViewById(R.id.customerPhone);
        itemOnlyTotal = activity.findViewById(R.id.itemOnlyTotal);
        totalVat = activity.findViewById(R.id.totalVat);
        orderTaxPercentage = activity.findViewById(R.id.orderTaxPercentage);
        inTotalPrice = activity.findViewById(R.id.inTotalPrice);

        orderIDTextShow = activity.findViewById(R.id.orderIDTextShow);
        bookingTime = activity.findViewById(R.id.bookingTime);
        customerNameTextView = activity.findViewById(R.id.customerNameTextView);
        customerPhoneTextView = activity.findViewById(R.id.customerPhoneTextView);
        servedByTextView = activity.findViewById(R.id.servedByTextView);
        orderedItemsRecyclerView = activity.findViewById(R.id.orderedItemsRecyclerView);
        closeTableButton = activity.findViewById(R.id.closeTableButton);
        addItemTableButton = activity.findViewById(R.id.addItemTableButton);
        printItemsButton = activity.findViewById(R.id.printItemsButton);

        // mechanism
        ipAddress = customTools.setPref("serverIpAddress", null);
        connectorCode = customTools.setPref("connectorCode", null);

        addItemTableButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity, AddItemToOrderListGroupsActivity.class);
            intent.putExtra("order_id", order_id);
            activity.startActivity(intent);
        });



        closeTableButton.setOnClickListener(v -> closeTable());

        Bundle bundle = activity.getIntent().getExtras();
        if (bundle != null){
            if (bundle.containsKey("tableID") && bundle.containsKey("tableName")){
                table_id = bundle.getString("tableID");
                String table_name = bundle.getString("tableName");
                if (bundle.containsKey("order_id")){
                    bookTableView.setVisibility(View.GONE);
                    openTableView.setVisibility(View.VISIBLE);
                    pageTitle.setText(""+ table_name);

                    order_id = bundle.getString("order_id");
                    customer_name = bundle.getString("customer_name");
                    customer_phone = bundle.getString("customer_phone");
                    order_taker_name = bundle.getString("order_taker_name");
                    order_status = bundle.getString("order_status");
                    order_time = bundle.getString("order_time");
                    orderTaxPercent = bundle.getString("vat");
                    connectionUsername = bundle.getString("connectionUsername");

                    orderIDTextShow.setText("Booking ID: " + order_id);
                    bookingTime.setText("Sit Time: " + order_time);
                    customerNameTextView.setText("Customer Name: " + customer_name);
                    customerPhoneTextView.setText("Customer Number: " + customer_phone);
                    servedByTextView.setText("Served by: " + order_taker_name);
                    orderTaxPercentage.setText(orderTaxPercent + "%");

                    if (!connectionUsername.equalsIgnoreCase(order_taker_name)) {
                        addItemTableButton.setVisibility(View.GONE);
                        closeTableButton.setVisibility(View.GONE);
                    }

                    load_ordered_items();
                }else{
//                    openTableButtonClicked();
                }

                orderedItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                orderedItemsAdapter = new OrderedItemsAdapter(activity, orderedItem);
                orderedItemsRecyclerView.setAdapter(orderedItemsAdapter);

                openTableButton.setOnClickListener(v -> {
                    openTableButtonClicked();
                });
            }
        }
    }

    private void openTableButtonClicked() {
        String cName = URLEncoder.encode(String.valueOf(customerName.getText()));
        String cPhone = URLEncoder.encode(String.valueOf(customerPhone.getText()));
        String url = "http://"+ipAddress+"/json/app?connectorCode="+connectorCode+"&book_table="+table_id+"&customer_name="+cName+"&customer_phone="+cPhone;
        @SuppressLint("SetTextI18n") Internet2 internet2 = new Internet2(activity, url, ((code, result) -> {
            try {
                if (result.has("book_table")) {
                    if (result.getBoolean("book_table")){
                        order_id = result.getString("order_id");
                        order_taker_name = result.getString("connectionUsername");
                        order_time = result.getString("time");
                        orderTaxPercent = result.getString("vat");

                        bookTableView.setVisibility(View.GONE);
                        openTableView.setVisibility(View.VISIBLE);

                        orderIDTextShow.setText("Booking ID: "+order_id);
                        bookingTime.setText("Sit Time: "+order_time);
                        customerNameTextView.setText("Customer Name: "+cName);
                        customerPhoneTextView.setText("Customer Number: "+cPhone);
                        servedByTextView.setText("Served by: "+order_taker_name);
                        orderTaxPercentage.setText(orderTaxPercent+"%");
                        load_ordered_items();
                    }else{
                        customTools.toast("Something went wrong!");
                    }
                }
            }catch (Exception error){
                Log.e("errnos", error.getMessage());
            }
        }));
        internet2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void closeTable(){
        closeTableButton.setClickable(false);
        closeTableButton.setAlpha(0.5F);
        addItemTableButton.setVisibility(View.GONE);
        printItemsButton.setVisibility(View.GONE);
        String url = "http://"+ipAddress+"/json/app?connectorCode="+connectorCode+"&tableClosed="+order_id;
        Internet2 internet2 = new Internet2(activity, url, ((code, result) -> {
            try {
                if (result.has("tableClosed")) {
                    if (result.getBoolean("closeStatus")){
                        closeTableButton.setText("CLOSED");
                        printClosedTable(result.getJSONObject("tableClosed"), result.getJSONArray("ordered_items"));
                    }else{
                        closeTableButton.setClickable(true);
                        closeTableButton.setAlpha(1F);
                        addItemTableButton.setVisibility(View.VISIBLE);
                        printItemsButton.setVisibility(View.VISIBLE);
                        customTools.toast("Try again later...");
                    }
                }
            }catch (Exception e){
                Log.e("errnos", e.getMessage());
            }
        }));
        internet2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void printClosedTable(JSONObject tableClosed, JSONArray ordered_items) {

//        customTools.toast("Table Closed...");
        if (GcPrinterUtils.isDeviceSupport()) {
            try {
                customTools.toast("Printing...");
                GcPrinterUtils.drawCustom(TITLE, GcPrinterUtils.fontBig, GcPrinterUtils.alignCenter);
                GcPrinterUtils.drawOneLine();
                GcPrinterUtils.drawLeftRight("Booking ID", 0, order_id, 0);
                GcPrinterUtils.drawLeftRight("Booking Time", GcPrinterUtils.fontSmall, order_time, GcPrinterUtils.fontSmall);
                GcPrinterUtils.drawLeftRight("Billing Time", GcPrinterUtils.fontSmall, tableClosed.has("billed_time") ? tableClosed.getString("billed_time") : "OPEN", GcPrinterUtils.fontSmall);
//                GcPrinterUtils.drawNewLine();
//                GcPrinterUtils.drawLeftRight("Customer Name", 0, tableClosed.getString("customer_name"), 0);
//                GcPrinterUtils.drawLeftRight("Customer Number", 0, tableClosed.getString("customer_phone"), 0);
//                GcPrinterUtils.drawLeftRight("Served By", 0, tableClosed.getString("order_taker_name"), 0);
                GcPrinterUtils.drawLeftRight("Waiter Name: ", GcPrinterUtils.fontSmall, order_taker_name, GcPrinterUtils.fontSmall);
                GcPrinterUtils.drawNewLine();
                GcPrinterUtils.drawText("Name", GcPrinterUtils.fontSmallBold, "price x unit", GcPrinterUtils.fontSmall, "Total", GcPrinterUtils.fontSmallBold);
                GcPrinterUtils.drawOneLine();
                float totalPrice = 0;
                for (int i = 0; i < ordered_items.length(); i++) {
                    JSONObject item = ordered_items.getJSONObject(i);
                    float thisPrice =  Float.parseFloat(item.getString("price_then")) * Float.parseFloat(item.getString("item_quantity"));
                    totalPrice += thisPrice;
                    GcPrinterUtils.drawText(item.getString("name_then"), GcPrinterUtils.fontSmallBold, item.getString("price_then")+" x "+item.getString("item_quantity"), GcPrinterUtils.fontSmall, String.format("%.2f", thisPrice), GcPrinterUtils.fontSmallBold);
                }
                GcPrinterUtils.drawOneLine();
                GcPrinterUtils.drawText("", GcPrinterUtils.fontSmallBold, "Total", GcPrinterUtils.fontSmall, "€" + String.format("%.2f", totalPrice), GcPrinterUtils.fontSmallBold);
//                Float totalWhenBooked = Float.parseFloat(tableClosed.getString("total_when_booked"));
//                Float totalTax = totalWhenBooked * (Float.parseFloat(tableClosed.getString("vat")) / 100);
//                GcPrinterUtils.drawText("", GcPrinterUtils.fontSmallBold, "Vat ("+tableClosed.getString("vat")+")", GcPrinterUtils.fontSmall, String.format("%.2f",totalTax), GcPrinterUtils.fontSmallBold);
//                GcPrinterUtils.drawCustom("In Total\t "+String.format("%.2f", totalWhenBooked+totalTax), GcPrinterUtils.fontMedium, GcPrinterUtils.alignRight);
                GcPrinterUtils.drawNewLine();
                GcPrinterUtils.drawBarcode(order_id + "\tProgrammerJibon", GcPrinterUtils.alignCenter, GcPrinterUtils.barcodeQrCode);

                GcPrinterUtils.printText(activity, true);
            }catch (Exception e){
                customTools.toast(e.getMessage());
                activity.finish();
            }
        }else{
            customTools.toast("Unsupported Device...");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
//        load_ordered_items();
    }

    public void load_ordered_items(){
        if (order_id != null){
            String url = "http://"+ipAddress+"/json/app?connectorCode="+connectorCode+"&ordered_items="+order_id;
            @SuppressLint({"SetTextI18n", "DefaultLocale"}) Internet2 internet2 = new Internet2(activity, url, ((code, result) -> {
                try {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            load_ordered_items();
                        }
                    },1000);
                    if (result.has("ordered_items")) {
                        orderedItem = result.getJSONArray("ordered_items");
                        printItemsButton.setOnClickListener(v -> {
                            printClosedTable(new JSONObject(), orderedItem);
                        });
                        if (orderedItem.length() > 0){
                            orderedItemsAdapter.updateData(orderedItem);
                            Float makeTotal = Float.parseFloat("0");
                            for (int i = 0; i < orderedItem.length(); i++) {
                                JSONObject item = orderedItem.getJSONObject(i);
                                Float thisItemTotal = Float.parseFloat(item.getString("price_then")) * Float.parseFloat(item.getString("item_quantity"));
                                makeTotal += thisItemTotal;
                            }
                            itemOnlyTotal.setText("€" + String.format("%.2f", makeTotal));
                            Float vat = makeTotal * (Float.parseFloat(orderTaxPercent)/100);
                            totalVat.setText("€" + String.format("%.2f", vat));
                            String inTotalPriceText = String.format("%.2f", makeTotal+vat);
                            inTotalPrice.setText("€" + inTotalPriceText);
                        }
                    }
                }catch (Exception e){
                    Log.e("errnos ", url + "\t" + e);
                }
            }));
            internet2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
}

//            int width = openTableView.getWidth();
//            int height = openTableView.getHeight();
//            openTableView.setBackgroundColor(Color.WHITE);
//            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            Canvas canvas = new Canvas(bitmap);
//            canvas.setBitmap(bitmap);
//            openTableView.draw(canvas);
//            ImageView imageView = activity.findViewById(R.id.image);
//            imageView.setImageBitmap(bitmap);