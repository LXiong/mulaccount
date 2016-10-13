package io.virtualapp.util;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.privacy.common.Utils;

import io.virtualapp.R;

/**
 * Created by wangqi on 16/4/11.
 */
public class ShowDialogPresenter {
    //   public static final String FIVE_STARED = "five_sta_ed";

    public static void showDialog(final Context context) {
        final View alertDialogView = View.inflate(context, R.layout.ivy_privacy_space_five_star_rating, null);
        final AlertDialog d = new AlertDialog.Builder(context, R.style.add_dialog).create();
//        Utils.addAlertAttribute(d.getWindow());
        d.setView(alertDialogView);
        d.show();

        alertDialogView.findViewById(R.id.next_time).setOnClickListener(v -> d.dismiss());
        alertDialogView.findViewById(R.id.five_star_button).setOnClickListener(v -> {
            d.dismiss();
            if (com.privacy.common.Utils.hasPlayStore(v.getContext())) {
                Utils.rateUs(v.getContext());
            }

            View dialogView = View.inflate(v.getContext(), R.layout.scan_result_rate_five, null);

            final FiveRateWidget w = new FiveRateWidget(context, FiveRateWidget.MATCH_PARENT, FiveRateWidget.MATCH_PARENT, FiveRateWidget.PORTRAIT);
            w.addView(dialogView);
            w.addToWindow();

            w.setOnClickListener(v1 -> {
                w.removeAllViews();
                w.removeFromWindow();

            });
        });


        alertDialogView.findViewById(R.id.five_complaint).setOnClickListener(v -> {
            d.cancel();
            showComplainDialog(context);
        });

    }


    public static void showComplainDialog(final Context context) {
        final View alertDialogView = View.inflate(context, R.layout.scan_result_rate_click, null);
        final AlertDialog d = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert).create();

        //Utils.addAlertAttribute(d.getWindow());
        d.setView(alertDialogView);
        d.show();
        TextView submitText = (TextView) alertDialogView.findViewById(R.id.submit);
        submitText.setOnClickListener(v -> {
           /* EditText content = (EditText) alertDialogView.findViewById(R.id.content);
            EditText email = (EditText) alertDialogView.findViewById(R.id.email);
            if (content.getText().length() != 0 && email.getText().length() != 0) {

                RiseSdk.track("privacespace_content",
                        email.getText().toString() + "  " + content.getText().toString(), "", 1);

            } else if (content.getText().length() == 0 && email.getText().length() != 0) {
                RiseSdk.track(MyTracker.CATEGORY_RATE_BAD_CONTENT,
                        email.getText().toString() + "  ", "", 1);


            } else if (content.getText().length() != 0 && email.getText().length() == 0) {
                RiseSdk.track(MyTracker.CATEGORY_RATE_BAD_CONTENT,
                        content.getText().toString() + "  ", "", 1);

            }*/
            d.cancel();
        });

    }

}
