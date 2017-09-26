package com.mangoo.weather;


import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


import java.util.ArrayList;

public class GetCityActivity extends AppCompatActivity {

    private final int MENU_DELETE = 1;

    Button btnGetName;
    Button btnFinish;
    EditText etGetName;
    ListView lvUsersPickCities;
    ArrayAdapter<String> adapter;
    ArrayList<String> moreCityNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_city);


        btnGetName = (Button) findViewById(R.id.btnGetName);
        btnFinish = (Button) findViewById(R.id.btnFinish);
        etGetName = (EditText) findViewById(R.id.etGetName);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, moreCityNames);
        lvUsersPickCities = (ListView) findViewById(R.id.listViewUsersPickCities);
        lvUsersPickCities.setAdapter(adapter);

        registerForContextMenu(lvUsersPickCities);


        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()){
                    case R.id.btnGetName:
                        moreCityNames.add(etGetName.getText().toString());
                        adapter.notifyDataSetChanged();
                        etGetName.setText(null);
                        lvUsersPickCities.smoothScrollToPosition(adapter.getCount());
                        break;

                    case R.id.btnFinish:
                        if (moreCityNames.size() != 0) {
                            Intent intent = new Intent();
                            intent.putStringArrayListExtra("list", moreCityNames);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {

                            AlertDialog.Builder builder = new AlertDialog.Builder(GetCityActivity.this);
                            builder.setTitle(R.string.alert_dialog_title)
                                    .setMessage(R.string.alert_dialog_message)
                                    .setPositiveButton(R.string.alert_dialog_positive_button_name, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .setNegativeButton(R.string.alert_dialog_negative_button_name, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                }

            }
        };
        btnGetName.setOnClickListener(onClickListener);
        btnFinish.setOnClickListener(onClickListener);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, MENU_DELETE, 0, R.string.contec_menu_delete_city);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (item.getItemId() == MENU_DELETE){
            adapter.remove(adapter.getItem(info.position));
        }
        return super.onContextItemSelected(item);
    }
}
