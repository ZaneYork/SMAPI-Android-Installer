package com.zane.smapiinstaller.ui.about;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.didikee.donate.AlipayDonate;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.zane.smapiinstaller.R;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.utils.DialogUtils;

import java.time.Duration;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, root);
        return root;
    }
    @OnClick(R.id.button_release) void release() {
        CommonLogic.openUrl(this.getContext(), "https://github.com/ZaneYork/SMAPI-Android-Installer/releases");
    }
    @OnClick(R.id.button_gplay) void gplay() {
        try
        {
            this.OpenPlayStore("market://details?id=" + this.getActivity().getPackageName());
        }
        catch (ActivityNotFoundException ex)
        {
            CommonLogic.openUrl(this.getContext(), "https://play.google.com/store/apps/details?id=" + this.getActivity().getPackageName());
        }

    }
    private void OpenPlayStore(String url)
    {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(Uri.parse(url));
        intent.setPackage("com.android.vending");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.getActivity().startActivity(intent);
    }

    @OnClick({R.id.button_qq_group_1, R.id.button_qq_group_2}) void joinQQ(Button which) {
        String baseUrl = "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D";
        if(which.getId() == R.id.button_qq_group_1) {
            CommonLogic.openUrl(this.getContext(), baseUrl + "AAflCLHiWw1haM1obu_f-CpGsETxXc6b");
        }
        else {
            CommonLogic.openUrl(this.getContext(), baseUrl + "kshK7BavcS2jXZ6exDvezc18ksLB8YsM");
        }
    }

    @OnClick(R.id.button_donation) void donation() {
        Context context = this.getContext();
        DialogUtils.setCurrentDialog(new MaterialDialog.Builder(context).title(R.string.button_donation_text).items(R.array.donation_methods).itemsCallback((dialog, itemView, position, text) -> {
            switch (position){
                case 0:
                    boolean hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(context);
                    if (hasInstalledAlipayClient) {
                        AlipayDonate.startAlipayClient(this.getActivity(), "fkx13570v1pp2xenyrx4y3f");
                    }
                    else {
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
                        if (CommonLogic.copyToClipboard(context, "9188262")) {
                            PackageManager packageManager = context.getPackageManager();
                            Intent intent = packageManager.getLaunchIntentForPackage("com.eg.android.AlipayGphone");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                            Toast.makeText(context, R.string.toast_redpacket_message, Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
            }
        }).show());
    }
}
