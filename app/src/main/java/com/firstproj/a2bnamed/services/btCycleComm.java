package com.firstproj.a2bnamed.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;


public class btCycleComm extends IntentService {
    private static final String ACTION_FIND_CYCLE = "com.firstproj.a2bnamed.action.findcycle";
    private static final String ACTION_SEND_CYCLE = "com.firstproj.a2bnamed.action.sendcycle";

    public btCycleComm() {
        super("btCycleComm");
    }


    public static void startActionFindCycle(Context context) {
        Intent intent = new Intent(context, btCycleComm.class);
        intent.setAction(ACTION_FIND_CYCLE);
        context.startService(intent);
    }


    public static void startActionSendCycle(Context context, String param1, String param2) {
        Intent intent = new Intent(context, btCycleComm.class);
        intent.setAction(ACTION_SEND_CYCLE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FIND_CYCLE.equals(action)) {
                handleActionFindCycle();
            } else if (ACTION_SEND_CYCLE.equals(action)) {
                handleActionSendCycle();
            }
        }
    }

    /**
     * Handle action FindCycle in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFindCycle() {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action SendCycle in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSendCycle() {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
