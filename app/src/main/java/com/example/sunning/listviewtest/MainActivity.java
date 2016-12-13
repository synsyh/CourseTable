package com.example.sunning.listviewtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private GridView gView;
    private List<Map<String, String>> data_list;
    private SimpleAdapter sim_adapter;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=(Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                gView = (GridView) findViewById(R.id.clmgview);
                data_list = new ArrayList<Map<String, String>>();
                Map<String, String> map = new HashMap<>();
                map.put("crsName", "Math");
//        map.put("crsNum", "test");
                data_list.add(map);
                map.put("crsName", "Math1");
//        map.put("crsNum", "test");
                data_list.add(map);
                map.put("crsName", "Math2");
//        map.put("crsNum", "test");
                data_list.add(map);
                map.put("crsName", "Math3");
//        map.put("crsNum", "test");
                data_list.add(map);
                map.put("crsName", "Math4");
//        map.put("crsNum", "test");
                data_list.add(map);
                map.put("crsName", "Math5");
//        map.put("crsNum", "test");
                data_list.add(map);
                map.put("crsName", "Math6");
//        map.put("crsNum", "test");
                data_list.add(map);
                map.put("crsName", "Math7");
//        map.put("crsNum", "test");
                data_list.add(map);
                map.put("crsName", "Math8");
//        map.put("crsNum", "test");
                data_list.add(map);
                String[] from = {"crsName"};
                int[] to = {R.id.textView};
                sim_adapter = new SimpleAdapter(MainActivity.this, data_list, R.layout.item, from, to);
                gView.setAdapter(sim_adapter);
            }
        });
    }
}
