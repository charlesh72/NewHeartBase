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

package com.beakon.newheart.activities.home;

import android.os.Bundle;

import com.beakon.newheart.HabitsApplication;
import com.beakon.newheart.activities.ActivityModule;
import com.beakon.newheart.activities.BaseActivity;
import com.beakon.newheart.activities.ThemeSwitcher;
import com.beakon.newheart.preferences.Preferences;

/**
 * Created by Charles on 7/25/2017.
 *
 * Activity for the Home of the application. Top level navigation to features.
 */

public class HomeActivity extends BaseActivity {

    private HomeComponent component;

    private Preferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HabitsApplication app = (HabitsApplication) getApplicationContext();

        component = DaggerHomeComponent
            .builder()
            .appComponent(app.getComponent())
            .activityModule(new ActivityModule(this))
            .build();

        HomeRootView rootView = component.getRootView();
        HomeScreen screen = component.getScreen();
        HomeController controller = component.getController();

        setScreen(screen);
        screen.setMenu(component.getMenu());
        screen.setController(controller);

        prefs = app.getComponent().getPreferences();
    }
}