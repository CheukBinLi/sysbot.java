package com.github.cheukbinli.core.ns.command;

import com.github.cheukbinli.core.ns.command.constant.KeyboardKey;
import com.github.cheukbinli.core.ns.command.constant.ScreenState;
import com.github.cheukbinli.core.ns.command.constant.SwitchButton;
import com.github.cheukbinli.core.ns.constant.SwitchStick;

public interface ControllerCommand extends BaseCommand {

    /***
     *     按键盘
     *      HidNpadButton key = Util::parseStringToButton(argv[1]);
     * 		Commands::click(key);
     * @return
     */
    default void key(KeyboardKey... keys) throws CommectionException {
        StringBuilder command = new StringBuilder("key");
        for (KeyboardKey key : keys) {
            command.append(" ").append(key.getV());
        }
        write(command.substring(1));
    }

    default void screenState(ScreenState state) {

        write("screen" + state);
    }

    /***
     *     按键
     *      HidNpadButton key = Util::parseStringToButton(argv[1]);
     * 		Commands::click(key);
     * @return
     */
    default void clickButton(SwitchButton.Button button) throws CommectionException {
        write(SwitchButton.ButtonEvent.click + " " + button);
    }

    /***
     *     按下
     *      HidNpadButton key = Util::parseStringToButton(argv[1]);
     * 		Commands::press(key);
     * @return
     */
    default void pressButton(SwitchButton.Button button) throws CommectionException {
        write(SwitchButton.ButtonEvent.press + " " + button);
    }

    /***
     *     弹起
     *      HidNpadButton key = Util::parseStringToButton(argv[1]);
     * 		Commands::release(key);
     * @return
     */
    default void releaseButton(SwitchButton.Button button) throws CommectionException {
        write(SwitchButton.ButtonEvent.release + " " + button);
    }

    /***
     *     移动遥杆
     *     	int dxVal = strtol(argv[2], NULL, 0);
     * 		if (dxVal > JOYSTICK_MAX)
     * 			dxVal = JOYSTICK_MAX; // 0x7FFF
     * 		if (dxVal < JOYSTICK_MIN)
     * 			dxVal = JOYSTICK_MIN; //-0x8000
     * 		int dyVal = strtol(argv[3], NULL, 0);
     * 		if (dyVal > JOYSTICK_MAX)
     * 			dyVal = JOYSTICK_MAX;
     * 		if (dyVal < JOYSTICK_MIN)
     * 			dyVal = JOYSTICK_MIN;
     *      Commands::setStickState(side, dxVal, dyVal);
     * @return
     */
    default void setStick(SwitchStick stick, float x, float y) throws CommectionException {
        write(stick + " " + x + " " + y);
    }

    /***
     *     分离控制器
     *      Commands::detachController()
     * @return
     */
    default void detachController() throws CommectionException {
        write("detachController");
    }

    /***
     *     模拟触碰
     *      Commands::detachController()
     * @return
     */
    default void touch() throws CommectionException {

    }

    default void touchHold() throws CommectionException {

    }

    default void touchDraw() throws CommectionException {

    }

    default void touchCancel() throws CommectionException {

    }

}
