package group6.interactivehandwriting.activities.Room;

import android.util.Log;

import group6.interactivehandwriting.common.app.actions.draw.EndDrawAction;
import group6.interactivehandwriting.common.app.actions.draw.MoveDrawAction;
import group6.interactivehandwriting.common.app.actions.draw.StartDrawAction;

/**
 * Created by JakeL on 10/1/18.
 */

public class RoomViewActionUtility {
    private static float touchX;
    private static float touchY;

    private static int R;
    private static int G;
    private static int B;
    private static int a;
    private static float Width;

    private static boolean toggleEraserValue;

    static {
        R = 0;
        G = 0;
        B = 0;
        a = 255;
        toggleEraserValue = false;
        Width = 11;
    }

    public static void setTouchPosition(float x, float y) {
        touchX = x;
        touchY = y;
    }

    public static void setEraser() {
        toggleEraserValue = !toggleEraserValue;
    }

    public static StartDrawAction touchStarted(float x, float y) {
        setTouchPosition(x, y);
        StartDrawAction startAction = new StartDrawAction(true);
        startAction.setPosition(x, y);
        startAction.setColor(R, G, B, a);
        startAction.setWidth(12.0f);
        startAction.setErase(toggleEraserValue);
        startAction.setWidth(Width);
        return startAction;
    }

    public static boolean didTouchMove(float x, float y, float tolerance) {
        float dx = x - touchX;
        float dy = y - touchY;
        return Math.abs(dx) >= tolerance || Math.abs(dy) >= tolerance;
    }

    public static MoveDrawAction touchMoved(float x, float y) {
        float dx = x - touchX;
        float dy = y - touchY;
        MoveDrawAction action = new MoveDrawAction();
        action.setMovePosition(touchX, touchY, dx, dy);
        setTouchPosition(x, y);
        return action;
    }

    public static EndDrawAction touchReleased() {
        EndDrawAction action = new EndDrawAction();
        action.setPosition(touchX, touchY);
        return action;
    }

    public static void ChangeColorHex(String hexValue) {
        toggleEraserValue = false;
        a = Integer.parseInt(hexValue.substring(0, 2), 16);
        R = Integer.parseInt(hexValue.substring(2, 4), 16);
        G = Integer.parseInt(hexValue.substring(4, 6), 16);
        B = Integer.parseInt(hexValue.substring(6, 8), 16);
    }

    public static void ChangeWidth(float width) {
        Width = width;
    }
}
