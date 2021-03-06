/*
 * Copyright (C) 2017 Charles Hancock
 *
 * NewHeart is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * NewHeart is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.beakon.newheart.scripturestudy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.beakon.newheart.R;
import com.beakon.newheart.scripturestudy.list.ScriptureListActivity;
import com.beakon.newheart.scripturestudy.scriptureview.Note;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddScriptureTextActivity extends BaseShareActivity {

    @BindView(R.id.scripture_title)
    EditText titleTextView;

    @BindView(R.id.scripture_text)
    EditText bodyTextView;

    @BindView(R.id.scripture_note)
    EditText notesTextView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);

        ButterKnife.bind(this);

        // Set up ActionBar with support API
        setSupportActionBar(toolbar);

        // Get the text that was shared with us from the start intent
        Intent startIntent = getIntent();
        String text = startIntent.getStringExtra(Intent.EXTRA_TEXT);
        if (text == null) {
            text = startIntent.getStringExtra(Intent.EXTRA_TITLE);
        } else {
            String titleString = startIntent.getStringExtra(Intent.EXTRA_TITLE);
            if (titleString != null && !titleString.isEmpty()) {
                titleTextView.setText(titleString);
            }
        }
        if (text == null) {
            Log.e("AddScriptureText",
                    "AddScriptureTextActivity was started without a TEXT or TITLE extra");
        }
        bodyTextView.setText(cleanupText(text));

        if (titleTextView.getText().toString().isEmpty()) {
            // If we didn't get a title from the intent, try to parse the scripture reference
            if (text != null) {
                Matcher m = Pattern.compile(
                        "lds.org/scriptures/[\\w-]+/([\\w-]+)/(\\d+).(\\d+)(?:(?:,\\d+)*,(\\d+))?"
                ).matcher(text);
                if (m.find()) {
                    String book = m.group(1);
                    String chap = m.group(2);
                    String verseStart = m.group(3);
                    String verseEnd = m.group(4);

                    if (book.equals("dc")) {
                        // Special case for "dc" --> "D&C"
                        book = "D&C";
                    } else {
                        // Replace all "-" characters with blank space
                        book = book.replaceAll("-", " ");
                        // Make all words uppercase, unless they are "of"
                        Matcher m1 = Pattern.compile("\\b[a-z][A-z]*\\b").matcher(book);
                        while (m1.find()) {
                            String word = m1.group();
                            if (word.equalsIgnoreCase("of"))
                                continue; // We don't want to capitalize "of"
                            char[] wordChars = word.toCharArray();
                            wordChars[0] = Character.toUpperCase(wordChars[0]);
                            book = book.replaceFirst(word, new String(wordChars));
                        }
                    }

                    String ref; // Will be filled with the parsed scripture reference
                    if (verseEnd == null || verseEnd.isEmpty()) { // If the reference is just one verse, not a range
                        ref = String.format("%s %s:%s", book, chap, verseStart);
                    } else {
                        ref = String.format("%s %s:%s-%s", book, chap, verseStart, verseEnd);
                    }
                    titleTextView.setText(ref);
                }
            }
        }
    }

    /**
     * Gets rid of any words in the text that contain slashes or carets
     */
    private String cleanupText(String text) {
        String[] tokens = text.split("\\s"); // Split into word-like tokens
        for (String token : tokens) {
            // If the token has any slashes or carets, remove it
            if (token.matches(".*[\\\\/<>].*")) {
                text = text.replace(token, "");
            }
        }
        return text.trim(); // Trim any extra whitespace
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_done) {
            Log.d("AddScriptureText", "Done button pressed");
            // Construct a new Scripture object using the text provided by the user.
            String reference = titleTextView.getText().toString();
            String body = bodyTextView.getText().toString();
            String note = notesTextView.getText().toString();

            // Make sure the user entered something in both fields.
            if (reference.isEmpty()) {
                Toast.makeText(this,
                        "Please enter a scripture reference for the title",
                        Toast.LENGTH_LONG).show();
                return false;
            }
            if (body.isEmpty()) {
                Toast.makeText(this,
                        "Please enter a scripture passage",
                        Toast.LENGTH_LONG).show();
                return false;
            }

            //Create new Scripture object and save to file
            Scripture s = new Scripture(reference, body, ScriptureListActivity.Category.IN_PROGRESS);
            s.writeToFile(this);

            //Create new Note object, associate with scripture and save to file
            long time = System.currentTimeMillis();
            File notefile = new File(getDir(Scripture.NOTES_DIR, Context.MODE_PRIVATE),
                    s.getFilename());
            Note.writeNoteToFile(time, note, notefile);
            //Attempt to share the note with ACC friend(s)
            share(note, s.getReference());

            //Report to user
            Toast.makeText(this, "Added " + reference, Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
