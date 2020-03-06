package com.zane.smapiinstaller.logic;

import android.view.View;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.entity.DownloadableContentList;

public class DownloadabeContentManager {

    private final View root;

    private static final String TAG = "DLC_MGR";

    private static boolean updateChecked = false;

    private static DownloadableContentList downloadableContentList = null;

    public DownloadabeContentManager(View root) {
        this.root = root;
        downloadableContentList = CommonLogic.getAssetJson(root.getContext(), "downloadable_content_list.json", DownloadableContentList.class);
        if(!updateChecked) {
            updateChecked = true;
            OkGo.<String>get(Constants.DLC_LIST_UPDATE_URL).execute(new StringCallback(){
                @Override
                public void onSuccess(Response<String> response) {
                    DownloadableContentList content = new Gson().fromJson(response.body(), DownloadableContentList.class);
                    if(downloadableContentList.getVersion() < content.getVersion()) {
                        CommonLogic.writeAssetJson(root.getContext(), "downloadable_content_list.json", content);
                        downloadableContentList = content;
                    }
                }
            });
        }
    }

    public DownloadableContentList getDownloadableContentList() {
        return downloadableContentList;
    }
}
