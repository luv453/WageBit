package com.wagebit.app;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Projector {

    public static class Result {
        public final String label;
        public final boolean hit;
        public final double yearsFromNow;

        public Result(String label, boolean hit, double yearsFromNow) {
            this.label = label;
            this.hit = hit;
            this.yearsFromNow = yearsFromNow;
        }
    }

    public static Result project(double btcHeld, double btcPrice, double targetAmount, double dcaAmount, int frequencyIndex, double cagrPercent, 
                                 String setGoalStr, String todayStr, String enterPriceStr, String beyondStr) {
        if (targetAmount <= 0) {
            return new Result(setGoalStr, false, 0);
        }
        if (btcPrice > 0 && btcHeld * btcPrice >= targetAmount) {
            return new Result(todayStr, true, 0);
        }
        if (btcPrice <= 0) {
            return new Result(enterPriceStr, false, 0);
        }

        int daysPerStep = getFrequencyDays(frequencyIndex);
        double currentBtc = Math.max(0, btcHeld);
        double price = btcPrice;
        double annualGrowth = Math.max(-0.999, cagrPercent / 100.0);
        Calendar start = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        int maxSteps = Math.max(1, (int) Math.ceil(36500.0 / daysPerStep));

        for (int step = 1; step <= maxSteps; step++) {
            price *= Math.pow(1.0 + annualGrowth, daysPerStep / 365.25);
            if (dcaAmount > 0) {
                currentBtc += dcaAmount / price;
            }
            date.add(Calendar.DAY_OF_YEAR, daysPerStep);
            if (currentBtc * price >= targetAmount) {
                double yearsFromNow = (date.getTimeInMillis() - start.getTimeInMillis()) / (86400000.0 * 365.25);
                java.text.SimpleDateFormat displayFormat = new java.text.SimpleDateFormat("MMM d, yyyy", Locale.US);
                return new Result(displayFormat.format(date.getTime()), true, yearsFromNow);
            }
        }

        return new Result(beyondStr, false, 100);
    }

    private static int getFrequencyDays(int position) {
        if (position == 0) return 1; // Daily
        if (position == 2) return 30; // Monthly
        return 7; // Weekly (default or position 1)
    }

    public static double calculateInflatedGoal(double monthlyExpenses, double years, double costOfLivingPercent) {
        if (monthlyExpenses <= 0 || years <= 0) {
            return 0;
        }
        double annualGrowth = Math.max(-0.999, costOfLivingPercent / 100.0);
        int fullYears = (int) Math.floor(years);
        double partialYear = years - fullYears;
        double total = 0;
        for (int year = 0; year < fullYears; year++) {
            total += monthlyExpenses * 12.0 * Math.pow(1.0 + annualGrowth, year);
        }
        if (partialYear > 0) {
            total += monthlyExpenses * 12.0 * partialYear * Math.pow(1.0 + annualGrowth, fullYears);
        }
        return total;
    }
}