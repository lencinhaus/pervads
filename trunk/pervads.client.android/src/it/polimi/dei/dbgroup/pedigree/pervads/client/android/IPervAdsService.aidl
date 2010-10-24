package it.polimi.dei.dbgroup.pedigree.pervads.client.android;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.PervAd;
import it.polimi.dei.dbgroup.pedigree.pervads.client.android.IPervAdsServiceListener;

interface IPervAdsService
{
    void startUpdate();
    PervAd[] getPervAds();
    void pervAdSeen(String pervAdId);
    boolean isSupported();
    boolean isEnabled();
    boolean isUpdating();
    void registerListener(IPervAdsServiceListener listener);
    void unregisterListener(IPervAdsServiceListener listener);
}