package name.bagi.levente.pedometer;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class Utils implements TextToSpeech.OnInitListener {
    private static final String TAG = "Utils";
    private Context mContext;

    private static Utils instance;

    public Utils(Context context) {
        mContext = context;
        instance = this;
    }
     
    public static Utils getInstance() {
        return instance;
    }
    
    /********** SPEAKING **********/
    
    private TextToSpeech mTts;
    private boolean mSpeak = false;
    private boolean mSpeakingEngineAvailable = false;

    public void initTTS() {
        // Initialize text-to-speech. This is an asynchronous operation.
        // The OnInitListener (second argument) is called after initialization completes.
        mTts = new TextToSpeech(mContext,
            this  // TextToSpeech.OnInitListener
            );
    }
    public void shutdownTTS() {
        mSpeakingEngineAvailable = false;
        mTts.shutdown();
    }
    public void say(String text) {
        if (mSpeak && mSpeakingEngineAvailable) {
            mTts.speak(text,
                    TextToSpeech.QUEUE_ADD,  // Drop all pending entries in the playback queue.
                    null);
        }
    }

    // Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
               // Language data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            } else {
                Log.e(TAG, "TextToSpeech Initialized.");
                mSpeakingEngineAvailable = true;
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

    public void setSpeak(boolean speak) {
        mSpeak = speak;
    }

    public boolean isSpeakingEnabled() {
        return mSpeak;
    }

    public boolean isSpeakingNow() {
        return mTts.isSpeaking();
    }

    public void ding() {
    }
}
