package com.wagebit.app;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Locale;

public class FreedomWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_UPDATE = "com.wagebit.app.ACTION_WIDGET_UPDATE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, FreedomWidgetProvider.class));
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("wagebit_inputs", Context.MODE_PRIVATE);
        
        double monthlyExpenses = read(prefs, "monthlyExpenses", 3000);
        double expenseYears = read(prefs, "expenseYears", 20);
        double costOfLiving = read(prefs, "costOfLiving", 3);
        double currentPrice = read(prefs, "btcPrice", 100000);
        double dcaAmount = read(prefs, "dcaAmount", 100);
        int dcaFreq = prefs.getInt("dcaFrequency", 1);
        double cagr = read(prefs, "cagr", 25);
        double age = read(prefs, "age", 35);
        String term = prefs.getString("freedomTerm", "Freedom");

        double totalBtc = 0;
        int lotCount = prefs.getInt("btcLotCount", 0);
        if (lotCount == 0 && prefs.contains("btcHeld")) {
            totalBtc = read(prefs, "btcHeld", 0.05);
        } else {
            for (int i = 0; i < lotCount; i++) {
                totalBtc += read(prefs, "btcLotAmount" + i, 0);
            }
        }

        double targetAmount = Projector.calculateInflatedGoal(monthlyExpenses, expenseYears, costOfLiving);
        Projector.Result result = Projector.project(totalBtc, currentPrice, targetAmount, dcaAmount, dcaFreq, cagr,
                context.getString(R.string.set_a_goal), context.getString(R.string.today), context.getString(R.string.enter_btc_price), context.getString(R.string.beyond_100_years));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.freedom_widget);
        views.setTextViewText(R.id.widget_title, term + " Clock");
        views.setTextViewText(R.id.widget_date, result.label);
        views.setTextColor(R.id.widget_date, result.hit ? 0xFF2DD4BF : 0xFFF87171); // success : red
        
        if (result.hit && result.yearsFromNow > 0) {
            views.setTextViewText(R.id.widget_years, String.format(Locale.US, "%.1f yrs", result.yearsFromNow));
            views.setViewVisibility(R.id.widget_years, View.VISIBLE);
            if (age > 0) {
                views.setTextViewText(R.id.widget_age, String.format(Locale.US, "Age: %.1f", age + result.yearsFromNow));
            } else {
                views.setTextViewText(R.id.widget_age, "");
            }
        } else {
            views.setViewVisibility(R.id.widget_years, View.GONE);
            views.setTextViewText(R.id.widget_age, "");
        }

        // Tap to update
        Intent intent = new Intent(context, FreedomWidgetProvider.class);
        intent.setAction(ACTION_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static double read(SharedPreferences prefs, String key, double fallback) {
        try {
            String val = prefs.getString(key, String.valueOf(fallback));
            return Double.parseDouble(val.replace(",", "").replace(" ", ""));
        } catch (Exception e) {
            return fallback;
        }
    }
}