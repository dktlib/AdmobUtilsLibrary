package com.vapp.admoblibrary.rate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialog;
import androidx.core.content.ContextCompat;

import com.vapp.admoblibrary.R;
import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.RotationRatingBar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ahulr on 24-10-2016.
 */

public class RatingDialog extends AppCompatDialog implements RatingBar.OnRatingBarChangeListener, View.OnClickListener {

    private static final String SESSION_COUNT = "session_count";
    private static final String RATE5 = "rate_5";
    private static final String DATE_FIRST = "date_count";
    private static final String SHOW_NEVER = "show_never";
    private String MyPrefs = "RatingDialog";
    private SharedPreferences sharedpreferences;
    private Activity context;
    private Builder builder;
    private TextView tvTitle,tvContent , tvNegative, tvPositive;
    private RotationRatingBar ratingBar;
    private ImageView ivIcon;
    private LinearLayout ratingButtons;
    Button btnRate;
    TextView btnLate;

    private float threshold;
    private int session;
    private int date;
    private boolean thresholdPassed = true;
    public int starnumber = 0;

    public RatingDialog(Activity context, Builder builder) {
        super(context,R.style.AppCompatAlertDialogStyle);
        this.context = context;
        this.builder = builder;

        this.session = builder.session;
        this.threshold = builder.threshold;
        this.date = builder.date;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedpreferences = context.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_rating);

        tvTitle = (TextView) findViewById(R.id.dialog_rating_title);
        tvContent = (TextView) findViewById(R.id.dialog_rating_content);
        tvNegative = (TextView) findViewById(R.id.dialog_rating_button_negative);
        tvPositive = (TextView) findViewById(R.id.dialog_rating_button_positive);
        ratingBar = (RotationRatingBar) findViewById(R.id.dialog_rating_rating_bar);
        ivIcon = (ImageView) findViewById(R.id.dialog_rating_icon);
        ratingButtons = (LinearLayout) findViewById(R.id.dialog_rating_buttons);
        btnRate = findViewById(R.id.btnRate);
        btnLate = findViewById(R.id.btnLate);
        init();
    }

    private void init() {

        GradientDrawable drawable = (GradientDrawable) context.getResources()
                .getDrawable(R.drawable.bg_button);
        drawable.mutate();
        drawable.setColor(builder.ratingBarBackgroundColor);
        btnRate.setBackground(drawable);

        btnRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        if (builder.isShowLate){
            btnLate.setVisibility(View.VISIBLE);
        }else {
            btnLate.setVisibility(View.GONE);
        }

        if (builder.btnLate == null || builder.btnLate.equals("")){
            btnLate.setText("Maybe Later");
        }else {
            btnLate.setText(builder.btnLate);
        }

        btnLate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               builder.maybeLaterCallback.onClick();
               if (builder.isDismiss){
                   dismiss();
               }
            }
        });

        btnRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if (starnumber <= 3){
                  dismiss();
                  IAReview.getInstance().openDialogFeedback(context, builder.appName, builder.logo, builder.email, starnumber);
              }else {
                  dismiss();
                  sharedpreferences = context.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE);
                  if (sharedpreferences.getBoolean(RATE5, true)) {
                      sharedpreferences.edit().putBoolean(RATE5, false).commit();
                      IAReview.getInstance().showIAReview(context);
                  }else {
                      final String appPackageName = context.getPackageName(); // getPackageName() from Context or Activity object
                      try {
                          context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                      } catch (android.content.ActivityNotFoundException anfe) {
                          context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                      }
                  }


              }
            }
        });
        tvTitle.setText(builder.title);
        tvContent.setText("We’d greatly appreciate if you can rate us.");
        tvPositive.setText(builder.positiveText);
        tvNegative.setText(builder.negativeText);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
        int color = typedValue.data;

//        tvTitle.setTextColor(builder.titleTextColor != 0 ? ContextCompat.getColor(context, builder.titleTextColor) : ContextCompat.getColor(context, R.color.black));
        tvPositive.setTextColor(builder.positiveTextColor != 0 ? ContextCompat.getColor(context, builder.positiveTextColor) : color);


        if (builder.positiveBackgroundColor != 0) {
            tvPositive.setBackgroundResource(builder.positiveBackgroundColor);

        }
        if (builder.negativeBackgroundColor != 0) {
            tvNegative.setBackgroundResource(builder.negativeBackgroundColor);
        }

