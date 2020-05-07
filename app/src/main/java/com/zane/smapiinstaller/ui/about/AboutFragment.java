package com.zane.smapiinstaller.ui.about;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.didikee.donate.AlipayDonate;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.microsoft.appcenter.crashes.Crashes;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.constant.DialogAction;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.utils.DialogUtils;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java9.util.function.BiConsumer;

/**
 * @author Zane
 */
public class AboutFragment extends Fragment {

    @BindView(R.id.img_heart)
    ImageView imgHeart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, root);
        return root;
    }

    @OnClick(R.id.button_release)
    void release() {
        CommonLogic.doOnNonNull(this.getContext(), (context) -> CommonLogic.openUrl(context, "https://github.com/ZaneYork/SMAPI-Android-Installer/releases"));
    }

    @OnClick(R.id.button_gplay)
    void gplay() {
        CommonLogic.openInPlayStore(this.getActivity());
    }

    @OnClick(R.id.button_qq_group_1)
    void joinQQ() {
        String baseUrl = "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D";
        DialogUtils.showListItemsDialog(imgHeart, R.string.button_qq_group_text, R.array.qq_group_list, (dialog, position) -> {
            switch (position){
                case 0:
                    CommonLogic.doOnNonNull(this.getContext(), (context) -> CommonLogic.openUrl(context, baseUrl + "AAflCLHiWw1haM1obu_f-CpGsETxXc6b"));
                    break;
                case 1:
                    CommonLogic.doOnNonNull(this.getContext(), (context) -> CommonLogic.openUrl(context, baseUrl + "kshK7BavcS2jXZ6exDvezc18ksLB8YsM"));
                    break;
                default:
                    CommonLogic.doOnNonNull(this.getContext(), (context) -> CommonLogic.openUrl(context, baseUrl + "zqsWYGBuAxPx0n9RI_ONs-7NA1Mm48QY"));
                    break;
            }
        });
    }

    @OnClick(R.id.button_donation)
    void donation() {
        DialogUtils.showListItemsDialog(imgHeart, R.string.button_donation_text, R.array.donation_methods, (dialog, position) ->
                CommonLogic.showAnimation(imgHeart, R.anim.heart_beat, (animation) ->
                        CommonLogic.doOnNonNull(this.getActivity(), (activity) -> listSelectLogic(activity, position))));
    }

    @OnClick(R.id.button_privacy_policy)
    void privacyPolicy() {
        CommonLogic.showPrivacyPolicy(imgHeart, (dialog, dialogAction) -> {
        });
    }

    private void listSelectLogic(Context context, int position) {
        switch (position) {
            case 0:
                boolean hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(context);
                if (hasInstalledAlipayClient) {
                    try {
                        AlipayDonate.startAlipayClient(this.getActivity(), "fkx13570v1pp2xenyrx4y3f");
                    } catch (Exception e) {
                        Crashes.trackError(e);
                        CommonLogic.openUrl(context, "http://dl.zaneyork.cn/alipay.png");
                    }
                } else {
                    CommonLogic.openUrl(context, "http://dl.zaneyork.cn/alipay.png");
                }
                break;
            case 1:
                CommonLogic.openUrl(context, "http://dl.zaneyork.cn/wechat.png");
                break;
            case 2:
                CommonLogic.openUrl(context, "http://dl.zaneyork.cn/qqpay.png");
                break;
            case 3:
                hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(context);
                if (hasInstalledAlipayClient) {
                    if (CommonLogic.copyToClipboard(context, Constants.RED_PACKET_CODE)) {
                        PackageManager packageManager = context.getPackageManager();
                        CommonLogic.doOnNonNull(packageManager.getLaunchIntentForPackage("com.eg.android.AlipayGphone"), (intent) -> {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                            Toast.makeText(context, R.string.toast_redpacket_message, Toast.LENGTH_LONG).show();
                        });
                    }
                }
                break;
            case 4:
                CommonLogic.openUrl(context, "http://zaneyork.cn/download/list.html");
                break;
            default:
                break;
        }
    }
}
