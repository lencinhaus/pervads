package it.polimi.dei.dbgroup.pedigree.pervads;

import it.polimi.dei.dbgroup.pedigree.pervads.PervAd;
import it.polimi.dei.dbgroup.pedigree.pervads.IPervAdsServiceListener;

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