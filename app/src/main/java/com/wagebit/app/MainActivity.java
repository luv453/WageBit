package com.wagebit.app;

import androidx.annotation.NonNull;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Editable;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PREFS = "wagebit_inputs";
    private static final int BG = Color.rgb(15, 23, 42);
    private static final int SURFACE = Color.rgb(24, 34, 56);
    private static final int SURFACE_ALT = Color.rgb(32, 44, 69);
    private static final int ORANGE = Color.rgb(247, 147, 26);
    private static final int GREEN = Color.rgb(45, 212, 191);
    private static final int TEXT = Color.rgb(248, 250, 252);
    private static final int MUTED = Color.rgb(203, 213, 225);
    private static final int RED = Color.rgb(248, 113, 113);
    private static final String MASK = "******";
    private static final SimpleDateFormat INPUT_DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat DISPLAY_DATE = new SimpleDateFormat("MMM d, yyyy", Locale.US);

    private final List<BtcLot> btcLots = new ArrayList<>();
    private SharedPreferences prefs;
    private LinearLayout btcLotList;
    private TextView heroTitle;
    private TextView hoursSavedValue;
    private TextView heroSubtitle;
    private TextView freedomCardTitle;
    private TextView freedomDateValue;
    private TextView freedomAgeValue;
    private TextView daysSavedValue;
    private TextView monthsSavedValue;
    private TextView efficiencyTitle;
    private TextView efficiencyValue;
    private TextView efficiencyDetailValue;
    private TextView progressTitle;
    private TextView progressValue;
    private TextView targetAmountValue;
    private TextView targetBtcValue;
    private TextView targetFutureBtcValue;
    private TextView requiredPriceValue;
    private TextView totalValueValue;
    private TextView hourlyWageValue;
    private TextView netIncomeValue;
    private TextView comparisonSummaryValue;
    private TextView updatedValue;
    private ProgressBar freedomProgress;
    private ComparisonChartView comparisonChart;
    private EditText monthlyIncomeInput;
    private EditText monthlyExpensesInput;
    private EditText costOfLivingInput;
    private EditText weeklyHoursInput;
    private EditText dailyHoursInput;
    private EditText ageInput;
    private EditText btcPriceInput;
    private EditText expenseYearsInput;
    private EditText dcaAmountInput;
    private EditText cagrInput;
    private EditText timeCapsuleInput;
    private EditText freedomTermInput;
    private Spinner dcaFrequencyInput;
    private Switch valueVisibilitySwitch;
    private Switch progressAmountsVisibilitySwitch;
    private boolean restoring;
    private boolean showBitcoinValue = true;
    private boolean showProgressAmounts = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        INPUT_DATE.setLenient(false);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        restoring = true;
        setContentView(buildUi());
        loadInputs();
        recalculate();
    }

    private View buildUi() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(BG);

        LinearLayout root = column();
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        scrollView.addView(root);

        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout headerLeft = column();
        headerLeft.addView(wageBitTitle());
        header.addView(headerLeft, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        updatedValue = text("Time is Money", 12, MUTED, Typeface.NORMAL);
        header.addView(updatedValue);
        root.addView(header);

        LinearLayout hero = card();
        hero.setPadding(dp(20), dp(18), dp(20), dp(18));
        heroTitle = text("Time Capsule", 14, ORANGE, Typeface.BOLD);
        hero.addView(heroTitle);
        hoursSavedValue = text("0.0 hours", 46, ORANGE, Typeface.BOLD);
        hero.addView(hoursSavedValue);
        heroSubtitle = text("Enter your work profile and BTC lots to calculate stored labor.", 15, MUTED, Typeface.NORMAL);
        hero.addView(heroSubtitle);
        root.addView(hero, blockParams());

        LinearLayout freedomCard = card();
        freedomCardTitle = text("Freedom Clock", 20, TEXT, Typeface.BOLD);
        freedomCard.addView(freedomCardTitle);
        freedomDateValue = text("Not projected", 30, TEXT, Typeface.BOLD);
        freedomCard.addView(freedomDateValue);
        freedomAgeValue = addSummary(freedomCard, "Age", "Enter current age");
        root.addView(freedomCard, blockParams());

        LinearLayout timeMetrics = row();
        daysSavedValue = addMetric(timeMetrics, "Work Days Saved", "0.0 days", "hours saved / worked hours");
        monthsSavedValue = addMetric(timeMetrics, "Work Months Saved", "0.00 months", "hours saved / hours");
        root.addView(timeMetrics, blockParams());

        LinearLayout efficiencyCard = card();
        efficiencyTitle = text("BTC Time Efficiency", 20, TEXT, Typeface.BOLD);
        efficiencyCard.addView(efficiencyTitle);
        efficiencyValue = text("0.0%", 30, ORANGE, Typeface.BOLD);
        efficiencyCard.addView(efficiencyValue);
        efficiencyDetailValue = text("Stored BTC time against work time since your earliest acquired date.", 13, MUTED, Typeface.NORMAL);
        efficiencyCard.addView(efficiencyDetailValue);
        root.addView(efficiencyCard, blockParams());

        LinearLayout progressCard = card();
        progressTitle = text("Freedom Progress", 20, TEXT, Typeface.BOLD);
        progressCard.addView(progressTitle);
        progressValue = text("0.0% funded", 28, ORANGE, Typeface.BOLD);
        progressCard.addView(progressValue);
        freedomProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        freedomProgress.setMax(1000);
        freedomProgress.setProgressTintList(ColorStateList.valueOf(ORANGE));
        progressCard.addView(freedomProgress, blockParams());
        targetAmountValue = addSummary(progressCard, "Goal amount", "$0.00");
        targetBtcValue = addSummary(progressCard, "BTC goal at current price", "0.00000000 BTC");
        targetFutureBtcValue = addSummary(progressCard, "BTC goal at future price", "0.00000000 BTC");
        requiredPriceValue = addSummary(progressCard, "Goal met if price now", "$0.00");
        root.addView(progressCard, blockParams());

        LinearLayout comparisonContent = column();
        comparisonChart = new ComparisonChartView(this);
        comparisonContent.addView(comparisonChart, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(220)));
        comparisonSummaryValue = text("Projected values use the same starting value and DCA cadence.", 13, MUTED, Typeface.NORMAL);
        comparisonContent.addView(comparisonSummaryValue, blockParams());
        comparisonContent.addView(legendRow("BTC", ORANGE, "S&P 500", GREEN), blockParams());
        comparisonContent.addView(legendRow("Nasdaq-100", Color.rgb(96, 165, 250), "Gold", Color.rgb(250, 204, 21)), blockParams());
        root.addView(expandableCard("Growth Comparison", comparisonContent, false), blockParams());

        LinearLayout metrics = column();
        root.addView(metrics, blockParams());
        totalValueValue = addMetric(metrics, "Total Bitcoin Position", "$0.00", "0.00000000 BTC");
        hourlyWageValue = addMetric(metrics, "Real Hourly Wage", "$0.00/hr", "(income - fixed expenses) / hrs worked");
        netIncomeValue = addMetric(metrics, "Monthly Net Income", "$0.00", "Income left after fixed expenses");

        LinearLayout workContent = column();
        monthlyIncomeInput = addInput(workContent, "Monthly income", "4000", "after taxes");
        monthlyExpensesInput = addInput(workContent, "Monthly expenses", "3000", "Fixed needs like housing, utilities, debt/liabilities, etc.");
        costOfLivingInput = addInput(workContent, "Cost of living increase %", "3", "Represents inflation applied to future monthly expenses.");
        weeklyHoursInput = addInput(workContent, "Weekly hours worked", "40", "include weekly/daily commute times for more accuracy");
        dailyHoursInput = addInput(workContent, "Daily hours worked", "8", null);
        ageInput = addInput(workContent, "Current age", "35", "Used to estimate your age at the projected date.");
        root.addView(expandableCard("Labor Profile", workContent), blockParams());

        LinearLayout bitcoinContent = column();
        btcPriceInput = addInput(bitcoinContent, "Current BTC price (USD)", "100000", null);
        expenseYearsInput = addInput(bitcoinContent, "Years of expenses wanted", "20", null);
        dcaAmountInput = addInput(bitcoinContent, "DCA buy amount (USD)", "100", "Dollar-cost averaging: buying a set amount on a regular schedule.");
        bitcoinContent.addView(text("DCA frequency", 13, MUTED, Typeface.BOLD));
        dcaFrequencyInput = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Daily", "Weekly", "Monthly"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dcaFrequencyInput.setAdapter(adapter);
        dcaFrequencyInput.setBackgroundColor(SURFACE_ALT);
        dcaFrequencyInput.setPadding(dp(8), dp(6), dp(8), dp(6));
        dcaFrequencyInput.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                recalculate();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
        bitcoinContent.addView(dcaFrequencyInput, blockParams());
        cagrInput = addInput(bitcoinContent, "BTC CAGR %", "25", "Compound annual growth rate. S&P 500 Approx. 10-Yr CAGR ~12%, Nasdaq-100 ~15%, Gold ~7%, BTC > 50%");
        LinearLayout lotHeader = row();
        lotHeader.setGravity(Gravity.CENTER_VERTICAL);
        lotHeader.addView(text("BTC Lots", 18, TEXT, Typeface.BOLD), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        Button addLotButton = button("+ Add");
        addLotButton.setOnClickListener(v -> {
            addBtcLot("", todayString());
            persistInputs();
            recalculate();
        });
        lotHeader.addView(addLotButton);
        bitcoinContent.addView(lotHeader, blockParams());
        bitcoinContent.addView(text("Enter each BTC amount held/bought and the date it was acquired. Use YYYY-MM-DD.", 13, MUTED, Typeface.NORMAL));
        btcLotList = column();
        bitcoinContent.addView(btcLotList);
        root.addView(expandableCard("Bitcoin Position", bitcoinContent), blockParams());


        LinearLayout settingsContent = column();
        timeCapsuleInput = addInput(settingsContent, "Vault Name", "Time Capsule", null);
        freedomTermInput = addInput(settingsContent, "Freedom word", "Freedom", "This changes the wording for the date and progress sections.");
        valueVisibilitySwitch = new Switch(this);
        valueVisibilitySwitch.setText(R.string.show_total_btc_value);
        valueVisibilitySwitch.setTextColor(TEXT);
        valueVisibilitySwitch.setTextSize(14);
        valueVisibilitySwitch.setChecked(true);
        valueVisibilitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showBitcoinValue = isChecked;
            persistInputs();
            recalculate();
        });
        settingsContent.addView(valueVisibilitySwitch, blockParams());
        progressAmountsVisibilitySwitch = new Switch(this);
        progressAmountsVisibilitySwitch.setText(R.string.show_goal_progress_values);
        progressAmountsVisibilitySwitch.setTextColor(TEXT);
        progressAmountsVisibilitySwitch.setTextSize(14);
        progressAmountsVisibilitySwitch.setChecked(true);
        progressAmountsVisibilitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showProgressAmounts = isChecked;
            persistInputs();
            recalculate();
        });
        settingsContent.addView(progressAmountsVisibilitySwitch, blockParams());
        root.addView(expandableCard("Settings", settingsContent, false), blockParams());

        TextView disclaimer = text(getString(R.string.disclaimer_text), 12, MUTED, Typeface.NORMAL);
        disclaimer.setGravity(Gravity.CENTER);
        root.addView(disclaimer, blockParams());

        TextView donation = text(getString(R.string.donation_address), 12, MUTED, Typeface.NORMAL);
        donation.setGravity(Gravity.CENTER);
        root.addView(donation, blockParams());

        return scrollView;
    }

    private LinearLayout expandableCard(String title, LinearLayout content) {
        return expandableCard(title, content, true);
    }

    private LinearLayout expandableCard(String title, LinearLayout content, boolean expanded) {
        LinearLayout card = card();
        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView titleView = text(title, 20, TEXT, Typeface.BOLD);
        Button toggle = button(expanded ? "v" : ">");
        header.addView(titleView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        header.addView(toggle);
        card.addView(header);
        card.addView(content);
        content.setVisibility(expanded ? View.VISIBLE : View.GONE);
        toggle.setOnClickListener(v -> {
            boolean show = content.getVisibility() != View.VISIBLE;
            content.setVisibility(show ? View.VISIBLE : View.GONE);
            toggle.setText(show ? "v" : ">");
        });
        return card;
    }

    private LinearLayout legendRow(String firstLabel, int firstColor, String secondLabel, int secondColor) {
        LinearLayout layout = row();
        layout.addView(legendItem(firstLabel, firstColor), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        layout.addView(legendItem(secondLabel, secondColor), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        return layout;
    }

    private TextView legendItem(String label, int color) {
        TextView item = text("* " + label, 13, color, Typeface.BOLD);
        item.setGravity(Gravity.CENTER);
        return item;
    }

    private EditText addInput(LinearLayout parent, String label, String hint, String info) {
        parent.addView(text(label, 13, MUTED, Typeface.BOLD));
        if (info != null && !info.isEmpty()) {
            TextView help = text(info, 12, MUTED, Typeface.NORMAL);
            help.setPadding(0, dp(2), 0, 0);
            parent.addView(help);
        }
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextColor(TEXT);
        input.setHintTextColor(Color.rgb(100, 116, 139));
        input.setTextSize(18);
        input.setSingleLine(false);
        input.setMaxLines(2);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setPadding(dp(12), dp(8), dp(12), dp(8));
        input.setBackgroundColor(SURFACE_ALT);
        input.addTextChangedListener(new RecalcWatcher());
        parent.addView(input, blockParams());
        return input;
    }

    private void addBtcLot(String btcAmount, String acquiredDate) {
        LinearLayout row = row();
        row.setGravity(Gravity.CENTER_VERTICAL);
        EditText btc = smallInput("BTC held/bought", btcAmount);
        EditText date = smallInput("YYYY-MM-DD", acquiredDate);
        Button remove = button("-");
        BtcLot lot = new BtcLot(row, btc, date);
        remove.setOnClickListener(v -> {
            btcLots.remove(lot);
            btcLotList.removeView(row);
            if (btcLots.isEmpty()) {
                addBtcLot("", todayString());
            }
            persistInputs();
            recalculate();
        });
        row.addView(btc, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(date, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(remove);
        btcLots.add(lot);
        btcLotList.addView(row, blockParams());
    }

    private EditText smallInput(String hint, String value) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setText(value);
        input.setTextColor(TEXT);
        input.setHintTextColor(Color.rgb(100, 116, 139));
        input.setTextSize(14);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setPadding(dp(10), dp(8), dp(10), dp(8));
        input.setBackgroundColor(SURFACE_ALT);
        input.addTextChangedListener(new RecalcWatcher());
        return input;
    }

    private TextView addMetric(LinearLayout parent, String label, String value, String note) {
        LinearLayout card = card();
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.addView(text(label, 13, MUTED, Typeface.BOLD));
        TextView valueView = text(value, 24, TEXT, Typeface.BOLD);
        card.addView(valueView);
        card.addView(text(note, 12, MUTED, Typeface.NORMAL));
        LinearLayout.LayoutParams params;
        if (parent.getOrientation() == LinearLayout.HORIZONTAL) {
            params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            params.setMargins(0, 0, dp(8), 0);
        } else {
            params = blockParams();
        }
        parent.addView(card, params);
        return valueView;
    }

    private TextView addSummary(LinearLayout parent, String label, String value) {
        LinearLayout row = row();
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(text(label, 14, MUTED, Typeface.BOLD), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        TextView valueView = text(value, 15, TEXT, Typeface.BOLD);
        row.addView(valueView);
        parent.addView(row, blockParams());
        return valueView;
    }

    private void recalculate() {
        if (restoring) {
            return;
        }

        double monthlyIncome = read(monthlyIncomeInput);
        double monthlyExpenses = read(monthlyExpensesInput);
        double costOfLivingPercent = read(costOfLivingInput);
        double weeklyHours = read(weeklyHoursInput);
        double dailyHours = read(dailyHoursInput);
        double currentAge = read(ageInput);
        double btcPrice = read(btcPriceInput);
        double expenseYears = read(expenseYearsInput);
        double dcaAmount = read(dcaAmountInput);
        double cagrPercent = read(cagrInput);
        String capsuleName = fallback(value(timeCapsuleInput), "Time Capsule");
        String freedomWord = fallback(value(freedomTermInput), "Freedom");
        double btcHeld = totalBtcHeld();
        Date earliestAcquired = earliestAcquiredDate();

        double monthlyHours = weeklyHours * 52.0 / 12.0;
        double netIncome = monthlyIncome - monthlyExpenses;
        double hourlyWage = monthlyHours > 0 ? netIncome / monthlyHours : 0;
        double currentValue = btcHeld * btcPrice;
        double targetAmount = inflatedExpenseGoal(monthlyExpenses, expenseYears, costOfLivingPercent);
        double targetBtc = btcPrice > 0 ? targetAmount / btcPrice : 0;
        double progress = targetAmount > 0 ? currentValue / targetAmount : 0;
        double hoursSaved = hourlyWage > 0 ? currentValue / hourlyWage : 0;
        double daysSaved = dailyHours > 0 ? hoursSaved / dailyHours : 0;
        double monthsSaved = monthlyHours > 0 ? hoursSaved / monthlyHours : 0;
        double requiredPrice = btcHeld > 0 ? targetAmount / btcHeld : 0;
        double workedHoursSinceAcquired = workedHoursSince(earliestAcquired, weeklyHours);
        double efficiency = workedHoursSinceAcquired > 0 ? hoursSaved / workedHoursSinceAcquired : 0;
        Projection projection = projectFreedomDate(btcHeld, btcPrice, targetAmount, dcaAmount, dcaFrequencyInput.getSelectedItemPosition(), cagrPercent);
        double futurePrice = btcPrice * Math.pow(1.0 + cagrPercent / 100.0, projection.yearsFromNow);
        double targetFutureBtc = futurePrice > 0 ? targetAmount / futurePrice : 0;
        int chartSteps = Math.max(1, (int) Math.ceil((projection.hit ? projection.yearsFromNow : 20.0) * 12.0));
        double[][] comparisonSeries = buildComparisonSeries(currentValue, dcaAmount, dcaFrequencyInput.getSelectedItemPosition(), cagrPercent, chartSteps);

        heroTitle.setText(capsuleName);
        freedomCardTitle.setText(getString(R.string.freedom_clock_format, freedomWord));
        progressTitle.setText(getString(R.string.freedom_progress_format, freedomWord));
        efficiencyTitle.setText(getString(R.string.efficiency_format, capsuleName));
        hoursSavedValue.setText(getString(R.string.hours_saved_format, hoursSaved));
        heroSubtitle.setText(showBitcoinValue
                ? getString(R.string.hero_subtitle_bitcoin, money(currentValue))
                : getString(R.string.hero_subtitle_masked));
        freedomDateValue.setText(projection.label);
        freedomDateValue.setTextColor(projection.hit ? GREEN : RED);
        freedomAgeValue.setText(projection.hit && currentAge > 0
                ? getString(R.string.years_old_format, currentAge + projection.yearsFromNow)
                : getString(R.string.enter_current_age));
        daysSavedValue.setText(getString(R.string.days_saved_format, daysSaved));
        daysSavedValue.setTextColor(ORANGE);
        monthsSavedValue.setText(getString(R.string.months_saved_format, monthsSaved));
        monthsSavedValue.setTextColor(ORANGE);
        efficiencyValue.setText(getString(R.string.efficiency_value_format, efficiency * 100.0));
        efficiencyDetailValue.setText(earliestAcquired == null
                ? getString(R.string.add_acquired_date_hint)
                : getString(R.string.efficiency_detail_format, hoursSaved, workedHoursSinceAcquired, INPUT_DATE.format(earliestAcquired)));
        String progressAmounts = showProgressAmounts
                ? " "
                : "";
        progressValue.setText(getString(R.string.funded_format, progress * 100.0, progressAmounts));
        freedomProgress.setProgress((int) Math.min(1000, Math.max(0, Math.round(progress * 1000))));
        targetAmountValue.setText(showProgressAmounts ? money(targetAmount) : MASK);
        targetBtcValue.setText(showProgressAmounts ? getString(R.string.btc_format, targetBtc) : MASK);
        targetFutureBtcValue.setText(showProgressAmounts && projection.hit ? getString(R.string.btc_format, targetFutureBtc) : MASK);
        requiredPriceValue.setText(showProgressAmounts ? getString(R.string.required_price_format, money(requiredPrice)) : MASK);
        
        String btcValueStr = showBitcoinValue ? money(currentValue) : MASK;
        String btcHeldStr = showBitcoinValue ? getString(R.string.btc_format, btcHeld) : MASK;
        totalValueValue.setText(btcValueStr);
        if (totalValueValue.getParent() instanceof LinearLayout) {
            LinearLayout card = (LinearLayout) totalValueValue.getParent();
            if (card.getChildCount() > 2 && card.getChildAt(2) instanceof TextView) {
                ((TextView) card.getChildAt(2)).setText(btcHeldStr);
            }
        }

        hourlyWageValue.setText(getString(R.string.hourly_wage_format, money(hourlyWage)));
        netIncomeValue.setText(money(netIncome));
        comparisonChart.setSeries(comparisonSeries);
        comparisonSummaryValue.setText(getString(R.string.projection_summary_format,
                chartSteps / 12.0, frequencyLabel().toLowerCase(Locale.US), cagrPercent));
        updatedValue.setText(R.string.time_is_money);

        persistInputs();
    }

    private Projection projectFreedomDate(double btcHeld, double btcPrice, double targetAmount, double dcaAmount, int frequencyIndex, double cagrPercent) {
        if (targetAmount <= 0) {
            return new Projection(getString(R.string.set_a_goal), false, 0);
        }
        if (btcPrice > 0 && btcHeld * btcPrice >= targetAmount) {
            return new Projection(getString(R.string.today), true, 0);
        }
        if (btcPrice <= 0) {
            return new Projection(getString(R.string.enter_btc_price), false, 0);
        }

        int daysPerStep = frequencyDays(frequencyIndex);
        double currentBtc = Math.max(0, btcHeld);
        double price = btcPrice;
        double annualGrowth = Math.max(-0.999, cagrPercent / 100.0);
        Calendar start = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        int maxSteps = Math.max(1, (int) Math.ceil(36500.0 / daysPerStep));

        for (int step = 1; step <= maxSteps; step++) {
            price *= Math.pow(1.0 + annualGrowth, daysPerStep / 365.0);
            if (dcaAmount > 0) {
                currentBtc += dcaAmount / price;
            }
            date.add(Calendar.DAY_OF_YEAR, daysPerStep);
            if (currentBtc * price >= targetAmount) {
                double yearsFromNow = daysBetween(start.getTime(), date.getTime()) / 365.25;
                return new Projection(DISPLAY_DATE.format(date.getTime()), true, yearsFromNow);
            }
        }

        return new Projection(getString(R.string.beyond_100_years), false, 100);
    }

    private double[][] buildComparisonSeries(double startingValue, double dcaAmount, int frequencyIndex, double btcCagrPercent, int months) {
        double[] cagrs = {btcCagrPercent / 100.0, 0.12, 0.15, 0.07};
        double[][] series = new double[cagrs.length][months + 1];
        int dcaEventsPerMonth = dcaEventsPerMonth(frequencyIndex);
        for (int i = 0; i < cagrs.length; i++) {
            double value = Math.max(0, startingValue);
            series[i][0] = value;
            double monthlyGrowth = Math.pow(1.0 + Math.max(-0.999, cagrs[i]), 1.0 / 12.0);
            for (int month = 1; month <= months; month++) {
                value = value * monthlyGrowth + dcaAmount * dcaEventsPerMonth;
                series[i][month] = value;
            }
        }
        return series;
    }

    private double inflatedExpenseGoal(double monthlyExpenses, double years, double costOfLivingPercent) {
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

    private int dcaEventsPerMonth(int position) {
        if (position == 0) {
            return 30;
        }
        if (position == 2) {
            return 1;
        }
        return 4;
    }

    private void loadInputs() {
        restoring = true;
        monthlyIncomeInput.setText(prefs.getString("monthlyIncome", "4000"));
        monthlyExpensesInput.setText(prefs.getString("monthlyExpenses", "3000"));
        costOfLivingInput.setText(prefs.getString("costOfLiving", "3"));
        weeklyHoursInput.setText(prefs.getString("weeklyHours", "40"));
        dailyHoursInput.setText(prefs.getString("dailyHours", "8"));
        ageInput.setText(prefs.getString("age", "35"));
        btcPriceInput.setText(prefs.getString("btcPrice", "100000"));
        expenseYearsInput.setText(prefs.getString("expenseYears", "20"));
        dcaAmountInput.setText(prefs.getString("dcaAmount", "100"));
        cagrInput.setText(prefs.getString("cagr", "25"));
        timeCapsuleInput.setText(prefs.getString("timeCapsule", "Time Capsule"));
        freedomTermInput.setText(prefs.getString("freedomTerm", "Freedom"));
        dcaFrequencyInput.setSelection(prefs.getInt("dcaFrequency", 1));
        showBitcoinValue = prefs.getBoolean("showBitcoinValue", true);
        showProgressAmounts = prefs.getBoolean("showProgressAmounts", false);
        valueVisibilitySwitch.setChecked(showBitcoinValue);
        progressAmountsVisibilitySwitch.setChecked(showProgressAmounts);

        int count = Math.max(1, prefs.getInt("btcLotCount", 1));
        for (int i = 0; i < count; i++) {
            String defaultBtc = i == 0 ? prefs.getString("btcHeld", "0.05") : "";
            addBtcLot(prefs.getString("btcLotAmount" + i, defaultBtc), prefs.getString("btcLotDate" + i, todayString()));
        }
        restoring = false;
    }

    private void persistInputs() {
        if (restoring) {
            return;
        }
        SharedPreferences.Editor editor = prefs.edit()
                .putString("monthlyIncome", value(monthlyIncomeInput))
                .putString("monthlyExpenses", value(monthlyExpensesInput))
                .putString("costOfLiving", value(costOfLivingInput))
                .putString("weeklyHours", value(weeklyHoursInput))
                .putString("dailyHours", value(dailyHoursInput))
                .putString("age", value(ageInput))
                .putString("btcPrice", value(btcPriceInput))
                .putString("expenseYears", value(expenseYearsInput))
                .putString("dcaAmount", value(dcaAmountInput))
                .putString("cagr", value(cagrInput))
                .putString("timeCapsule", value(timeCapsuleInput))
                .putString("freedomTerm", value(freedomTermInput))
                .putInt("dcaFrequency", dcaFrequencyInput.getSelectedItemPosition())
                .putBoolean("showBitcoinValue", showBitcoinValue)
                .putBoolean("showProgressAmounts", showProgressAmounts)
                .putInt("btcLotCount", btcLots.size());
        for (int i = 0; i < btcLots.size(); i++) {
            editor.putString("btcLotAmount" + i, value(btcLots.get(i).btcAmount));
            editor.putString("btcLotDate" + i, value(btcLots.get(i).acquiredDate));
        }
        editor.apply();
    }

    private double totalBtcHeld() {
        double total = 0;
        for (BtcLot lot : btcLots) {
            total += read(lot.btcAmount);
        }
        return total;
    }

    private Date earliestAcquiredDate() {
        Date earliest = null;
        for (BtcLot lot : btcLots) {
            Date date = parseDate(value(lot.acquiredDate));
            if (date != null && (earliest == null || date.before(earliest))) {
                earliest = date;
            }
        }
        return earliest;
    }

    private double workedHoursSince(Date acquiredDate, double weeklyHours) {
        if (acquiredDate == null || weeklyHours <= 0) {
            return 0;
        }
        double days = Math.max(0, daysBetween(acquiredDate, new Date()));
        return (days / 7.0) * weeklyHours;
    }

    private Date parseDate(String value) {
        try {
            return INPUT_DATE.parse(value.trim());
        } catch (ParseException | NullPointerException ignored) {
            return null;
        }
    }

    private double daysBetween(Date start, Date end) {
        return (end.getTime() - start.getTime()) / 86400000.0;
    }

    private String frequencyLabel() {
        int position = dcaFrequencyInput == null ? 1 : dcaFrequencyInput.getSelectedItemPosition();
        if (position == 0) {
            return "Daily";
        }
        if (position == 2) {
            return "Monthly";
        }
        return "Weekly";
    }

    private int frequencyDays(int position) {
        if (position == 0) {
            return 1;
        }
        if (position == 2) {
            return 30;
        }
        return 7;
    }

    private String todayString() {
        return INPUT_DATE.format(new Date());
    }

    private String fallback(String value, String fallback) {
        String clean = value == null ? "" : value.trim();
        return clean.isEmpty() ? fallback : clean;
    }

    private String value(EditText input) {
        return input == null ? "" : input.getText().toString();
    }

    private double read(EditText input) {
        try {
            String value = value(input).trim().replace(",", "").replace(" ", "");
            return value.isEmpty() ? 0 : Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String money(double value) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(value);
    }

    private Button button(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(BG);
        button.setTextSize(13);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackgroundColor(ORANGE);
        return button;
    }

    private LinearLayout card() {
        LinearLayout card = column();
        card.setBackgroundColor(SURFACE);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        return card;
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout row() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        return layout;
    }

    private TextView text(String value, int sp, int color, int style) {
        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextColor(color);
        textView.setTextSize(sp);
        textView.setTypeface(Typeface.DEFAULT, style);
        textView.setLineSpacing(2, 1.05f);
        return textView;
    }

    private TextView wageBitTitle() {
        TextView title = text("WageBit", 28, TEXT, Typeface.BOLD);
        SpannableString styled = new SpannableString("WageBit");
        styled.setSpan(new ForegroundColorSpan(ORANGE), 4, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        title.setText(styled);
        return title;
    }

    private LinearLayout.LayoutParams blockParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(12), 0, 0);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private class RecalcWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            recalculate();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private static class ComparisonChartView extends View {
        private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path linePath = new Path();
        private double[][] series = new double[0][0];
        private final int[] colors = {ORANGE, GREEN, Color.rgb(96, 165, 250), Color.rgb(250, 204, 21)};

        ComparisonChartView(android.content.Context context) {
            super(context);
            setMinimumHeight(220);
            axisPaint.setColor(MUTED);
            axisPaint.setStrokeWidth(2f);
            axisPaint.setTextSize(28f);
            gridPaint.setColor(Color.rgb(51, 65, 85));
            gridPaint.setStrokeWidth(1f);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(5f);
        }

        void setSeries(double[][] series) {
            this.series = series == null ? new double[0][0] : series;
            invalidate();
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();
            int left = 18;
            int top = 18;
            int right = width - 18;
            int bottom = height - 32;
            canvas.drawColor(SURFACE_ALT);
            for (int i = 0; i <= 4; i++) {
                float y = top + (bottom - top) * i / 4f;
                canvas.drawLine(left, y, right, y, gridPaint);
            }
            canvas.drawLine(left, bottom, right, bottom, axisPaint);
            canvas.drawLine(left, top, left, bottom, axisPaint);

            if (series.length == 0 || series[0].length < 2) {
                canvas.drawText("Add inputs to plot growth.", left + 14, top + 44, axisPaint);
                return;
            }

            double max = 0;
            for (double[] line : series) {
                for (double value : line) {
                    max = Math.max(max, value);
                }
            }
            if (max <= 0) {
                max = 1;
            }

            for (int lineIndex = 0; lineIndex < series.length; lineIndex++) {
                double[] line = series[lineIndex];
                if (line.length < 2) {
                    continue;
                }
                linePath.reset();
                for (int i = 0; i < line.length; i++) {
                    float x = left + (right - left) * i / (float) (line.length - 1);
                    float y = bottom - (float) (line[i] / max) * (bottom - top);
                    if (i == 0) {
                        linePath.moveTo(x, y);
                    } else {
                        linePath.lineTo(x, y);
                    }
                }
                linePaint.setColor(colors[lineIndex % colors.length]);
                canvas.drawPath(linePath, linePaint);
            }
        }
    }

    private static class BtcLot {
        final LinearLayout container;
        final EditText btcAmount;
        final EditText acquiredDate;

        BtcLot(LinearLayout container, EditText btcAmount, EditText acquiredDate) {
            this.container = container;
            this.btcAmount = btcAmount;
            this.acquiredDate = acquiredDate;
        }
    }

    private static class Projection {
        final String label;
        final boolean hit;
        final double yearsFromNow;

        Projection(String label, boolean hit, double yearsFromNow) {
            this.label = label;
            this.hit = hit;
            this.yearsFromNow = yearsFromNow;
        }
    }
}
