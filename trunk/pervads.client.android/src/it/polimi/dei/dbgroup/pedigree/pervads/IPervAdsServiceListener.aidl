package it.polimi.dei.dbgroup.pedigree.pervads;

oneway interface IPervAdsServiceListener
{
    void updateEvent(int type);
    void networkScanEvent(int type, int numFoundNetworks);
    void networkConnectionEvent(int type, String SSID);
}