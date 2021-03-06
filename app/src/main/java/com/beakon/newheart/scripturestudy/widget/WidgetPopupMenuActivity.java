/*
 * Copyright 2015. Dan Mercer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.beakon.newheart.scripturestudy.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.beakon.newheart.R;
import com.beakon.newheart.scripturestudy.Scripture;
import com.beakon.newheart.scripturestudy.memorize.MemorizeActivity;
import com.beakon.newheart.scripturestudy.scriptureview.AddNoteActivity;
import com.beakon.newheart.scripturestudy.scriptureview.ScriptureIntent;
import com.beakon.newheart.scripturestudy.scriptureview.ScriptureViewActivity;


public class WidgetPopupMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private Scripture mScripture;
    private int mAppWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_popup_menu);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            // Exit with an error if no app widget ID was in the intent
            Log.e("WidgetPopupMenuActivity", "No appwidget ID provided in intent");
            finish();
            return;
        }

        // Get the Scripture from the intent.
        mScripture = intent.getParcelableExtra(ScriptureIntent.EXTRA_SCRIPTURE);
        if (mScripture == null) {
            // Exit with an error if no Scripture was in the intent
            Log.e("WidgetPopupMenuActivity", "No Scripture provided in intent");
            finish();
            return;
        }
        setTitle(mScripture.getReference());

        // Get Buttons
        Button buttonChange = (Button) findViewById(R.id.button_change_scripture);
        Button buttonView = (Button) findViewById(R.id.button_view_scripture);
        Button buttonMemorize = (Button) findViewById(R.id.button_memorize);
        Button buttonAddNote = (Button) findViewById(R.id.button_add_note);

        buttonChange.setOnClickListener(this);

        // If the scripture doesn't exist, only give them the option to change the scripture.
        if (!mScripture.fileExists(this)) {
            buttonChange.setText("Choose new scripture");
            buttonView.setVisibility(View.GONE);
            buttonMemorize.setVisibility(View.GONE);
            buttonAddNote.setVisibility(View.GONE);
        } else {
            buttonView.setOnClickListener(this);
            buttonMemorize.setOnClickListener(this);
            buttonAddNote.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_change_scripture) {
            launchConfigActivity();
            return;
        } else if (i == R.id.button_view_scripture) {
            startActivity(new ScriptureIntent(this, ScriptureViewActivity.class, mScripture));
            return;
        } else if (i == R.id.button_memorize) {
            startActivity(new ScriptureIntent(this, MemorizeActivity.class, mScripture));
            return;
        } else if (i == R.id.button_add_note) {
            startActivity(new ScriptureIntent(this, AddNoteActivity.class, mScripture));
            return;
        }
    }

    private void launchConfigActivity() {
        Intent i = new Intent(this, ScriptureAppWidgetConfigureActivity.class);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        startActivity(i);
        finish();
    }
}
