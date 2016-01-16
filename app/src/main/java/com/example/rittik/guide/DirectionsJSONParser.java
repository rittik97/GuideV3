package com.example.rittik.guide;

/**
 * Created by Rittik on 1/15/2016.
 */
/**
 * Created by rittik97 on 7/6/2015.
 */
import android.text.Html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

public class DirectionsJSONParser {

    LatLng destination;


    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public List<List<HashMap<String,String>>> parse(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");


                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        String polyline = "";
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        for(int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                            hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }
        return routes;
    }

    /**
     * Method to decode polyline points
     * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
    public ArrayList parsehtml(JSONObject jObject){

        ArrayList html=new ArrayList();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");


                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        String instructions = "";
                        instructions = (String)(((JSONObject)jSteps.get(k)).get("html_instructions"));
                        instructions=sanitizeinstructions(instructions);
                        html.add(instructions);

                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }
        return html;
    }

    protected synchronized String sanitizeinstructions(String instructions){
        //return instructions.replaceAll("\\<.*?>","");
        return  Html.fromHtml(instructions).toString();
    }

    public ArrayList parseendpoints(JSONObject jObject){

        ArrayList endlocations=new ArrayList();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        JSONObject temp;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");


                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        String lat = "";
                        String lng = "";
                        //lat = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("end_location")).get("lat");
                        //lng = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("end_location")).get("lng");
                        temp=jSteps.getJSONObject(k);
                        temp=temp.getJSONObject("start_location");
                        lat=temp.getString("lat");
                        lng=temp.getString("lng");

                        endlocations.add(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng)));
                        //endlocations.add(lat);
                        if(k==jSteps.length()-1){
                            destination=(new LatLng(Double.parseDouble(lat),Double.parseDouble(lng)));
                        }

                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }
        return endlocations;
    }
    public ArrayList<String> parsemaneuver(JSONObject jObject){

        ArrayList turns=new ArrayList();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;


        try {

            jRoutes = jObject.getJSONArray("routes");


            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();


                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");



                    for(int k=1;k<jSteps.length();k++){

                        String maneuver = "";

                        maneuver = (String)(((JSONObject)jSteps.get(k)).get("maneuver"));

                        turns.add(maneuver);


                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }
        return turns;

    }

}
