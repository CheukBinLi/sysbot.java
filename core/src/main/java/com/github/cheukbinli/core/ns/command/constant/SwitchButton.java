package com.github.cheukbinli.core.ns.command.constant;

public class SwitchButton {

    public enum Button {
        A,
        B,
        X,
        Y,
        LSTICK,
        RSTICK,
        L,
        R,
        ZL,
        ZR,
        PLUS,
        MINUS,
        DLEFT,
        DUP,
        DDOWN,
        DRIGHT,
        HOME,
        CAPTURE
    }

    public enum ButtonEvent {
        click,
        press,
        release
    }


    public static String keyAction(ButtonEvent keyEvent, Button key) {
        return keyEvent.name() + " " + key.name();
    }

//    摇杆
//    setStick LEFT/RIGHT <xVal from -0x8000 to 0x7FFF> <yVal from -0x8000 to 0x7FFF

}
