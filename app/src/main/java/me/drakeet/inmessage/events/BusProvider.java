package me.drakeet.inmessage.events;

import com.squareup.otto.Bus;

/**
 * Created by drakeet on 12/1/14.
 */
public class BusProvider {
    private static final Bus BUS = new Bus();

    public static Bus getInstance() {
        return BUS;
    }

    private BusProvider() {
        // No instances.
    }
}
