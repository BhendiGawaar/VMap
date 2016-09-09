package com.example.vishal.userapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, PlaceSelectionListener, RecognitionListener, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;

    private ImageButton btnSpeak;
    private PopupWindow popup;
    private LayoutInflater layoutInflater;
    private RelativeLayout relativeLayout;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    private GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapter mAdapter;
    public static int isQuery=0;
    public PlaceAutocompleteFragment autocompleteFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //<----------------------------------------Speech Recognition--------------------------------------->>

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        //<---------------------------------Map ------------------------------------------------->>
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //<---------------------------------Place Auto complete------------------------------------------------->>

        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Register a listener to receive callbacks when a place has been selected or an error has
        // occurred.
        autocompleteFragment.setOnPlaceSelectedListener(this);


        mGoogleApiClient = new GoogleApiClient.Builder(MapsActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();

        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_MOUNTAIN_VIEW, null);

        //ArrayList<PlaceAutocomplete> = mPlaceArrayAdapter.getPredictions();
        //<------------------------------------Speak popup-------------------------------------->>
        relativeLayout = (RelativeLayout) findViewById(R.id.relative);
        Toast.makeText(MapsActivity.this,"create called",Toast.LENGTH_LONG).show();

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.speakpopup, null);
                //popup = new PopupWindow(container, 400, 400, true);
                //popup.showAtLocation(relativeLayout, Gravity.NO_GRAVITY, 500, 500);
                //speechRecognizer.startListening(recognizerIntent);
                isQuery=1;
                promptSpeechInput();
            }
        });




    }

    public void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak...");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }
    //
    public ArrayList<AutocompletePrediction> mResultList;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //txtSpeechInput.setText(result.get(0));
                    String spokenWord=result.get(0);

                    Toast.makeText(MapsActivity.this,"You said: "+spokenWord,Toast.LENGTH_LONG).show();
                    //ArrayList<PlaceAutocomplete> suggestions= mPlaceArrayAdapter.getPredictions(result.get(0));
                    //mResultList = mPlaceArrayAdapter.getPredictions("");

                    //Log.i(TAG, "mResultList 0 " + mResultList.get(0).getFullText(null));

                    if(isQuery==1)
                    {
                        Log.d(TAG, "STT Query: " + spokenWord);
                       // mAdapter.getAutocomplete("Gandhinagar");
                        mAdapter.getAutocomplete(spokenWord);
                        isQuery=0;
                    }
                    else
                        Log.d(TAG, "STT Response: " + spokenWord);
                        mResultList = mAdapter.getmResultList();
                    AutocompletePrediction place;
                        if(mResultList !=null)
                        {
                            Log.d(TAG, "mResultList size: " + mResultList.size());
                            for (int i = 0; i < mResultList.size(); i++)
                            {

                                place =mResultList.get(i);
                                Log.d(TAG, "checking Place... " + place.getFullText(null).toString());
                                if(place.getFullText(null).toString().toLowerCase().contains(spokenWord.toLowerCase()))
                                {

                                    Log.d(TAG, "Place selected by U: " + place.getFullText(null).toString());

                                    //onPlaceSelected(place);

                                    Places.GeoDataApi.getPlaceById(mGoogleApiClient, place.getPlaceId())
                                            .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                                @Override
                                                public void onResult(PlaceBuffer places) {
                                                    if (places.getStatus().isSuccess() && places.getCount() > 0)
                                                    {
                                                        final Place myPlace = places.get(0);
                                                        Log.i(TAG, "Place found: " + myPlace.getName());
                                                        onPlaceSelected(myPlace);
                                                        autocompleteFragment.setText(myPlace.getName());
                                                    } else {
                                                        Log.e(TAG, "Place not found");
                                                    }
                                                    places.release();
                                                }
                                            });

                                }
                            }
                        }
                }
                break;
            }

        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

 //        Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }


    @Override
    public void onPlaceSelected(Place place) {
        Log.i(TAG, "Place Selected: " + place.getName());
        LatLng latlng = place.getLatLng();
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latlng).title("Pick-Up location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onError(Status status) {
        Log.i(TAG, "Error: " + status.getStatusMessage());
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");//
        Log.i("default", String.valueOf(Locale.getDefault()));
       // progressBar.setIndeterminate(false);
        //progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        //progressBar.setIndeterminate(true);
        //toggleButton.setChecked(false);
    }




    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {

        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text += result + "\n";
        Log.i(LOG_TAG, "onResults");
        Log.i(LOG_TAG, text);
        Toast.makeText(MapsActivity.this, "You clicked yes button",Toast.LENGTH_LONG).show();
        //returnedText.setText(text);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        //progressBar.setProgress((int) rmsdB);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
       // returnedText.setText(errorMessage);
        //toggleButton.setChecked(false);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.i(LOG_TAG, "Google Places API connected.");
        //Log.i(TAG, "on Create:  getAutocomplete(\"Gandhinagar\") before");
        //mAdapter.getAutocomplete("Gandhinagar");
        /*while(true)
        {
            if(PredictionReady==1)
            {
                mResultList = mAdapter.getmResultList();
                break;
            }
        }*/
        //Log.i(TAG, "on Create:  getAutocomplete(\"Gandhinagar\") after");
        //manageInteraction();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }
}
