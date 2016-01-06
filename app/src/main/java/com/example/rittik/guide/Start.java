package com.example.rittik.guide;


import android.content.Intent;
import android.os.Bundle;

import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class Start extends Fragment implements View.OnClickListener{
    private TextView spoken;


    public Start() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View view =  inflater.inflate(R.layout.fragment_start, container, false);
        spoken = (TextView) view.findViewById(R.id.spoken);
        ImageButton button = (ImageButton) view.findViewById(R.id.imageButton);
        button.setOnClickListener(this);

        //return view;

        // Inflate the layout for this fragment
        return view;
        //        return inflater.inflate(R.layout.fragment_start, container, false);

    }


    @Override
    public void onClick(View v) {

        spoken.setText("Hey");
        listens();
    }
    public void listens() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something!");

        try {
            startActivityForResult(i, 100);
        } catch (Exception e) {
            //Toast.makeText()
        }

    }
    public void onActivityResult(int request_code, int result_code, Intent i){
        super.onActivityResult(request_code, result_code, i);

        switch(request_code){
            case 100: {if(result_code ==  -1 && i!=null) {
                ArrayList<String> result = i.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                spoken.setText(result.get(0));


            }
            }
            break;
        }

    }
}

