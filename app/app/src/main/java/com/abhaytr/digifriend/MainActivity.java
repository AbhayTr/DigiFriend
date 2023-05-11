package com.abhaytr.digifriend;

/*

Welcome to DigiFriend!

Your friend who lives in your phone and thinks in the cloud.
This is the source code for the Android App.

© Abhay Tripathi

*/

import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import android.speech.tts.*;
import android.media.*;
import android.speech.tts.TextToSpeech.*;
import android.speech.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.text.*;
import java.util.concurrent.*;
import android.provider.*;
import android.app.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity implements OnInitListener
{

    private TextToSpeech friend_throat;
    private RequestQueue queue;
    private static final int CHECK_TTS_DATA = 0X123;
    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private AnimationDrawable friend_body;
    private AnimationDrawable celebration_body;
    private TextView status;
    TextView credits;
    TextView about;
    private TextView update;
    private ImageView friend;
    private ImageView celebration;
    final private String BRAIN_OFF = "I am unable to use my brain right now. Please ensure internet connection as I think in the cloud and I need internet for that. If you are connected to the internet and yet I am not able to use my brain, then please click the update button located below me as my brain might have moved somewhere. Please restart the app or try again after some time.";
    private String brain_url = "";
    final private String app_version = "1.0.1";
    final private String brain_location_url = "https://abhaytr.github.io/digifriend/Brain.txt";
    private String uid;
    private ScheduledExecutorService ses;
    private String[] tts_lst = new String[]{};
    private int tts_no = 0;
    private boolean busy = false;
    private AudioManager am;
    private Intent speechRecognizerIntent;
    private int ringer_level = -1;
    private int music_level = -1;
    private boolean in_app = true;
    private boolean music_muted = false;
    private final String PERMISSION_TEXT = "Please note that your phone's volume levels will be controlled by our app <b>only when you are in our app</b> for best user experience. Your audio will automatically be restored to your preset levels when you exit the app.<br><br>Kindly provide me microphone permission so that I can hear you.";
    private boolean first_run = true;
    final private String BRAIN_NOT_FOUND_FIRST = "DigiFriend needs to know where it's brain is so that it can use it. Right now for some reason it cannot locate it.<br><br>Kindly check your internet connection and restart the app or try again after some time";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
        uid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        ses = Executors.newSingleThreadScheduledExecutor();
        friend = findViewById(R.id.friend);
        celebration = findViewById(R.id.celebration);
        status = findViewById(R.id.status);
        credits = findViewById(R.id.credits);
        credits.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                alert("Credits for DigiFriend App", "<br>DigiFriend is an intellectual property of Abhay Tripathi.<br><br>Only Talking Tom is an intellectual property of Outfit7 Ltd.<br><br><b>© Abhay Tripathi</b>", "OK");
            }
        });
        about = findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                alert("About DigiFriend", "DigiFriend is your virtual friend which makes your phone feel like any modern day smart device (Amazon Alexa, Google Home, etc.)<br><br>You can use digifriend almost just as you would have used any smart device, i.e. digifriend app supports many functions (such as enquiring data, talking like a friend, etc.) that today's modern smart devices provide.<br><br>One of it's best feature is that you don't have to press any buttons to talk to digifriend, i.e. just open the app and start talking directly to digifriend. This feature makes your phone just like any modern smart device and makes your interaction experience highly realistic.<br><br><b>Important Function:</b><br><br>If you are connected to the internet and still DigiFriend says that it cannot use it's brain, then click on the <b>Update</b> button placed below DigiFriend which will map DigiFriend's brain correctly with DigiFriend and hence DigiFriend can use it's brain normally again.<br><br>App Version " + app_version, "OK");
            }
        });
        update = findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                update_brain_location();
            }
        });
        friend.setBackgroundResource(R.drawable.a1);
        celebration.setBackgroundResource(R.drawable.cg_gif);
        Intent checkTtsDataIntent = new Intent();
        checkTtsDataIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTtsDataIntent, CHECK_TTS_DATA);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.setRecognitionListener(new RecognitionListener()
        {
            @Override
            public void onReadyForSpeech(Bundle bundle)
            {
                
            }

            @Override
            public void onBeginningOfSpeech()
            {
                
            }

            @Override
            public void onRmsChanged(float v)
            {

            }

            @Override
            public void onBufferReceived(byte[] bytes)
            {

            }

            @Override
            public void onEndOfSpeech()
            {

            }

            @Override
            public void onError(int i)
            {
                if (in_app && !busy)
                {
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
            }

            @Override
            public void onResults(Bundle bundle)
            {
                if (!busy)
                {
                    ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String message = data.get(0);
                    getReady();
                    processMessage(message);
                }
                if (in_app && !busy)
                {
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
            }

            @Override
            public void onPartialResults(Bundle bundle)
            {

            }

            @Override
            public void onEvent(int i, Bundle bundle)
            {
                if (in_app && !busy)
                {
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
            }
        });
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mute();
        mute_music();
        ifFirstRun();
        if (!first_run)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    askPermission();
                }
            }
            speechRecognizer.startListening(speechRecognizerIntent);
        }
    }

    private void askPermission()
    {
        alert("Welcome to DigiFriend!", PERMISSION_TEXT, "OK");
    }

    private void getReady()
    {
        friend.setBackgroundResource(R.drawable.bg_gif);
        friend_body = (AnimationDrawable) friend.getBackground();
    }

    public void onInit(int status)
    {
        if (status == TextToSpeech.SUCCESS)
        {
            if (friend_throat.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
            {
                friend_throat.setLanguage(Locale.US);
            }
        }
        else if (status == TextToSpeech.ERROR)
        {
            Toast.makeText(this, "My throat is sore right now. Please restart the app or try again after some time.", Toast.LENGTH_LONG).show();
        }
    }

    private void alert(String title, final String message, String ok_text)
    {
        AlertDialog.Builder alert_box = new AlertDialog.Builder(MainActivity.this);
        alert_box.setTitle(Html.fromHtml("<font color='#FFFFFF'>" + title + "</font>"));
        alert_box.setMessage(Html.fromHtml("<font color='#FFFFFF'>" + message + "</font>"));
        if (message.equals(PERMISSION_TEXT) || message.equals(BRAIN_NOT_FOUND_FIRST))
        {
            alert_box.setCancelable(false);
        }
        else
        {
            alert_box.setCancelable(true);
        }
        alert_box.setPositiveButton(ok_text, new DialogInterface.OnClickListener()
        {
            public void onClick(final DialogInterface dialog, int whichButton)
            {
                if (message.equals(PERMISSION_TEXT))
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
                }
                if (message.equals(BRAIN_NOT_FOUND_FIRST))
                {
                    System.exit(0);
                }
                if (!message.equals(BRAIN_NOT_FOUND_FIRST))
                {
                    dialog.dismiss();
                }
            }
        });
        final AlertDialog dialog_window = alert_box.create();
        dialog_window.show();
        dialog_window.getWindow().getDecorView().setBackgroundResource(R.drawable.popup_bubble);
        dialog_window.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
    }

    private void speak(String tts, String utteranceId)
    {
        tts_lst = tts.split("\\. ");
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
        friend_throat.speak(tts_lst[0], TextToSpeech.QUEUE_FLUSH, params);
        tts_no = 1;
    }

    private void happyBirthday()
    {
        MainActivity.this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                status.setText("🗣️ Speaking...");
                friend.setBackgroundResource(R.drawable.bg_gif);
                celebration.setVisibility(View.VISIBLE);
                friend_body = (AnimationDrawable) friend.getBackground();
                celebration_body = (AnimationDrawable) celebration.getBackground();
                friend_body.start();
                celebration_body.start();
            }
        });
        MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.hbd);
        mPlayer.start();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                MainActivity.this.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        friend_body.stop();
                        celebration_body.stop();
                        celebration.setVisibility(View.GONE);
                        friend.setBackgroundResource(R.drawable.a1);
                    }
                });
                reset();
            }
        });
    }

    protected void customOutput(String message)
    {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case CHECK_TTS_DATA:
            {
                if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
                {
                    friend_throat = new TextToSpeech(this, this);
                    friend_throat.setOnUtteranceProgressListener(new UtteranceProgressListener()
                    {
                        @Override
                        public void onStart(String utteranceId)
                        {
                            MainActivity.this.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    status.setText("🗣️ Speaking...");
                                    friend.setBackgroundResource(R.drawable.bg_gif);
                                    friend_body = (AnimationDrawable) friend.getBackground();
                                    friend_body.start();
                                }
                            });
                        }

                        @Override
                        public void onError(String utteranceId)
                        {
                            reset();
                            Toast.makeText(MainActivity.this, "Something went wrong with my throat. Please restart the app or try again after some time.", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onDone(String utteranceId)
                        {
                            MainActivity.this.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    friend_body.stop();
                                    friend.setBackgroundResource(R.drawable.a1);
                                }
                            });
                            if (tts_no != tts_lst.length)
                            {
                                String tts_message = tts_lst[tts_no];
                                if (tts_message != "")
                                {
                                    HashMap<String, String> params = new HashMap<String, String>();
                                    params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "friend");
                                    friend_throat.speak(tts_message, TextToSpeech.QUEUE_FLUSH, params);
                                    tts_no += 1;
                                }
                                else
                                {
                                    reset();
                                }
                            }
                            else
                            {
                                reset();
                            }
                        }

                        @Override
                        public void onStop(String utteranceId, boolean interrupted)
                        {
                            MainActivity.this.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    friend_body.stop();
                                    friend.setBackgroundResource(R.drawable.a1);
                                }
                            });
                        }
                    });
                }
                else
                {
                    Intent installTtsIntent = new Intent();
                    installTtsIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installTtsIntent);
                }
            }
        }
    }

    private void processMessage(String message)
    {
        busy = true;
        MainActivity.this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                update.setClickable(false);
                status.setText("🤔 Hmmm...");
                status.setBackgroundDrawable(getDrawable(R.drawable.thinking_bubble));
                speechRecognizer.stopListening();
                if (music_muted)
                {
                    unmute_music();
                }
            }
        });
        StringRequest brain_request = new StringRequest(Request.Method.GET, brain_url + message, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                MainActivity.this.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        status.setText("😀 Got it!");
                        status.setBackgroundDrawable(getDrawable(R.drawable.speaking_bubble));
                    }
                });
                int actionCode = checkResponse(response);
                if (actionCode == 0)
                {
                    speak(response, "friend");
                }
                else if (actionCode == 1)
                {
                    happyBirthday();
                }
            }
        },
        new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                MainActivity.this.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        status.setText("😔 Something's wrong.");
                        status.setBackgroundDrawable(getDrawable(R.drawable.error_bubble));
                    }
                });
                speak(BRAIN_OFF, "friend");
            }
        })
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("uid", uid);
                return params;
            }
        };
        brain_request.setRetryPolicy(new DefaultRetryPolicy(7200000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(brain_request);
    }

    private int checkResponse(String response)
    {
        try
        {
            if (response.contains("<"))
            {
                MainActivity.this.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        status.setText("😔 Something's wrong.");
                        status.setBackgroundDrawable(getDrawable(R.drawable.error_bubble));
                    }
                });
                speak(BRAIN_OFF, "friend");
                return -1;
            }
            if (response.contains("[List]"))
            {
                Object[] list = response.replace("[List]", "").split(",");
                boolean once = Boolean.parseBoolean(((String) list[list.length - 3]).replace("\n", ""));
                boolean reverse = Boolean.parseBoolean(((String) list[list.length - 2]).replace("\n", ""));
                int ms = Integer.parseInt(((String) list[list.length - 1]).replace("\n", ""));
                Object[] actualList = Arrays.copyOfRange(list, 0, list.length - 3);
                narrateList(actualList, ms, once, reverse);
                return -1;
            }
            if (response.contains("[Range]"))
            {
                Object[] range = response.replace("[Range]", "").split(",");
                int start = Integer.parseInt(((String) range[0]).replace("\n", ""));
                int end = Integer.parseInt(((String) range[1]).replace("\n", ""));
                boolean once = Boolean.parseBoolean(((String) range[2]).replace("\n", ""));
                boolean reverse = Boolean.parseBoolean(((String) range[3]).replace("\n", ""));
                int ms = Integer.parseInt(((String) range[4]).replace("\n", ""));
                narrateRange(start, end, ms, once, reverse);
                return -1;
            }
            if (response.contains("[HBD]"))
            {
                return 1;
            }
            return 0;
        }
        catch (Exception ex)
        {
            error();
            return -1;
        }
    }

    private void mute()
    {
        try
        {
            if (ringer_level == -1)
            {
                ringer_level = am.getStreamVolume(AudioManager.STREAM_RING);
            }
            am.setStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
        catch (Exception e)
        {
            //Never Mind...
        }
    }

    private void unmute()
    {
        try
        {
            am.setStreamVolume(AudioManager.STREAM_RING, ringer_level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        }
        catch (Exception e)
        {
            //Never Mind...
        }
    }

    private void mute_music()
    {
        try
        {
            if (music_level == -1)
            {
                music_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            }
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            music_muted = true;
        }
        catch (Exception e)
        {
            //Never Mind...
        }
    }

    private void unmute_music()
    {
        try
        {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, music_level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            music_muted = false;
        }
        catch (Exception e)
        {
            //Never Mind...
        }
    }

    private void reset()
    {
        tts_lst = new String[]{};
        tts_no = 0;
        busy = false;
        MainActivity.this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (in_app)
                {
                    mute_music();
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                status.setText("🙂 Say whatever you want!");
                status.setBackgroundDrawable(getDrawable(R.drawable.default_bubble));
                update.setClickable(true);
            }
        });
    }

    private void error()
    {
        speak("Sorry can you say that again.", "friend");
    }

    private void narrateRange(final int start, final int end, int ms, boolean once, boolean reverse)
    {
        try
        {
            if (!once)
            {
                Runnable t = new Runnable()
                {
                    int zm = 0;
                    int o = 0;

                    public void run()
                    {
                        speak(Integer.toString(zm), "friend");
                        if (zm < end && o == 0)
                        {
                            zm += 1;
                        }
                        else if (zm > start && o == 1)
                        {
                            zm -= 1;
                        }
                        else
                        {
                            if (zm == end)
                            {
                                zm = end - 1;
                                o = 1;
                            }
                            else if (zm == 0)
                            {
                                zm = start + 1;
                                o = 0;
                            }
                        }
                    }
                };
                ses.scheduleAtFixedRate(t, 0, ms, TimeUnit.MILLISECONDS);
            }
            else
            {
                if (!reverse)
                {
                    for (int i = start; i <= end; i++)
                    {
                        final int fi = i;
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                speak(Integer.toString(fi), "friend");
                            }
                        });
                        Thread.sleep(ms);
                    }
                }
                else
                {
                    for (int i = end; i >= start; i--)
                    {
                        final int fi = i;
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                speak(Integer.toString(fi), "friend");
                            }
                        });
                        Thread.sleep(ms);
                    }
                }
            }
        }
        catch (Exception e)
        {
            error();
        }
    }

    private void narrateList(final Object[] list, int ms, boolean once, boolean reverse)
    {
        try
        {
            if (!once)
            {
                Runnable t = new Runnable()
                {
                    int start = 0;
                    int end = list.length - 1;
                    int zm = 0;
                    int o = 0;

                    public void run()
                    {
                        speak((String) list[zm], "friend");
                        if (zm < end && o == 0)
                        {
                            zm += 1;
                        }
                        else if (zm > start && o == 1)
                        {
                            zm -= 1;
                        }
                        else
                        {
                            if (zm == end)
                            {
                                zm = end - 1;
                                o = 1;
                            }
                            else if (zm == 0)
                            {
                                zm = start + 1;
                                o = 0;
                            }
                        }
                    }
                };
                ses.scheduleAtFixedRate(t, 0, ms, TimeUnit.MILLISECONDS);
            }
            else
            {
                if (!reverse)
                {
                    for (int i = 0; i < list.length; i++)
                    {
                        final int fi = i;
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                speak((String) list[fi], "friend");
                            }
                        });
                        Thread.sleep(ms);
                    }
                }
                else
                {
                    for (int i = (list.length - 1); i >= 0; i--)
                    {
                        final int fi = i;
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                speak((String) list[fi], "friend");
                            }
                        });
                        Thread.sleep(ms);
                    }
                }
            }
        }
        catch (Exception e)
        {
            error();
        }
    }

    private void save(String key, String value)
    {
        SharedPreferences opts = getApplicationContext().getSharedPreferences("Options", 0);
        SharedPreferences.Editor editor = opts.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private String get(String key)
    {
        SharedPreferences opts = getApplicationContext().getSharedPreferences("Options", 0);
        return opts.getString(key, null);
    }

    private boolean check(String key)
    {
        SharedPreferences opts = getApplicationContext().getSharedPreferences("Options", 0);
        return opts.contains(key);
    }

    private void ifFirstRun()
    {
        if (!check("first_run"))
        {
            update_brain_location();
        }
        else
        {
            brain_url = get("brain_url");
            first_run = false;
        }
    }

    private void update_brain_location()
    {
        busy = true;
        MainActivity.this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                update.setClickable(false);
                status.setText("🤔 Locating my brain...");
                status.setBackgroundDrawable(getDrawable(R.drawable.thinking_bubble));
                try
                {
                    speechRecognizer.stopListening();
                }
                catch (Exception ex)
                {
                    //Never Mind...
                }
                if (music_muted)
                {
                    unmute_music();
                }
            }
        });
        StringRequest brain_location_request = new StringRequest(Request.Method.GET, brain_location_url, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String url)
            {
                brain_url = url;
                MainActivity.this.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (!first_run)
                        {
                            save("brain_url", brain_url);
                            busy = false;
                            status.setText("😀 Found it!");
                            status.setBackgroundDrawable(getDrawable(R.drawable.speaking_bubble));
                            if (in_app)
                            {
                                mute_music();
                                speechRecognizer.startListening(speechRecognizerIntent);
                            }
                            status.setText("🙂 Say whatever you want!");
                            status.setBackgroundDrawable(getDrawable(R.drawable.default_bubble));
                            update.setClickable(true);
                            alert("Brain Found", "DigiFriend's brain was located successfully", "OK");
                        }
                        else
                        {
                            save("first_run", "Complete");
                            save("brain_url", brain_url);
                            first_run = false;
                            busy = false;
                            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
                            {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                {
                                    askPermission();
                                }
                            }
                            else
                            {
                                alert("Welcome to DigiFriend!", "Please note that your phone's volume levels will be controlled by our app <b>only when you are in our app</b> for best user experience. Your audio will automatically be restored to your preset levels when you exit the app.", "OK");
                            }
                            if (in_app)
                            {
                                speechRecognizer.startListening(speechRecognizerIntent);
                            }
                            status.setText("🙂 Say whatever you want!");
                            status.setBackgroundDrawable(getDrawable(R.drawable.default_bubble));
                            update.setClickable(true);
                        }
                    }
                });
            }
        },
        new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                MainActivity.this.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        status.setText("😔 Can't find it");
                        status.setBackgroundDrawable(getDrawable(R.drawable.error_bubble));
                    }
                });
                if (first_run)
                {
                    alert("Brain not found", BRAIN_NOT_FOUND_FIRST, "OK");
                }
                else
                {
                    busy = false;
                    MainActivity.this.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (in_app)
                            {
                                mute_music();
                                speechRecognizer.startListening(speechRecognizerIntent);
                            }
                            status.setText("🙂 Say whatever you want!");
                            status.setBackgroundDrawable(getDrawable(R.drawable.default_bubble));
                            update.setClickable(true);
                            alert("Brain not found", "Unable to locate DigiFriend's brain right now.<br><br>Check your internet connection or try again after some time.", "OK");
                        }
                    });
                }
            }
        })
        {
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.125 Safari/537.36");
                params.put("referer", "https://abhaytr.tk/");
                return params;
            }
        };
        queue.add(brain_location_request);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        in_app = false;
        speechRecognizer.stopListening();
        unmute();
        if (music_muted)
        {
            unmute_music();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        in_app = true;
        mute();
        if (!busy)
        {
            mute_music();
            speechRecognizer.startListening(speechRecognizerIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0)
        {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                askPermission();
            }
        }
    }

}