//        if (builder.ratingBarColor != 0) {
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
//                LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
//                stars.getDrawable(2).setColorFilter(ContextCompat.getColor(context, builder.ratingBarColor), PorterDuff.Mode.SRC_ATOP);
//                stars.getDrawable(1).setColorFilter(ContextCompat.getColor(context, builder.ratingBarColor), PorterDuff.Mode.SRC_ATOP);
//                int ratingBarBackgroundColor = builder.ratingBarBackgroundColor != 0 ? builder.ratingBarBackgroundColor : R.color.grey_200;
//                stars.getDrawable(0).setColorFilter(ContextCompat.getColor(context, ratingBarBackgroundColor), PorterDuff.Mode.SRC_ATOP);
//            } else {
//                Drawable stars = ratingBar.getProgressDrawable();
//                DrawableCompat.setTint(stars, ContextCompat.getColor(context, builder.ratingBarColor));
//            }
//        }

        Drawable d = context.getPackageManager().getApplicationIcon(context.getApplicationInfo());
      //  ivIcon.setImageDrawable(builder.drawable != null ? builder.drawable : d);

        ratingBar.setOnRatingChangeListener(new BaseRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChange(BaseRatingBar ratingBar, float rating, boolean fromUser) {
                int b = Math.round(rating);
                starnumber = b;
                switch (b){
                    case 0:
                        tvTitle.setText("We are working hard for a better user eperience.");
                        tvContent.setText("We’d greatly appreciate if you can rate us.");
                        ivIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.rate_0));
                        break;
                    case 1:
                        tvTitle.setText("Oh, no!");
                        tvContent.setText("Please leave us some feedback");
                        ivIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.rate_1));
                        break;

                    case 2:
                        tvTitle.setText("Oh, no!");
                        tvContent.setText("Please leave us some feedback");
                        ivIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.rate_2));
                        break;

                    case 3:
                        tvTitle.setText("Oh, no!");
                        tvContent.setText("Please leave us some feedback");
                        ivIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.rate_3));
                        break;
                    case 4:
                        tvTitle.setText("We like you too!");
                        tvContent.setText("Thank for your feedback.");
                        ivIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.rate_4));
                        break;
                    case 5:
                        tvTitle.setText("We like you too!");
                        tvContent.setText("Thank for your feedback.");
                        ivIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.rate_5));
                        break;
                    default:
                        tvTitle.setText("Oh, no!");
                        tvContent.setText("Please leave us some feedback");
                        ivIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.rate_1));
                        break;
                }

            }
        });
        tvPositive.setOnClickListener(this);
        tvNegative.setOnClickListener(this);

        if (session == 1) {
            tvNegative.setVisibility(View.GONE);
        }
    }
    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.dialog_rating_button_negative) {

            dismiss();
            showNever();

        } else if (view.getId() == R.id.dialog_rating_button_positive) {

            dismiss();

        }

    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {


        if (ratingBar.getRating() >= threshold) {
            thresholdPassed = true;

            if (builder.ratingThresholdClearedListener == null) {
                setRatingThresholdClearedListener();
            }
            builder.ratingThresholdClearedListener.onThresholdCleared(this, ratingBar.getRating(), thresholdPassed);

        } else {
            thresholdPassed = false;

            if (builder.ratingThresholdFailedListener == null) {
                setRatingThresholdFailedListener();
            }
            builder.ratingThresholdFailedListener.onThresholdFailed(this, ratingBar.getRating(), thresholdPassed);
        }

        if (builder.ratingDialogListener != null) {
            builder.ratingDialogListener.onRatingSelected(ratingBar.getRating(), thresholdPassed);
        }
        showNever();
    }

    private void setRatingThresholdClearedListener() {
        builder.ratingThresholdClearedListener = new Builder.RatingThresholdClearedListener() {
            @Override
            public void onThresholdCleared(RatingDialog ratingDialog, float rating, boolean thresholdCleared) {
                openPlaystore(context);
                dismiss();
            }
        };
    }

    private void setRatingThresholdFailedListener() {
        builder.ratingThresholdFailedListener = new Builder.RatingThresholdFailedListener() {
            @Override
            public void onThresholdFailed(RatingDialog ratingDialog, float rating, boolean thresholdCleared) {
                openForm();
            }
        };
    }

    private void openForm() {
        ratingButtons.setVisibility(View.GONE);
        ivIcon.setVisibility(View.GONE);
        tvTitle.setVisibility(View.GONE);
        ratingBar.setVisibility(View.GONE);
    }

    private void openPlaystore(Context context) {
        final Uri marketUri = Uri.parse(builder.playstoreUrl);
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, marketUri));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "Couldn't find PlayStore on this device", Toast.LENGTH_SHORT).show();
        }
    }

    public TextView getTitleTextView() {
        return tvTitle;
    }

    public TextView getPositiveButtonTextView() {
        return tvPositive;
    }

    public TextView getNegativeButtonTextView() {
        return tvNegative;
    }

    public ImageView getIconImageView() {
        return ivIcon;
    }

    public RotationRatingBar  getRatingBarView() {
        return ratingBar;
    }

    @Override
    public void show() {

        if (checkIfSessionMatches(session)) {
            super.show();
        }
    }
    int differenceDate = 0;

    private boolean checkIfSessionMatches(int session) {
        sharedpreferences = context.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE);


        String currentTime = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date());

        if (sharedpreferences.getBoolean("firstrun", true)) {
            sharedpreferences.edit().putBoolean("firstrun", false).commit();

            if (currentTime != null){
                sharedpreferences.edit().putString(DATE_FIRST, String.valueOf(currentTime)).commit();
            }
        }

        String currentDate = sharedpreferences.getString(DATE_FIRST, String.valueOf(currentTime));
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

        Date startDate = null;
        try {
            startDate = df.parse(currentDate);
//            startDate = df.parse("01/09/2021");
            String newDateString = df.format(startDate);
            System.out.println(newDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date endDate = null;
        try {
            endDate = df.parse(String.valueOf(currentTime));
//            endDate = df.parse(String.valueOf("03/09/2021"));
            String newDateString = df.format(endDate);
            System.out.println(newDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(startDate !=null && endDate !=null){
            differenceDate=
                    ((int)((endDate.getTime()/(24*60*60*1000))
                            -(int)(startDate.getTime()/(24*60*60*1000))));
        }

        if (session == 1) {
            return true;
        }


        if (sharedpreferences.getBoolean(SHOW_NEVER, false)) {
            return false;
        }

        int count = sharedpreferences.getInt(SESSION_COUNT, 1);

        if (session == count && differenceDate >= date) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt(SESSION_COUNT, 1);
            editor.commit();
            return true;
        } else if (session > count) {
            count++;
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt(SESSION_COUNT, count);
            editor.commit();
            return false;
        } else {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt(SESSION_COUNT, 2);
            editor.commit();
            return false;
        }
    }

    private void showNever() {
        sharedpreferences = context.getSharedPreferences(MyPrefs, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(SHOW_NEVER, true);
        editor.commit();
    }

    public static class Builder {

        private final Activity context;
        private boolean isShowLate;
        private boolean isDismiss;
        private int logo;
        private String appName;
        private String email;
        private MaybeLaterCallback maybeLaterCallback;
        private String title, positiveText, negativeText, playstoreUrl, btnLate;
        private int positiveTextColor, negativeTextColor, titleTextColor, ratingBarColor, ratingBarBackgroundColor, feedBackTextColor;
        private int positiveBackgroundColor, negativeBackgroundColor;
        private RatingThresholdClearedListener ratingThresholdClearedListener;
        private RatingThresholdFailedListener ratingThresholdFailedListener;
        private RatingDialogFormListener ratingDialogFormListener;
        private RatingDialogListener ratingDialogListener;
        private Drawable drawable;

        private int session = 1;
        private float threshold = 1;
        private int date = 1;
        public interface RatingThresholdClearedListener {
            void onThresholdCleared(RatingDialog ratingDialog, float rating, boolean thresholdCleared);
        }

        public interface RatingThresholdFailedListener {
            void onThresholdFailed(RatingDialog ratingDialog, float rating, boolean thresholdCleared);
        }

        public interface RatingDialogFormListener {
            void onFormSubmitted(String feedback);
        }

        public interface RatingDialogListener {
            void onRatingSelected(float rating, boolean thresholdCleared);
        }

        public Builder(Activity context) {
            this.context = context;
            // Set default PlayStore URL
            this.playstoreUrl = "market://details?id=" + context.getPackageName();
            initText();
        }

        private void initText() {
            title = context.getString(R.string.rating_dialog_experience);
            positiveText = context.getString(R.string.rating_dialog_maybe_later);
            negativeText = context.getString(R.string.rating_dialog_never);
        }

        public Builder session(int session) {
            this.session = session;
            return this;
        }

        public Builder date(int date) {
            if (date > 0){
                date = date - 1;
            }
            this.date = date;
            return this;
        }

        public Builder threshold(float threshold) {
            this.threshold = threshold;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /*public Builder icon(int icon) {
            this.icon = icon;
            return this;
        }*/

        public Builder icon(Drawable drawable) {
            this.drawable = drawable;
            return this;
        }

        public Builder positiveButtonText(String positiveText) {
            this.positiveText = positiveText;
            return this;
        }

        public Builder negativeButtonText(String negativeText) {
            this.negativeText = negativeText;
            return this;
        }

        public Builder titleTextColor(int titleTextColor) {
            this.titleTextColor = titleTextColor;
            return this;
        }

        public Builder positiveButtonTextColor(int positiveTextColor) {
            this.positiveTextColor = positiveTextColor;
            return this;
        }

        public Builder negativeButtonTextColor(int negativeTextColor) {
            this.negativeTextColor = negativeTextColor;
            return this;
        }

        public Builder positiveButtonBackgroundColor(int positiveBackgroundColor) {
            this.positiveBackgroundColor = positiveBackgroundColor;
            return this;
        }

        public Builder negativeButtonBackgroundColor(int negativeBackgroundColor) {
            this.negativeBackgroundColor = negativeBackgroundColor;
            return this;
        }

        public Builder onThresholdCleared(RatingThresholdClearedListener ratingThresholdClearedListener) {
            this.ratingThresholdClearedListener = ratingThresholdClearedListener;
            return this;
        }

        public Builder onThresholdFailed(RatingThresholdFailedListener ratingThresholdFailedListener) {
            this.ratingThresholdFailedListener = ratingThresholdFailedListener;
            return this;
        }

        public Builder onRatingChanged(RatingDialogListener ratingDialogListener) {
            this.ratingDialogListener = ratingDialogListener;
            return this;
        }

        public Builder onRatingBarFormSumbit(RatingDialogFormListener ratingDialogFormListener) {
            this.ratingDialogFormListener = ratingDialogFormListener;
            return this;
        }

        public Builder isShowButtonLater(boolean isShowLate) {
            this.isShowLate = isShowLate;
            return this;
        }
        public Builder isClickLaterDismiss(boolean isDismiss) {
            this.isDismiss = isDismiss;
            return this;
        }

        public Builder setNameApp(String appName) {
            this.appName = appName;
            return this;
        }
        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setOnlickMaybeLate(MaybeLaterCallback onlickMaybeLate) {
            this.maybeLaterCallback = onlickMaybeLate;
            return this;
        }

        public Builder setIcon(int logo) {
            this.logo = logo;
            return this;
        }

        public Builder setTextButtonLater(String s) {
            this.btnLate = s;
            return this;
        }

        public Builder ratingBarColor(int ratingBarColor) {
            this.ratingBarColor = ratingBarColor;
            return this;
        }

        public Builder ratingButtonColor(int ratingBarBackgroundColor) {
            this.ratingBarBackgroundColor = ratingBarBackgroundColor;
            return this;
        }

        public Builder feedbackTextColor(int feedBackTextColor) {
            this.feedBackTextColor = feedBackTextColor;
            return this;
        }

        public Builder playstoreUrl(String playstoreUrl) {
            this.playstoreUrl = playstoreUrl;
            return this;
        }

        public RatingDialog build() {
            return new RatingDialog(context, this);
        }
    }
}
