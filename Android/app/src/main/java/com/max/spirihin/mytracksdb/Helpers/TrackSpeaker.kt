package com.max.spirihin.mytracksdb.Helpers

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.garmin.fit.Bool
import com.max.spirihin.mytracksdb.R
import com.max.spirihin.mytracksdb.core.Track
import com.max.spirihin.mytracksdb.core.TrackRecordManager
import com.max.spirihin.mytracksdb.utilities.Print
import com.max.spirihin.mytracksdb.utilities.Utils
import java.util.*
import kotlin.concurrent.schedule

@RequiresApi(Build.VERSION_CODES.O)
class TrackSpeaker(val context: Context) : TextToSpeech.OnInitListener {

    private val mTextToSpeech: TextToSpeech = TextToSpeech(context, this)
    private val mAudioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mFocusRequest : AudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).run {
        build()
    }
    private val mTimer = Timer()
    private var mMediaPlayer : MediaPlayer? = null
    private var hasAudioFocus = false
    private var mText : String? = null

    private var mDistance = 0

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

    fun onTrackUpdated(track : Track)
    {
        val speechDistance = getSpeechDistance(track)
        val oldSpeechSegmentsCount = mDistance / speechDistance
        mDistance = track.distance
        val newSpeechSegmentsCount = mDistance / speechDistance
        if (newSpeechSegmentsCount > oldSpeechSegmentsCount) {
            speak(generateSpeechString(track))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun speak(text: String) {
        setAudioFocus(true)
        Print.Log("[TextToSpeech] ")
        mMediaPlayer = MediaPlayer.create(context, R.raw.notification_sound_1)
        mMediaPlayer?.start()
        mText = text
    }

    fun destroy() {
        mTimer.cancel()
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

    private fun getSpeechDistance(track: Track) : Int {
        val distance = SpeakerPreferences.getSpeakDistance(track.exerciseType)
        return if (distance <= 0) Int.MAX_VALUE else distance
    }

    private fun needSpeak(track: Track, speakerInfoType: SpeakerInfoType) : Boolean {
        return SpeakerPreferences.getNeedSpeakType(track.exerciseType, speakerInfoType)
    }

    private fun generateSpeechString(track: Track) : String {
        val speechDistance = getSpeechDistance(track)
        val distance = track.distance - track.distance % speechDistance
        val distanceString = if (distance % 1000 == 0) (distance / 1000).toString() else Utils.distanceToStringShort(track.distance, 1)

        var string = ""

        if (needSpeak(track, SpeakerInfoType.TOTAL_DISTANCE))
            string += "Pass $distanceString kilometers. "

        if (needSpeak(track, SpeakerInfoType.TOTAL_TIME))
            string += "Total time is ${Utils.timeToSpeechString(track.duration)}. "

        if (needSpeak(track, SpeakerInfoType.AVERAGE_PACE))
            string += "Pace is ${Utils.paceToString(track.pace, false)}. "

        //TODO if (needSpeak(track, SpeakerInfoType.CURRENT_PACE))

        if (needSpeak(track, SpeakerInfoType.CURRENT_HEARTRATE) && track.currentHeartrate > 0)
            string += "Heartrate is ${track.currentHeartrate}. "

        if (needSpeak(track, SpeakerInfoType.AVERAGE_HEARTRATE) && track.averageHeartrate > 0)
            string += "Average heartrate is ${track.averageHeartrate}. "

        return string
    }
}