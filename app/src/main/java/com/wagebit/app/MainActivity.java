package com.wagebit.app;

import androidx.annotation.NonNull;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
    private TextView freedomYearsValue;
    private TextView freedomAgeValue;
    private TextView daysSavedLabel;
    private TextView daysSavedValue;
    private TextView daysSavedNote;
    private TextView monthsSavedLabel;
    private TextView monthsSavedValue;
    private TextView monthsSavedNote;
    private TextView efficiencyTitle;
    private TextView efficiencyValue;
    private TextView efficiencyDetailValue;
    private TextView progressTitle;
    private TextView progressValue;
    private TextView futureBalanceTitle;
    private TextView futureBalanceValue;
    private TextView targetAmountValue;
    private TextView targetBtcValue;
    private TextView targetFutureBtcValue;
    private TextView requiredPriceValue;
    private TextView totalValueValue;
    private TextView totalValueLabel;
    private TextView hourlyWageValue;
    private TextView netIncomeValue;
    private TextView comparisonSummaryValue;
    private TextView updatedValue;
    private TextView btcPositionLabel;
    private TextView btcPriceLabel;
    private TextView btcCagrLabel;
    private TextView btcLotsLabel;
    private TextView btcLotsHint;
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
    private EditText coffeeAmountInput;
    private EditText coffeeDescriptionInput;
    private EditText timeCapsuleInput;
    private EditText freedomTermInput;
    private Spinner dcaFrequencyInput;
    private Spinner coffeeFrequencyInput;
    private TextView coffeeResultValue;
    private TextView coffeeCalculatorTitle;
    private Switch valueVisibilitySwitch;
    private Switch progressAmountsVisibilitySwitch;
    private CheckBox forbiddenUnlockCheckbox;
    private Switch forbiddenToggle;
    private EditText forbiddenInput;
    private LinearLayout forbiddenCard;
    private LinearLayout unlockContainer;
    private boolean restoring;
    private boolean showBitcoinValue = true;
    private boolean showProgressAmounts = true;
    private boolean forbiddenUnlocked = false;
    private boolean forbiddenEnabled = false;
    private String forbiddenPill = "BTC";
    private boolean unitWeeks = false;
    private boolean unitYears = false;
    private boolean unitFreedomDays = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
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
        scrollView.setFitsSystemWindows(true);

        LinearLayout root = column();
        root.setPadding(dp(18), dp(18), dp(18), dp(100));
        scrollView.addView(root);

        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout headerLeft = column();
        headerLeft.addView(wageBitTitle());
        header.addView(headerLeft, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        updatedValue = text(getString(R.string.time_is_money), 12, MUTED, Typeface.NORMAL);
        header.addView(updatedValue);
        root.addView(header);

        LinearLayout hero = card();
        hero.setPadding(dp(20), dp(18), dp(20), dp(18));
        heroTitle = text(getString(R.string.time_capsule), 14, ORANGE, Typeface.BOLD);
        hero.addView(heroTitle);
        hoursSavedValue = text(getString(R.string.zero_hours), 46, ORANGE, Typeface.BOLD);
        hero.addView(hoursSavedValue);
        heroSubtitle = text(getString(R.string.hero_subtitle_hint), 15, MUTED, Typeface.NORMAL);
        hero.addView(heroSubtitle);
        root.addView(hero, blockParams());

        LinearLayout freedomCard = card();
        freedomCardTitle = text(getString(R.string.freedom_clock), 20, TEXT, Typeface.BOLD);
        freedomCard.addView(freedomCardTitle);
        
        LinearLayout freedomDateRow = row();
        freedomDateRow.setGravity(Gravity.BOTTOM);
        freedomDateValue = text(getString(R.string.not_projected), 30, TEXT, Typeface.BOLD);
        freedomDateRow.addView(freedomDateValue, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        freedomYearsValue = text("", 18, TEXT, Typeface.NORMAL);
        freedomYearsValue.setGravity(Gravity.RIGHT);
        freedomYearsValue.setOnClickListener(v -> {
            unitFreedomDays = !unitFreedomDays;
            recalculate();
        });
        freedomDateRow.addView(freedomYearsValue);
        freedomCard.addView(freedomDateRow);
        
        freedomAgeValue = addSummary(freedomCard, getString(R.string.age), getString(R.string.enter_current_age));
        root.addView(freedomCard, blockParams());

        LinearLayout timeMetrics = row();
        daysSavedValue = addMetric(timeMetrics, getString(R.string.work_days_saved), getString(R.string.zero_days), getString(R.string.work_days_saved_note), true);
        daysSavedLabel = (TextView) ((LinearLayout) ((LinearLayout) daysSavedValue.getParent()).getChildAt(0)).getChildAt(0);
        daysSavedNote = (TextView) ((LinearLayout) daysSavedValue.getParent()).getChildAt(2);

        ((View) daysSavedValue.getParent()).setOnClickListener(v -> {
            unitWeeks = !unitWeeks;
            recalculate();
        });
        monthsSavedValue = addMetric(timeMetrics, getString(R.string.work_months_saved), getString(R.string.zero_months), getString(R.string.work_months_saved_note), true);
        monthsSavedLabel = (TextView) ((LinearLayout) ((LinearLayout) monthsSavedValue.getParent()).getChildAt(0)).getChildAt(0);
        monthsSavedNote = (TextView) ((LinearLayout) monthsSavedValue.getParent()).getChildAt(2);

        ((View) monthsSavedValue.getParent()).setOnClickListener(v -> {
            unitYears = !unitYears;
            recalculate();
        });
        root.addView(timeMetrics, blockParams());

        LinearLayout efficiencyCard = card();
        efficiencyTitle = text(getString(R.string.efficiency_format, ""), 20, TEXT, Typeface.BOLD);
        efficiencyCard.addView(efficiencyTitle);
        efficiencyValue = text(getString(R.string.zero_percent), 30, ORANGE, Typeface.BOLD);
        efficiencyCard.addView(efficiencyValue);
        efficiencyDetailValue = text(getString(R.string.add_acquired_date_hint), 13, MUTED, Typeface.NORMAL);
        efficiencyCard.addView(efficiencyDetailValue);
        root.addView(efficiencyCard, blockParams());

        LinearLayout progressCard = card();
        progressTitle = text(getString(R.string.freedom_progress), 20, TEXT, Typeface.BOLD);
        progressCard.addView(progressTitle);
        progressValue = text(getString(R.string.zero_percent), 28, ORANGE, Typeface.BOLD);
        progressCard.addView(progressValue);
        freedomProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        freedomProgress.setMax(1000);
        freedomProgress.setProgressTintList(ColorStateList.valueOf(ORANGE));
        progressCard.addView(freedomProgress, blockParams());
        targetAmountValue = addSummary(progressCard, getString(R.string.goal_amount), getString(R.string.zero_money));
        targetBtcValue = addSummary(progressCard, getString(R.string.btc_goal_current), getString(R.string.zero_btc));
        targetFutureBtcValue = addSummary(progressCard, getString(R.string.btc_goal_future), getString(R.string.zero_btc));
        requiredPriceValue = addSummary(progressCard, getString(R.string.btc_price_needed_now), getString(R.string.zero_money));
        root.addView(progressCard, blockParams());

        LinearLayout balanceCard = card();
        futureBalanceTitle = text(getString(R.string.future_balance), 20, TEXT, Typeface.BOLD);
        balanceCard.addView(futureBalanceTitle);
        futureBalanceValue = text("", 15, TEXT, Typeface.BOLD);
        balanceCard.addView(futureBalanceValue, blockParams());
        root.addView(balanceCard, blockParams());

        LinearLayout comparisonContent = column();
        comparisonChart = new ComparisonChartView(this);
        comparisonContent.addView(comparisonChart, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(220)));
        comparisonSummaryValue = text(getString(R.string.projected_values_note), 13, MUTED, Typeface.NORMAL);
        comparisonContent.addView(comparisonSummaryValue, blockParams());
        comparisonContent.addView(legendRow("BTC", ORANGE, "S&P 500", GREEN), blockParams());
        comparisonContent.addView(legendRow("Nasdaq-100", Color.rgb(96, 165, 250), "Gold", Color.rgb(250, 204, 21)), blockParams());
        root.addView(expandableCard(getString(R.string.growth_comparison), comparisonContent, false), blockParams());

        LinearLayout metrics = column();
        root.addView(metrics, blockParams());
        totalValueValue = addMetric(metrics, getString(R.string.total_bitcoin_position), getString(R.string.zero_money), getString(R.string.zero_btc));
        totalValueLabel = (TextView) ((LinearLayout) ((LinearLayout) totalValueValue.getParent()).getChildAt(0)).getChildAt(0);
        hourlyWageValue = addMetric(metrics, getString(R.string.real_hourly_wage), getString(R.string.zero_money), getString(R.string.real_hourly_wage_note));
        netIncomeValue = addMetric(metrics, getString(R.string.monthly_net_income), getString(R.string.zero_money), getString(R.string.monthly_net_income_note));

        LinearLayout workContent = column();
        monthlyIncomeInput = addInput(workContent, getString(R.string.monthly_income), "4000", getString(R.string.after_taxes));
        monthlyExpensesInput = addInput(workContent, getString(R.string.monthly_expenses), "3000", getString(R.string.fixed_needs_note));
        costOfLivingInput = addInput(workContent, getString(R.string.cost_of_living_increase), "3", getString(R.string.represents_inflation));
        weeklyHoursInput = addInput(workContent, getString(R.string.weekly_hours_worked), "40", getString(R.string.include_commute_note));
        dailyHoursInput = addInput(workContent, getString(R.string.daily_hours_worked), "8", getString(R.string.calculate_work_days));
        ageInput = addInput(workContent, getString(R.string.current_age), "35", getString(R.string.estimate_age_note));
        root.addView(expandableCard(getString(R.string.labor_profile), workContent), blockParams());

        LinearLayout bitcoinContent = column();
        btcPriceInput = addInput(bitcoinContent, getString(R.string.current_btc_price), "100000", null);
        btcPriceLabel = (TextView) bitcoinContent.getChildAt(0);
        expenseYearsInput = addInput(bitcoinContent, getString(R.string.years_of_expenses_wanted), "20", null);
        dcaAmountInput = addInput(bitcoinContent, getString(R.string.dca_buy_amount), "100", "Dollar-cost averaging: buying a set amount on a regular schedule.");
        bitcoinContent.addView(text(getString(R.string.dca_frequency), 13, MUTED, Typeface.BOLD));
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
        cagrInput = addInput(bitcoinContent, getString(R.string.btc_cagr), "25", "Compound annual growth rate. S&P 500 Approx. 10-Yr CAGR ~12%, Nasdaq-100 ~15%, Gold ~7%, BTC > 50%");
        btcCagrLabel = (TextView) bitcoinContent.getChildAt(bitcoinContent.indexOfChild(cagrInput) - 2);

        LinearLayout lotHeader = row();
        lotHeader.setGravity(Gravity.CENTER_VERTICAL);
        btcLotsLabel = text(getString(R.string.btc_lots), 18, TEXT, Typeface.BOLD);
        lotHeader.addView(btcLotsLabel, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        Button addLotButton = button(getString(R.string.add_lot));
        addLotButton.setOnClickListener(v -> {
            addBtcLot("", todayString());
            persistInputs();
            recalculate();
        });
        lotHeader.addView(addLotButton);
        bitcoinContent.addView(lotHeader, blockParams());
        btcLotsHint = text(getString(R.string.btc_lots_hint), 13, MUTED, Typeface.NORMAL);
        bitcoinContent.addView(btcLotsHint);
        btcLotList = column();
        bitcoinContent.addView(btcLotList);

        coffeeCalculatorTitle = text(getString(R.string.expense_calculator_title_format, "Coffee"), 18, TEXT, Typeface.BOLD);
        bitcoinContent.addView(coffeeCalculatorTitle, blockParams());
        coffeeAmountInput = addInput(bitcoinContent, getString(R.string.regular_expense), "35", getString(R.string.expense_compounding_note));
        
        bitcoinContent.addView(text(getString(R.string.expense_frequency), 13, MUTED, Typeface.BOLD));
        coffeeFrequencyInput = new Spinner(this);
        ArrayAdapter<String> coffeeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Daily", "Weekly", "Monthly"});
        coffeeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        coffeeFrequencyInput.setAdapter(coffeeAdapter);
        coffeeFrequencyInput.setBackgroundColor(SURFACE_ALT);
        coffeeFrequencyInput.setPadding(dp(8), dp(6), dp(8), dp(6));
        coffeeFrequencyInput.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                recalculate();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        bitcoinContent.addView(coffeeFrequencyInput, blockParams());

        coffeeResultValue = text("", 15, TEXT, Typeface.BOLD);
        bitcoinContent.addView(coffeeResultValue, blockParams());

        LinearLayout btcExp = expandableCard(getString(R.string.bitcoin_position_label), bitcoinContent);
        btcPositionLabel = (TextView) ((LinearLayout)((LinearLayout)btcExp.getChildAt(0))).getChildAt(0);
        root.addView(btcExp, blockParams());


        LinearLayout settingsContent = column();
        timeCapsuleInput = addInput(settingsContent, getString(R.string.vault_name), "Time Capsule", null);
        freedomTermInput = addInput(settingsContent, getString(R.string.freedom_word), "Freedom", getString(R.string.freedom_word_note));
        coffeeDescriptionInput = addInput(settingsContent, getString(R.string.compounding_expense_label), "Coffee", getString(R.string.compounding_expense_label_note));
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

        LinearLayout settingsCard = expandableCard(getString(R.string.settings), settingsContent, false);
        root.addView(settingsCard, blockParams());

        forbiddenCard = card();
        LinearLayout forbiddenContent = column();
        forbiddenToggle = new Switch(this);
        forbiddenToggle.setText(R.string.forbidden_pill_label);
        forbiddenToggle.setTextColor(TEXT);
        forbiddenToggle.setTextSize(14);
        forbiddenToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            forbiddenEnabled = isChecked;
            forbiddenInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                forbiddenUnlocked = false;
                forbiddenCard.setVisibility(View.GONE);
            }
            persistInputs();
            recalculate();
        });
        forbiddenContent.addView(forbiddenToggle, blockParams());
        forbiddenInput = addInput(forbiddenContent, getString(R.string.replacement_label), getString(R.string.replacement_label_hint), getString(R.string.replaces_btc_note));
        forbiddenInput.setVisibility(View.GONE);
        forbiddenCard.addView(forbiddenContent);
        forbiddenCard.setVisibility(View.GONE);
        root.addView(forbiddenCard, blockParams());

        TextView disclaimer = text(getString(R.string.disclaimer_text), 12, MUTED, Typeface.NORMAL);
        disclaimer.setGravity(Gravity.CENTER);
        root.addView(disclaimer, blockParams());

        TextView donation = text(getString(R.string.donation_address), 12, MUTED, Typeface.NORMAL);
        String donationText = donation.getText().toString();
        SpannableString donationStyled = new SpannableString(donationText);
        int btcIndex = donationText.indexOf("bc1q");
        if (btcIndex != -1) {
            donationStyled.setSpan(new UnderlineSpan(), btcIndex, donationText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            donationStyled.setSpan(new UnderlineSpan(), 0, donationStyled.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        donation.setText(donationStyled);
        donation.setGravity(Gravity.CENTER);
        donation.setClickable(true);
        donation.setFocusable(true);
        donation.setOnClickListener(v -> {
            String fullText = getString(R.string.donation_address);
            String addr = fullText;
            int btcStart = fullText.indexOf("bc1q");
            if (btcStart != -1) {
                addr = fullText.substring(btcStart);
            }
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Donation Address", addr));
            Toast.makeText(this, R.string.copied_toast, Toast.LENGTH_SHORT).show();

            unlockContainer.setVisibility(View.VISIBLE);
            
            View current = unlockContainer;
            while (current != null && !(current instanceof ScrollView)) {
                current = (View) current.getParent();
            }
            if (current instanceof ScrollView) {
                ScrollView finalSv = (ScrollView) current;
                unlockContainer.post(() -> finalSv.smoothScrollTo(0, unlockContainer.getBottom() + dp(100)));
            }
        });
        root.addView(donation, blockParams());

        unlockContainer = column();
        unlockContainer.setPadding(dp(20), dp(10), dp(20), dp(10));
        unlockContainer.addView(text(getString(R.string.unlock_description), 14, TEXT, Typeface.NORMAL));
        forbiddenUnlockCheckbox = new CheckBox(this);
        forbiddenUnlockCheckbox.setText(R.string.unlock_checkbox);
        forbiddenUnlockCheckbox.setTextColor(TEXT);
        forbiddenUnlockCheckbox.setButtonTintList(ColorStateList.valueOf(ORANGE));
        forbiddenUnlockCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            forbiddenUnlocked = isChecked;
            forbiddenCard.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                forbiddenEnabled = false;
                forbiddenToggle.setChecked(false);
                forbiddenInput.setVisibility(View.GONE);
            }
            persistInputs();
            recalculate();
            if (isChecked) {
                View current = forbiddenCard;
                while (current != null && !(current instanceof ScrollView)) {
                    current = (View) current.getParent();
                }
                if (current instanceof ScrollView) {
                    ScrollView finalSv = (ScrollView) current;
                    forbiddenCard.post(() -> finalSv.smoothScrollTo(0, forbiddenCard.getBottom() + dp(100)));
                }
            }
        });
        unlockContainer.addView(forbiddenUnlockCheckbox, blockParams());
        root.addView(unlockContainer);
        unlockContainer.setVisibility(View.GONE);

        TextView disclaimer2 = text(getString(R.string.market_disclaimer), 12, MUTED, Typeface.NORMAL);
        disclaimer2.setGravity(Gravity.CENTER);
        root.addView(disclaimer2, blockParams());

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
        View.OnClickListener listener = v -> {
            boolean show = content.getVisibility() != View.VISIBLE;
            content.setVisibility(show ? View.VISIBLE : View.GONE);
            toggle.setText(show ? "v" : ">");
        };
        header.setOnClickListener(listener);
        toggle.setOnClickListener(listener);
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
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.postDelayed(() -> {
                    // Find the ScrollView
                    View current = v;
                    while (current != null && !(current instanceof ScrollView)) {
                        current = (View) current.getParent();
                    }
                    if (current instanceof ScrollView) {
                        ScrollView sv = (ScrollView) current;
                        int[] location = new int[2];
                        v.getLocationInWindow(location);
                        int[] svLocation = new int[2];
                        sv.getLocationInWindow(svLocation);
                        int relativeTop = location[1] - svLocation[1] + sv.getScrollY();
                        sv.smoothScrollTo(0, relativeTop - dp(40));
                    }
                }, 500);
            }
        });
        parent.addView(input, blockParams());
        return input;
    }

    private void addBtcLot(String btcAmount, String acquiredDate) {
        LinearLayout row = row();
        row.setGravity(Gravity.CENTER_VERTICAL);
        EditText btc = smallInput(getString(R.string.btc_amount_hint), btcAmount);
        EditText date = smallInput(getString(R.string.date_hint), acquiredDate);
        Button remove = button("-");
        BtcLot lot = new BtcLot(btc, date);
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
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.postDelayed(() -> {
                    View current = v;
                    while (current != null && !(current instanceof ScrollView)) {
                        current = (View) current.getParent();
                    }
                    if (current instanceof ScrollView) {
                        ScrollView sv = (ScrollView) current;
                        int[] location = new int[2];
                        v.getLocationInWindow(location);
                        int[] svLocation = new int[2];
                        sv.getLocationInWindow(svLocation);
                        int relativeTop = location[1] - svLocation[1] + sv.getScrollY();
                        sv.smoothScrollTo(0, relativeTop - dp(40));
                    }
                }, 500);
            }
        });
        return input;
    }

    private TextView addMetric(LinearLayout parent, String label, String value, String note) {
        return addMetric(parent, label, value, note, false);
    }

    private TextView addMetric(LinearLayout parent, String label, String value, String note, boolean tappable) {
        LinearLayout card = card();
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        
        LinearLayout header = row();
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(text(label, 13, MUTED, Typeface.BOLD), new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        if (tappable) {
            header.addView(text("↻", 14, ORANGE, Typeface.BOLD));
        }
        card.addView(header);

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
        double coffeeAmount = read(coffeeAmountInput);
        String coffeeDescription = fallback(value(coffeeDescriptionInput), "Coffee");

        double monthlyHours = weeklyHours * 52.0 / 12.0;
        double netIncome = monthlyIncome - monthlyExpenses;
        double hourlyWage = monthlyHours > 0 ? netIncome / monthlyHours : 0;
        double currentValue = btcHeld * btcPrice;
        double targetAmount = Projector.calculateInflatedGoal(monthlyExpenses, expenseYears, costOfLivingPercent);
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
        freedomCardTitle.setText(p(getString(R.string.freedom_clock_format, freedomWord)));
        progressTitle.setText(p(getString(R.string.freedom_progress_format, freedomWord)));
        futureBalanceTitle.setText(p(getString(R.string.future_balance_title_format, freedomWord)));
        efficiencyTitle.setText(p(getString(R.string.efficiency_format, capsuleName)));
        hoursSavedValue.setText(getString(R.string.hours_saved_format, hoursSaved));
        heroSubtitle.setText(showBitcoinValue
                ? p(getString(R.string.hero_subtitle_bitcoin, String.format(Locale.US, "%,.8f", btcHeld) + " " + btcUnit()))
                : p(getString(R.string.hero_subtitle_masked)));
        freedomDateValue.setText(projection.label);
        freedomDateValue.setTextColor(projection.hit ? GREEN : RED);
        if (projection.hit && projection.yearsFromNow > 0) {
            String timeText;
            if (unitFreedomDays) {
                timeText = String.format(Locale.US, "%,.0f days", projection.yearsFromNow * 365.25);
            } else {
                timeText = String.format(Locale.US, "%.1f yrs", projection.yearsFromNow);
            }
            freedomYearsValue.setText(timeText);
            freedomYearsValue.setVisibility(View.VISIBLE);
        } else {
            freedomYearsValue.setVisibility(View.GONE);
        }
        freedomAgeValue.setText(projection.hit && currentAge > 0
                ? getString(R.string.years_old_format, currentAge + projection.yearsFromNow)
                : getString(R.string.enter_current_age));
        
        if (unitWeeks) {
            daysSavedLabel.setText(p(getString(R.string.work_weeks_saved)));
            daysSavedValue.setText(getString(R.string.weeks_saved_format, weeklyHours > 0 ? hoursSaved / weeklyHours : 0));
            daysSavedNote.setText(p(getString(R.string.work_weeks_saved_note)));
        } else {
            daysSavedLabel.setText(p(getString(R.string.work_days_saved)));
            daysSavedValue.setText(getString(R.string.days_saved_format, daysSaved));
            daysSavedNote.setText(p(getString(R.string.work_days_saved_note)));
        }
        daysSavedValue.setTextColor(ORANGE);

        if (unitYears) {
            monthsSavedLabel.setText(p(getString(R.string.work_years_saved)));
            monthsSavedValue.setText(getString(R.string.years_saved_format, (weeklyHours * 52.0) > 0 ? hoursSaved / (weeklyHours * 52.0) : 0));
            monthsSavedNote.setText(p(getString(R.string.work_years_saved_note)));
        } else {
            monthsSavedLabel.setText(p(getString(R.string.work_months_saved)));
            monthsSavedValue.setText(getString(R.string.months_saved_format, monthsSaved));
            monthsSavedNote.setText(p(getString(R.string.work_months_saved_note)));
        }
        monthsSavedValue.setTextColor(ORANGE);

        efficiencyValue.setText(getString(R.string.efficiency_value_format, efficiency * 100.0));
        efficiencyDetailValue.setText(earliestAcquired == null
                ? p(getString(R.string.add_acquired_date_hint))
                : p(getString(R.string.efficiency_detail_format, hoursSaved, workedHoursSinceAcquired, INPUT_DATE.format(earliestAcquired))));
        String progressAmounts = showProgressAmounts
                ? " "
                : "";
        progressValue.setText(getString(R.string.funded_format, progress * 100.0, progressAmounts));
        freedomProgress.setProgress((int) Math.min(1000, Math.max(0, Math.round(progress * 1000))));
        targetAmountValue.setText(showProgressAmounts ? money(targetAmount) : MASK);
        targetBtcValue.setText(showProgressAmounts ? p(getString(R.string.btc_format, targetBtc)) : MASK);
        targetFutureBtcValue.setText(showProgressAmounts && projection.hit ? p(getString(R.string.btc_format, targetFutureBtc)) : MASK);
        requiredPriceValue.setText(showProgressAmounts ? p(getString(R.string.required_price_format, money(requiredPrice))) : MASK);
        
        String btcValueStr = showBitcoinValue ? money(currentValue) : MASK;
        String btcHeldStr = showBitcoinValue ? p(getString(R.string.btc_format, btcHeld)) : MASK;
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
        comparisonSummaryValue.setText(p(getString(R.string.projection_summary_format,
                chartSteps / 12.0, frequencyLabel().toLowerCase(Locale.US), cagrPercent)));
        
        totalValueLabel.setText(p(getString(R.string.total_bitcoin_position)));
        btcPriceLabel.setText(p(getString(R.string.current_btc_price)));
        btcCagrLabel.setText(p(getString(R.string.btc_cagr)));
        btcLotsLabel.setText(p(getString(R.string.btc_lots)));
        btcLotsHint.setText(p(getString(R.string.btc_lots_hint)));
        btcPositionLabel.setText(p(getString(R.string.bitcoin_position_label)));

        coffeeCalculatorTitle.setText(p(getString(R.string.expense_calculator_title_format, coffeeDescription)));
        if (projection.hit && projection.yearsFromNow > 0 && hourlyWage > 0 && coffeeAmount > 0) {
            double annualRate = cagrPercent / 100.0;
            int coffeeFreqIdx = coffeeFrequencyInput.getSelectedItemPosition();
            double stepsPerYear = coffeeFreqIdx == 0 ? 365.0 : (coffeeFreqIdx == 1 ? 52.0 : 12.0);
            double stepRate = Math.pow(1.0 + annualRate, 1.0 / stepsPerYear) - 1.0;
            double totalSteps = projection.yearsFromNow * stepsPerYear;
            double fv;
            if (stepRate > 0) {
                fv = coffeeAmount * (Math.pow(1.0 + stepRate, totalSteps) - 1.0) / stepRate;
            } else {
                fv = coffeeAmount * totalSteps;
            }
            double coffeeHoursSaved = fv / hourlyWage;
            double coffeeBtcAmount = fv / futurePrice;
            String coffeeResultText = getString(R.string.compounded_expense_format, coffeeDescription, coffeeHoursSaved, coffeeBtcAmount, projection.label, btcUnit());
            coffeeResultValue.setText(orangeLaborHours(coffeeResultText));
            coffeeResultValue.setVisibility(View.VISIBLE);
        } else {
            coffeeResultValue.setVisibility(View.GONE);
        }

        if (projection.hit && projection.yearsFromNow > 0 && hourlyWage > 0) {
            double annualRate = cagrPercent / 100.0;
            int dcaFreqIdx = dcaFrequencyInput.getSelectedItemPosition();
            double stepsPerYear = dcaFreqIdx == 0 ? 365.0 : (dcaFreqIdx == 1 ? 52.0 : 12.0);
            double stepRate = Math.pow(1.0 + annualRate, 1.0 / stepsPerYear) - 1.0;
            double totalSteps = projection.yearsFromNow * stepsPerYear;
            double fvDca;
            if (stepRate > 0) {
                fvDca = dcaAmount * (Math.pow(1.0 + stepRate, totalSteps) - 1.0) / stepRate;
            } else {
                fvDca = dcaAmount * totalSteps;
            }
            double dcaBtcValueAtFreedom = fvDca + (currentValue * Math.pow(1.0 + annualRate, projection.yearsFromNow));
            double dcaHoursSaved = dcaBtcValueAtFreedom / hourlyWage;
            double dcaBtcAmount = dcaBtcValueAtFreedom / futurePrice;
            String futureBalanceText = getString(R.string.future_balance_format, freedomWord, dcaHoursSaved, dcaBtcAmount, btcUnit());
            futureBalanceValue.setText(orangeLaborHours(futureBalanceText));
            futureBalanceValue.setVisibility(View.VISIBLE);
        } else {
            futureBalanceValue.setVisibility(View.GONE);
        }

        updatedValue.setText(R.string.time_is_money);

        persistInputs();
    }

    private SpannableString orangeLaborHours(String fullText) {
        SpannableString ss = new SpannableString(fullText);
        String unit = getString(R.string.labor_hours_unit);
        int unitIndex = fullText.indexOf(unit);
        if (unitIndex != -1) {
            int end = unitIndex + unit.length();
            // Look backwards for the start of the number (digit or punctuation)
            int start = unitIndex - 1;
            while (start >= 0 && (Character.isDigit(fullText.charAt(start)) || fullText.charAt(start) == ',' || fullText.charAt(start) == '.' || fullText.charAt(start) == ' ')) {
                start--;
            }
            start++; // move back to the first digit/char of number
            if (start < unitIndex) {
                ss.setSpan(new ForegroundColorSpan(ORANGE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return ss;
    }

    private String btcUnit() {
        return forbiddenEnabled ? fallback(value(forbiddenInput), "BTC") : getString(R.string.btc_unit);
    }

    private String bitcoinUnit() {
        return forbiddenEnabled ? fallback(value(forbiddenInput), "Bitcoin") : getString(R.string.bitcoin_unit);
    }

    private String p(String original) {
        if (!forbiddenEnabled) return original;
        String replacement = fallback(value(forbiddenInput), "BTC");
        return original.replace("BTC", replacement).replace("Bitcoin", replacement).replace("bitcoin", replacement).replace("btc", replacement);
    }

    private Projection projectFreedomDate(double btcHeld, double btcPrice, double targetAmount, double dcaAmount, int frequencyIndex, double cagrPercent) {
        Projector.Result res = Projector.project(btcHeld, btcPrice, targetAmount, dcaAmount, frequencyIndex, cagrPercent,
                getString(R.string.set_a_goal), getString(R.string.today), getString(R.string.enter_btc_price), getString(R.string.beyond_100_years));
        return new Projection(res.label, res.hit, res.yearsFromNow);
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
        coffeeAmountInput.setText(prefs.getString("coffeeAmount", "35"));
        coffeeDescriptionInput.setText(prefs.getString("coffeeDescription", "Coffee"));
        timeCapsuleInput.setText(prefs.getString("timeCapsule", "Time Capsule"));
        freedomTermInput.setText(prefs.getString("freedomTerm", "Freedom"));
        dcaFrequencyInput.setSelection(prefs.getInt("dcaFrequency", 1));
        coffeeFrequencyInput.setSelection(prefs.getInt("coffeeFrequency", 1));
        showBitcoinValue = prefs.getBoolean("showBitcoinValue", true);
        showProgressAmounts = prefs.getBoolean("showProgressAmounts", false);
        forbiddenUnlocked = prefs.getBoolean("forbiddenUnlocked", false);
        forbiddenEnabled = prefs.getBoolean("forbiddenEnabled", false);
        forbiddenPill = prefs.getString("forbiddenPill", "BTC");
        valueVisibilitySwitch.setChecked(showBitcoinValue);
        progressAmountsVisibilitySwitch.setChecked(showProgressAmounts);
        forbiddenUnlockCheckbox.setChecked(forbiddenUnlocked);
        forbiddenToggle.setChecked(forbiddenEnabled);
        forbiddenInput.setText(forbiddenPill);
        unlockContainer.setVisibility(View.GONE);
        forbiddenCard.setVisibility(View.GONE);
        forbiddenInput.setVisibility(forbiddenEnabled ? View.VISIBLE : View.GONE);

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
                .putString("coffeeAmount", value(coffeeAmountInput))
                .putString("coffeeDescription", value(coffeeDescriptionInput))
                .putString("timeCapsule", value(timeCapsuleInput))
                .putString("freedomTerm", value(freedomTermInput))
                .putInt("dcaFrequency", dcaFrequencyInput.getSelectedItemPosition())
                .putInt("coffeeFrequency", coffeeFrequencyInput.getSelectedItemPosition())
                .putBoolean("showBitcoinValue", showBitcoinValue)
                .putBoolean("showProgressAmounts", showProgressAmounts)
                .putBoolean("forbiddenEnabled", forbiddenEnabled)
                .putString("forbiddenPill", value(forbiddenInput))
                .putInt("btcLotCount", btcLots.size());
        for (int i = 0; i < btcLots.size(); i++) {
            editor.putString("btcLotAmount" + i, value(btcLots.get(i).btcAmount));
            editor.putString("btcLotDate" + i, value(btcLots.get(i).acquiredDate));
        }
        editor.apply();

        // Notify widget
        Intent intent = new Intent(this, FreedomWidgetProvider.class);
        intent.setAction(FreedomWidgetProvider.ACTION_UPDATE);
        sendBroadcast(intent);
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
            canvas.drawLine(left, top, left, bottom, axisPaint);

            if (series.length == 0 || series[0].length < 2) {
                canvas.drawText(getContext().getString(R.string.chart_hint), left + 14, top + 44, axisPaint);
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
        final EditText btcAmount;
        final EditText acquiredDate;

        BtcLot(EditText btcAmount, EditText acquiredDate) {
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
