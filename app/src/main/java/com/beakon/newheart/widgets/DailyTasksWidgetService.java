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

package com.beakon.newheart.widgets;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.beakon.newheart.R;
import com.beakon.newheart.activities.service.ActOfService;
import com.beakon.newheart.activities.service.ActOfServiceDay;
import com.beakon.newheart.activities.service.DaysActsOfService;
import com.beakon.newheart.activities.service.manager.ServiceManagerActivity;
import com.beakon.newheart.utils.DateUtils;

import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmResults;

public class DailyTasksWidgetService extends Service {

    PendingIntent alarmService;
    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        buildUpdate();

        return super.onStartCommand(intent, flags, startId);
    }

    private void buildUpdate()
    {
        Log.d("DailyTasksService", "buildUpdate()");

//        RemoteViews view = new RemoteViews(getPackageName(), R.layout.widget_daily_tasks);
//
//        view.setTextViewText(R.id.widgetDTTVLabel, DailyTasksWidget.getTaskText());
//
//        // Push update for this widget to the home screen
//        ComponentName thisWidget = new ComponentName(this, DailyTasksWidget.class);
//        AppWidgetManager manager = AppWidgetManager.getInstance(this);
//        manager.updateAppWidget(thisWidget, view);


        // Start the ServiceManager Activity IF there are still acts of service uncompleted.
        boolean taskRemaining = true;
        DaysActsOfService day = DaysActsOfService.findDay(DateUtils.getStartOfToday());
        if (day.acts.size() < 1) {
            taskRemaining = false;
        }
        // TODO: 11/20/2017 Also check if there are goals waiting to be marked off

        Context context = getApplicationContext();
        if (taskRemaining) {
            Intent intent = new Intent (context, ServiceManagerActivity.class);
            intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity (intent);

            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        }


        final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final GregorianCalendar calendar = new GregorianCalendar();

        int interval =  Double.valueOf(Math.random() * 60).intValue() + 120;
        calendar.add(GregorianCalendar.MINUTE, interval);

        // Instead of setting an alarm after 10/before 9 set the hour to 9am
        int hourOfDay = calendar.get(GregorianCalendar.HOUR_OF_DAY);
        if (hourOfDay > 21 || hourOfDay < 9) {
            calendar.set(GregorianCalendar.HOUR_OF_DAY, 8);
        }

        long time = calendar.getTimeInMillis();
        final Intent alarmIntent = new Intent(context, DailyTasksWidgetService.class);
        if (alarmService == null) {
            alarmService = PendingIntent.getService(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        }
        m.set(AlarmManager.RTC, time, alarmService);

        this.stopSelf();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
