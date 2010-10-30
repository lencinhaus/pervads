package it.polimi.dei.dbgroup.pedigree.pervads.client.android;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.IPervAdsServiceListener;

interface IPervAdsService
{
    void startUpdate();
    boolean isSupported();
    boolean isEnabled();
    boolean isUpdating();
    void registerListener(IPervAdsServiceListener listener);
    void unregisterListener(IPervAdsServiceListener listener);
}