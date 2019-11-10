package com.amitinside.aspecio.service;

@FunctionalInterface
public interface WovenServiceListener {

    void onWovenServiceEvent(WovenServiceEvent event, WovenService wovenService);
}
