/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.beakon.newheart.activities.habits.list.views;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.support.annotation.*;
import android.text.*;
import android.util.*;

import com.beakon.newheart.*;
import com.beakon.newheart.activities.common.views.*;
import com.beakon.newheart.activities.habits.list.*;
import com.beakon.newheart.preferences.*;
import com.beakon.newheart.utils.*;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.*;

public class HeaderView extends ScrollableChart
    implements Preferences.Listener, MidnightTimer.MidnightListener
{

    private int buttonCount;

    @Nullable
    private Preferences prefs;

    @Nullable
    private MidnightTimer midnightTimer;

    private int dailyScore;

    private final TextPaint paint;
    private final TextPaint scorePaint;

    private RectF rect;

    public HeaderView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        if (isInEditMode())
        {
            setButtonCount(5);
        }

        Context appContext = context.getApplicationContext();
        if (appContext instanceof HabitsApplication)
        {
            HabitsApplication app = (HabitsApplication) appContext;
            prefs = app.getComponent().getPreferences();
        }

        if (context instanceof ListHabitsActivity)
        {
            ListHabitsActivity activity = (ListHabitsActivity) context;
            midnightTimer = activity.getListHabitsComponent().getMidnightTimer();
        }


        Resources res = context.getResources();
        setScrollerBucketSize((int) res.getDimension(R.dimen.checkmarkWidth));

        StyledResources sr = new StyledResources(context);
        paint = new TextPaint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setTextSize(getResources().getDimension(R.dimen.tinyTextSize));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(sr.getColor(R.attr.mediumContrastTextColor));

        scorePaint = new TextPaint(paint);
        scorePaint.setTextSize(getResources().getDimension(R.dimen.regularTextSize));
        scorePaint.setColor(Color.BLACK);

        rect = new RectF();

        dailyScore = 0;
    }

    @Override
    public void atMidnight()
    {
        post(() -> invalidate());
    }

    @Override
    public void onCheckmarkOrderChanged()
    {
        updateDirection();
        postInvalidate();
    }

    public void setButtonCount(int buttonCount)
    {
        this.buttonCount = buttonCount;
        postInvalidate();
    }

    @Override
    protected void onAttachedToWindow()
    {
        updateDirection();
        super.onAttachedToWindow();
        if (prefs != null) prefs.addListener(this);
        if (midnightTimer != null) midnightTimer.addListener(this);
    }

    private void updateDirection()
    {
        int direction = -1;
        if (shouldReverseCheckmarks()) direction *= -1;
        if (InterfaceUtils.isLayoutRtl(this)) direction *= -1;
        setDirection(direction);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        if (midnightTimer != null) midnightTimer.removeListener(this);
        if (prefs != null) prefs.removeListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) getContext()
            .getResources()
            .getDimension(R.dimen.checkmarkHeight);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        GregorianCalendar day = DateUtils.getStartOfTodayCalendar();
        Resources res = getContext().getResources();
        float width = res.getDimension(R.dimen.checkmarkWidth);
        float height = res.getDimension(R.dimen.checkmarkHeight);
        boolean reverse = shouldReverseCheckmarks();
        boolean isRtl = InterfaceUtils.isLayoutRtl(this);

        int xScorePos = canvas.getWidth() / 4;
        int yScorePos = (int) ((canvas.getHeight() / 2) - (scorePaint.descent() + scorePaint.ascent()) / 2);
        canvas.drawText("Score: " + dailyScore, xScorePos, yScorePos, scorePaint);

        day.add(GregorianCalendar.DAY_OF_MONTH, -getDataOffset());
        float em = paint.measureText("m");

        for (int i = 0; i < buttonCount; i++)
        {
            rect.set(0, 0, width, height);
            rect.offset(canvas.getWidth(), 0);

            if(reverse) rect.offset(- (i + 1) * width, 0);
            else rect.offset((i - buttonCount) * width, 0);

            if (isRtl) rect.set(canvas.getWidth() - rect.right, rect.top,
                canvas.getWidth() - rect.left, rect.bottom);

            String text = DateUtils.formatHeaderDate(day).toUpperCase();
            String[] lines = text.split("\n");

            int y1 = (int)(rect.centerY() - 0.25 * em);
            int y2 = (int)(rect.centerY() + 1.25 * em);

            canvas.drawText(lines[0], rect.centerX(), y1, paint);
            canvas.drawText(lines[1], rect.centerX(), y2, paint);
            day.add(GregorianCalendar.DAY_OF_MONTH, -1);
        }
    }

    private boolean shouldReverseCheckmarks()
    {
        if (prefs == null) return false;
        return prefs.shouldReverseCheckmarks();
    }


    public void setDailyScore(int dailyScore) {
        this.dailyScore = dailyScore;
        postInvalidate();
    }

}
