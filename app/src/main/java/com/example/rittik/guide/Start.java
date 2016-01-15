package com.example.rittik.guide;


import android.content.Intent;
import android.os.Bundle;

import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
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
    private MainActivity myActivity;

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
        myActivity = (MainActivity) getActivity();


        //return view;

        // Inflate the layout for this fragment
        return view;
        //        return inflater.inflate(R.layout.fragment_start, container, false);

    }


    @Override
    public void onClick(View v) {

        spoken.setText("Hey");
        myActivity.listens();
    }


}

