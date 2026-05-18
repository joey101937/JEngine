package GameDemo.RTSDemo;

import GameDemo.RTSDemo.Buttons.DigInButton;
import GameDemo.RTSDemo.Buttons.DigOutButton;
import GameDemo.RTSDemo.Buttons.LayMineButton;
import GameDemo.RTSDemo.Buttons.LaunchMissileButton;
import java.util.HashMap;
import java.util.Map;

public class ButtonHotkeyMap {

    private static final Map<Class<? extends CommandButton>, Character> MAP = new HashMap<>();

    static {
        MAP.put(LayMineButton.class,      'Q');
        MAP.put(DigInButton.class,        'Q');
        MAP.put(DigOutButton.class,       'U');
        MAP.put(LaunchMissileButton.class,'Q');
    }

    /** Returns the uppercase hotkey character for the given button class, or 0 if none. */
    public static char getHotkey(Class<? extends CommandButton> buttonClass) {
        Character c = MAP.get(buttonClass);
        return c != null ? c : 0;
    }
}
