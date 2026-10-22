package org.schabi.newpipe.player.helper;

import android.content.Context;
import android.content.Intent;
import android.media.audiofx.AudioEffect;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsListener;

public class AudioReactor implements AnalyticsListener {

    private final ExoPlayer player;
    private final Context context;

    public AudioReactor(@NonNull final Context context,
                        @NonNull final ExoPlayer player) {
        this.player = player;
        this.context = context;
        player.addAnalyticsListener(this);
        // Per-app volume: restore NewPipe's own player volume
        player.setVolume(context.getSharedPreferences("player_volume", Context.MODE_PRIVATE)
                .getFloat("v", 1f));
    }

    public void dispose() {
        player.removeAnalyticsListener(this);
        notifyAudioSessionUpdate(false, player.getAudioSessionId());
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Audio Manager
    //////////////////////////////////////////////////////////////////////////*/

    public int getVolume() {
        return Math.round(player.getVolume() * 100);
    }

    public void setVolume(final int volume) {
        final float level = volume / 100f;
        player.setVolume(level);
        context.getSharedPreferences("player_volume", Context.MODE_PRIVATE)
                .edit().putFloat("v", level).apply();
    }

    public int getMaxVolume() {
        return 100;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Audio Processing
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void onAudioSessionIdChanged(@NonNull final EventTime eventTime,
                                        final int audioSessionId) {
        notifyAudioSessionUpdate(true, audioSessionId);
    }
    private void notifyAudioSessionUpdate(final boolean active, final int audioSessionId) {
        final Intent intent = new Intent(active
                ? AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION
                : AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId);
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.getPackageName());
        context.sendBroadcast(intent);
    }
}
