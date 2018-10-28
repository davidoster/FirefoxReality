/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.vrbrowser.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import org.mozilla.geckoview.GeckoSession;
import org.mozilla.vrbrowser.R;
import org.mozilla.vrbrowser.audio.AudioEngine;
import org.mozilla.vrbrowser.browser.SessionStore;
import org.mozilla.vrbrowser.browser.SettingsStore;

public class CrashDialogWidget extends UIWidget implements WidgetManagerDelegate.FocusChangeListener {

    private static final String LOGTAG = "VRB";

    public interface CrashDialogDelegate {
        void onSendData();
    }

    private Button mLearnMoreButton;
    private Button mDoNotSendButton;
    private Button mSendDataButton;
    private CheckBox mSendDataCheckBox;
    private AudioEngine mAudio;
    private CrashDialogDelegate mCrashDialogDelegate;
    private TextView mCrashMessage;

    public CrashDialogWidget(Context aContext) {
        super(aContext);
        initialize(aContext);
    }

    public CrashDialogWidget(Context aContext, AttributeSet aAttrs) {
        super(aContext, aAttrs);
        initialize(aContext);
    }

    public CrashDialogWidget(Context aContext, AttributeSet aAttrs, int aDefStyle) {
        super(aContext, aAttrs, aDefStyle);
        initialize(aContext);
    }

    private void initialize(Context aContext) {
        inflate(aContext, R.layout.crash_dialog, this);

        mWidgetManager.addFocusChangeListener(this);

        mLearnMoreButton = findViewById(R.id.learnMoreButton);
        mDoNotSendButton = findViewById(R.id.dontSendButton);
        mSendDataButton = findViewById(R.id.sendDataButton);
        mSendDataCheckBox = findViewById(R.id.crashSendDataCheckbox);
        mCrashMessage = findViewById(R.id.crashMessage);

        mLearnMoreButton.setOnClickListener(view -> {
            if (mAudio != null) {
                mAudio.playSound(AudioEngine.Sound.CLICK);
            }

            GeckoSession session = SessionStore.get().getCurrentSession();
            if (session == null) {
                int sessionId = SessionStore.get().createSession();
                SessionStore.get().setCurrentSession(sessionId);
            }

            SessionStore.get().loadUri(getContext().getString(R.string.crash_dialog_learn_more_url));

            onDismiss();
        });

        mDoNotSendButton.setOnClickListener(view -> {
            if (mAudio != null) {
                mAudio.playSound(AudioEngine.Sound.CLICK);
            }

            onDismiss();
        });

        mSendDataButton.setOnClickListener(view -> {
            if (mAudio != null) {
                mAudio.playSound(AudioEngine.Sound.CLICK);
            }

            hide();

            if(mCrashDialogDelegate != null) {
                mCrashDialogDelegate.onSendData();
            }

            SettingsStore.getInstance(getContext()).setCrashReportingEnabled(mSendDataCheckBox.isChecked());
        });

        mSendDataCheckBox.setChecked(SettingsStore.getInstance(getContext()).isCrashReportingEnabled());
        mSendDataCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (mAudio != null) {
                mAudio.playSound(AudioEngine.Sound.CLICK);
            }
        });

        mCrashMessage.setText(getContext().getString(R.string.crash_dialog_message, getContext().getString(R.string.app_name)));

        mAudio = AudioEngine.fromContext(aContext);
    }

    @Override
    public void releaseWidget() {
        mWidgetManager.removeFocusChangeListener(this);

        super.releaseWidget();
    }

    @Override
    protected void initializeWidgetPlacement(WidgetPlacement aPlacement) {
        aPlacement.visible = false;
        aPlacement.width =  WidgetPlacement.dpDimension(getContext(), R.dimen.crash_dialog_width);
        aPlacement.height = WidgetPlacement.dpDimension(getContext(), R.dimen.crash_dialog_width);
        aPlacement.parentAnchorX = 0.5f;
        aPlacement.parentAnchorY = 0.5f;
        aPlacement.anchorX = 0.5f;
        aPlacement.anchorY = 0.5f;
        aPlacement.translationY = WidgetPlacement.unitFromMeters(getContext(), R.dimen.crash_dialog_world_y);
        aPlacement.translationZ = WidgetPlacement.unitFromMeters(getContext(), R.dimen.crash_dialog_world_z);
    }

    @Override
    public void show() {
        super.show();

        mWidgetManager.pushWorldBrightness(this, WidgetManagerDelegate.DEFAULT_DIM_BRIGHTNESS);
    }

    @Override
    public void hide() {
        super.hide();

        mWidgetManager.popWorldBrightness(this);
    }

    public void setCrashDialogDelegate(CrashDialogDelegate aDelegate) {
        mCrashDialogDelegate = aDelegate;
    }

    // WidgetManagerDelegate.FocusChangeListener
    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        if (oldFocus == this && isVisible()) {
            onDismiss();
        }
    }
}
