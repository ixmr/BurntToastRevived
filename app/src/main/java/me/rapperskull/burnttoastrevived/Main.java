package me.rapperskull.burnttoastrevived;

import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XResources;

public class Main implements IXposedHookZygoteInit {
    private XSharedPreferences pref;
    private int size;
    private int margin;
    private Context context;
    private int LateralMargin = 0;
    private int VerticalMargin = 0;
    private int CentralMargin = 0;
    private boolean isN = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N);

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        XC_LayoutInflated hook = new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) throws Throwable {
                commonScripts();

                if (liparam.view instanceof LinearLayout) {
                    XposedBridge.log("Burnt Toast Revived: Found LinearLayout");

                    LinearLayout layout = (LinearLayout) liparam.view;
                    context = layout.getContext();
                    TextView view = (TextView) layout.findViewById(android.R.id.message);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.gravity = android.view.Gravity.CENTER;
                    params.rightMargin = LateralMargin;

                    view.setLayoutParams(params);

                    PackageManager pm = context.getPackageManager();

                    ImageView imageView = new ImageView(context);
                    int imageViewSize = Math.max(view.getHeight(), size);
                    imageView.setMaxHeight(imageViewSize);
                    imageView.setMaxWidth(imageViewSize);
                    imageView.setAdjustViewBounds(true);
                    imageView.setImageDrawable(context.getApplicationInfo().loadIcon(pm));

                    LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params1.gravity = android.view.Gravity.CENTER;
                    params1.setMargins(LateralMargin, VerticalMargin, CentralMargin, VerticalMargin);
                    imageView.setLayoutParams(params1);

                    layout.setOrientation(LinearLayout.HORIZONTAL);
                    layout.addView(imageView, 0);

                } else if (liparam.view instanceof RelativeLayout) {
                    XposedBridge.log("Burnt Toast Revived: Found RelativeLayout");

                    RelativeLayout layout = (RelativeLayout) liparam.view;
                    context = layout.getContext();
                    TextView view = (TextView) layout.findViewById(android.R.id.message);

                    layout.setBackground(view.getBackground());

                    LinearLayout inner = new LinearLayout(context);
                    RelativeLayout.LayoutParams innerParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    innerParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    innerParams.setMargins(0, 0, 0, 0);
                    inner.setLayoutParams(innerParams);

                    PackageManager pm = context.getPackageManager();

                    ImageView imageView = new ImageView(context);
                    int imageViewSize = Math.max(view.getHeight(), size);
                    imageView.setMaxHeight(imageViewSize);
                    imageView.setMaxWidth(imageViewSize);
                    imageView.setAdjustViewBounds(true);
                    imageView.setImageDrawable(context.getApplicationInfo().loadIcon(pm));

                    LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params1.gravity = android.view.Gravity.CENTER;
                    params1.setMargins(LateralMargin, VerticalMargin, CentralMargin, VerticalMargin);
                    imageView.setLayoutParams(params1);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.gravity = android.view.Gravity.CENTER;
                    params.rightMargin = LateralMargin;
                    params.leftMargin = 0;

                    view.setLayoutParams(params);
                    view.setBackground(null);

                    layout.removeAllViews();
                    inner.setOrientation(LinearLayout.HORIZONTAL);
                    inner.addView(imageView, 0);
                    inner.addView(view, 1);
                    layout.addView(inner, 0);

                } else {
                    XposedBridge.log("Burnt Toast Revived: Unknown Layout");
                }
            }
        };

        XResources.hookSystemWideLayout("android", "layout", "transient_notification", hook);
        try {
            XResources.hookSystemWideLayout("android", "layout", "tw_transient_notification", hook);
        } catch (Resources.NotFoundException e) {
            XposedBridge.log("Burnt Toast Revived: Layout tw_transient_notification not found");
        } catch (Throwable t) {
            XposedBridge.log("Burnt Toast Revived: Error hooking layout tw_transient_notification");
        }
    }

    private void commonScripts() {
        if (isN) {
            pref = new XSharedPreferences(prefsFile);
        } else {
            pref = new XSharedPreferences(Main.class.getPackage().getName(), "settings");
        }
        pref.getFile().setReadable(true, false);
        pref.reload();
        size = pref.getInt("icon_size", 96);
        margin = pref.getInt("margin_size", 1);
        XposedBridge.log("Burnt Toast Revived: Got size of " + size + " and margin of " + margin);

        CentralMargin = 5 + 5 * margin;
        if (margin == 0) {
            LateralMargin = 0;
            VerticalMargin = 0;
        } else {
            LateralMargin = 10 * (margin + 1);
            VerticalMargin = 10 + 5 * margin;
        }
    }
}
