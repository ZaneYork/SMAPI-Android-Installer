package com.zane.smapiinstaller.ui.about

import android.content.Context
import android.content.Intent
import android.didikee.donate.AlipayDonate
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.microsoft.appcenter.crashes.Crashes
import com.zane.smapiinstaller.R
import com.zane.smapiinstaller.constant.Constants
import com.zane.smapiinstaller.constant.DialogAction
import com.zane.smapiinstaller.databinding.FragmentAboutBinding
import com.zane.smapiinstaller.logic.CommonLogic.copyToClipboard
import com.zane.smapiinstaller.logic.CommonLogic.openUrl
import com.zane.smapiinstaller.logic.CommonLogic.showAnimation
import com.zane.smapiinstaller.logic.CommonLogic.showPrivacyPolicy
import com.zane.smapiinstaller.utils.DialogUtils

/**
 * @author Zane
 */
class AboutFragment : Fragment() {
    private lateinit var binding: FragmentAboutBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        binding.buttonRelease.setOnClickListener { release() }
        binding.buttonQqGroup1.setOnClickListener { joinQQ() }
        binding.buttonDonation.setOnClickListener { donation() }
        binding.buttonPrivacyPolicy.setOnClickListener { privacyPolicy() }
        return binding.root
    }

    private fun release() {
        this.context?.let {
            openUrl(it, Constants.RELEASE_URL)
        }
    }

    private fun joinQQ() {
        val baseUrl =
            "mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D"
        DialogUtils.showListItemsDialog(
            binding.imgHeart, R.string.button_qq_group_text, R.array.qq_group_list
        ) { _, position ->
            when (position) {
                0 -> this.context?.let {
                    openUrl(it, baseUrl + "AAflCLHiWw1haM1obu_f-CpGsETxXc6b")
                }

                1 -> this.context?.let {
                    openUrl(it, baseUrl + "kshK7BavcS2jXZ6exDvezc18ksLB8YsM")
                }

                2 -> this.context?.let {
                    openUrl(it, baseUrl + "zqsWYGBuAxPx0n9RI_ONs-7NA1Mm48QY")
                }

                3 -> this.context?.let {
                    openUrl(it, baseUrl + "uYnxVTCGlWuLbeb3XA3mDXoO0tlYhy3J")
                }

                else -> this.context?.let {
                    openUrl(it, "https://s.zaneyork.cn:8443/s/qc")
                }
            }
        }
    }

    fun donation() {
        DialogUtils.showListItemsDialog(
            binding.imgHeart, R.string.button_donation_text, R.array.donation_methods
        ) { _, position ->
            showAnimation(
                binding.imgHeart, R.anim.heart_beat
            ) {
                this.activity?.let { activity ->
                    listSelectLogic(
                        activity, position
                    )
                }
            }
        }
    }

    private fun privacyPolicy() {
        showPrivacyPolicy(binding.imgHeart)
    }

    private fun listSelectLogic(context: Context, position: Int) {
        when (position) {
            0 -> {
                val hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(context)
                if (hasInstalledAlipayClient) {
                    try {
                        AlipayDonate.startAlipayClient(this.activity, "fkx13570v1pp2xenyrx4y3f")
                    } catch (e: Exception) {
                        Crashes.trackError(e)
                        openUrl(context, "http://dl.zaneyork.cn/alipay.png")
                    }
                } else {
                    openUrl(context, "http://dl.zaneyork.cn/alipay.png")
                }
            }

            1 -> openUrl(context, "http://dl.zaneyork.cn/wechat.png")
            2 -> openUrl(context, "http://dl.zaneyork.cn/qqpay.png")
            3 -> {
                val hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(context)
                if (hasInstalledAlipayClient) {
                    if (copyToClipboard(context, Constants.RED_PACKET_CODE)) {
                        val packageManager = context.packageManager
                        packageManager.getLaunchIntentForPackage("com.eg.android.AlipayGphone")
                            ?.let { intent ->
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                                Toast.makeText(
                                    context, R.string.toast_redpacket_message, Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                }
            }

            4 -> openUrl(context, "http://zaneyork.cn/dl/list.html")
            else -> {}
        }
    }
}