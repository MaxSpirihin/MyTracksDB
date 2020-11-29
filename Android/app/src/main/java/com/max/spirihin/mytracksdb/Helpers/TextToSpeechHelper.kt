package com.max.spirihin.mytracksdb.Helpers

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.utilities.Print
import java.util.*
import kotlin.concurrent.schedule

@RequiresApi(Build.VERSION_CODES.O)
class TextToSpeechHelper(val context: Context) : TextToSpeech.OnInitListener {

    private val mTextToSpeech: TextToSpeech = TextToSpeech(context, this)
    private val mAudioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mFocusRequest : AudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).run {
        build()
    }
    private val mTimer = Timer()
    private var mMediaPlayer : MediaPlayer? = null
    private var hasAudioFocus = false
    private var mText : String? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsLang = mTextToSpeech.setLanguage(Locale.ENGLISH)
            if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                    || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(context, "The Language is not supported!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "TTS Initialization failed!", Toast.LENGTH_SHORT).show()
        }

        mTimer.schedule(500, 500) {
            val mediaPlayer = mMediaPlayer
            if (hasAudioFocus) {
                if (mediaPlayer != null) {
                    if (!mediaPlayer.isPlaying) {
                        mMediaPlayer = null
                        mTextToSpeech.speak(mText, TextToSpeech.QUEUE_ADD, null, "MyTracksDBRecordInfo")
                    }
                } else if (!mTextToSpeech.isSpeaking)
                    setAudioFocus(false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun speak(text: String) {
        setAudioFocus(true)
        mMediaPlayer = MediaPlayer.create(context, R.raw.notification_sound_1)
        mMediaPlayer?.start()
        mText = text
    }

    fun destroy() {
        mTextToSpeech.stop()
        mTextToSpeech.shutdown()
        setAudioFocus(false)
    }

    private fun setAudioFocus(focus : Boolean){
        Print.Log("SetAudioFocus $focus")
        if (focus == hasAudioFocus)
            return

        hasAudioFocus = focus
        if (hasAudioFocus)
            mAudioManager.requestAudioFocus(mFocusRequest)
        else
            mAudioManager.abandonAudioFocusRequest(mFocusRequest)
    }
}