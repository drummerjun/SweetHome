package com.evsp.sweethome.services;

import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

public class CloudInstanceIDService extends InstanceIDListenerService {
    public void onTokenRefresh() {
        refreshAllTokens();
    }

    private void refreshAllTokens() {
        ArrayList<TokenList> tokenList = TokenList.get();
        InstanceID iid = InstanceID.getInstance(this);
        for(tokenIte : tokenList) {
            tokenItem.token = iid.getToken(
                    tokenItem.authorizedEntity, tokenItem.scope, tokenItem.options);
        }
    }
}
