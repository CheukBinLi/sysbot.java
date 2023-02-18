package com.github.cheukbinli.core.ns.service;

import com.github.cheukbinli.core.GlobalLogger;
import com.github.cheukbinli.core.ns.SwitchCommandApi;
import com.github.cheukbinli.core.ns.SwitchException;
import com.github.cheukbinli.core.ns.constant.KeyboardKey;
import com.github.cheukbinli.core.ns.constant.PokeDataOffsetsSV;
import com.github.cheukbinli.core.ns.constant.ScreenState;
import com.github.cheukbinli.core.ns.constant.SwitchButton;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.util.function.Function;

import static com.github.cheukbinli.core.ns.constant.PokeDataOffsetsSV.BOX_SLOT_SIZE;

public class SwitchService {

    private long PortalOffset;
    private long ConnectedOffset;
    private long TradePartnerNIDOffset;
    private long BoxStartOffset;
    private long OverworldOffset;
    private final SwitchCommandApi switchCommandApi;
    private long globalClickInterval = 800;

    public SwitchService(SwitchCommandApi switchCommandApi) {
        this.switchCommandApi = switchCommandApi;
        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void init() throws IOException {
        BoxStartOffset = new BigInteger(new String(switchCommandApi.pointerAll(PokeDataOffsetsSV.BoxStartPokemonPointer)), 16).longValue();
        OverworldOffset = new BigInteger(new String(switchCommandApi.pointerAll(PokeDataOffsetsSV.OverworldPointer)), 16).longValue();
        PortalOffset = new BigInteger(new String(switchCommandApi.pointerAll(PokeDataOffsetsSV.PortalBoxStatusPointer)), 16).longValue();
        ConnectedOffset = new BigInteger(new String(switchCommandApi.pointerAll(PokeDataOffsetsSV.IsConnectedPointer)), 16).longValue();
        TradePartnerNIDOffset = new BigInteger(new String(switchCommandApi.pointerAll(PokeDataOffsetsSV.LinkTradePartnerNIDPointer)), 16).longValue();
        switchCommandApi.SetScreen(ScreenState.Off);
//        switchCommandApi.SetScreen(ScreenState.On);
        System.out.println("Switch初始化");
        GlobalLogger.appendln("Switch初始化成功");
    }

    /***
     * PKM写入内存
     * @param pkm
     * @throws IOException
     * @throws InterruptedException
     */
    public void pushPKM(byte[] pkm, int slot) throws IOException, InterruptedException {
        switchCommandApi.pushPKM(pkm, BoxStartOffset + ((slot > 0 ? --slot : slot) * BOX_SLOT_SIZE));
    }

    /***
     * PKM写入内存
     * @param
     * @throws IOException
     * @throws InterruptedException
     */
    public byte[] pullPKM(int slot, int size) throws IOException, InterruptedException, DecoderException {
        byte[] data = switchCommandApi.peekAbsolute(size, BoxStartOffset + ((slot > 0 ? --slot : slot) * size));
        return Hex.decodeHex(new String(data));
    }

    //是否已打开WEB界面
    // Usually 0x9-0xA if fully loaded into Poké Portal.
    public boolean IsInPokePortal() throws IOException, DecoderException {
        byte[] data = switchCommandApi.peekAbsolute(1, PortalOffset);
        data = Hex.decodeHex(new String(data));
        return data[0] >= 9;
    }

    // Usually 4-6 in a box.
    public boolean IsInBox() throws IOException, DecoderException {
        byte[] data = switchCommandApi.peekAbsolute(1, PortalOffset);
        data = Hex.decodeHex(new String(data));
        return data[0] < 8;
    }

    //对话速度
    public int GetTextSpeed() throws IOException {
        var data = switchCommandApi.pointerPeek(1, PokeDataOffsetsSV.ConfigPointer);
        return data[0] & 3;
    }

    //    设置当前BOX分页为第1页 box=0
    public void SetCurrentBox(byte box) throws IOException {
        switchCommandApi.pointerPoke(new byte[]{box}, PokeDataOffsetsSV.CurrentBoxPointer);
    }

    //  是否在线
    public boolean IsConnectedOnline() throws IOException, DecoderException {
        var data = switchCommandApi.peekAbsolute(1, ConnectedOffset);
        data = Hex.decodeHex(new String(data));
        return data[0] == 1;
    }

    //    //获取交易人NID
    public long GetTradePartnerNID() throws IOException, DecoderException {
        byte[] data = switchCommandApi.peekAbsolute(8, TradePartnerNIDOffset);
        return new BigInteger(new String(data), 16).longValue();
    }

    //清除
    public void ClearTradePartnerNID() throws IOException {
        switchCommandApi.pokeAbsolute(new byte[8], TradePartnerNIDOffset);
    }

    //野外指针
    public boolean IsOnOverworld() throws IOException, DecoderException {
        var data = switchCommandApi.peekAbsolute(1, OverworldOffset);
        data = Hex.decodeHex(new String(data));
        return data[0] == 0x11;
    }

    public boolean IsRunnningProgram() throws IOException {
        String title = new String(switchCommandApi.getTitleID());
        return title.equals(PokeDataOffsetsSV.VioletID) || title.equals(PokeDataOffsetsSV.ScarletID);
    }

    /***
     * 获取训练家信息
     * @return
     * @throws IOException
     */
    public byte[] GetFakeTrainerSAV() throws IOException {
        return switchCommandApi.pointerPeek(PokeDataOffsetsSV.FAKE_TRAINER_SAV_INFO_LENGTH, PokeDataOffsetsSV.MyStatusPointer);
    }

    /***
     * 获取交易时训练家信息
     * @return
     * @param first  0-1
     * @throws IOException
     */
    public byte[] GetTradePartnerInfo(boolean first) throws IOException {
        return switchCommandApi.pointerPeek(PokeDataOffsetsSV.TradeMyStatusInfoLength, first ? PokeDataOffsetsSV.Trader1MyStatusPointer : PokeDataOffsetsSV.Trader2MyStatusPointer);
    }

    /***
     *
     * @param slot 监视槽位
     * @param referenceData 参数数据
     * @param watchTimeSC 监视时长，秒
     * @param intervalMs 间隙时间，毫秒
     * @param loopAction -IN 热荐花费人时间(毫秒)
     * @param loopAction -OUT 实际花费时间(毫秒)
     * @return
     */
    public boolean monitorSlotChanges(int slot, byte[] referenceData, int watchTimeSC, long intervalMs, Function<Long, Integer> loopAction) throws DecoderException, IOException, InterruptedException {
        long time = watchTimeSC * 1000;
        boolean ressult = false;
        long loopActionWaitingTime = 700;
        int len = referenceData.length;
        long tempTime;
        do {
            tempTime = loopAction.apply(loopActionWaitingTime) + intervalMs;
            if (!arrayEquals(referenceData, pullPKM(slot, len))) {
                return true;
            }
            Thread.sleep(intervalMs);
            if (!IsInBox()) {
                System.out.println("退出交易");
                GlobalLogger.appendln("退出交易");
                return false;
            }
        } while ((time -= tempTime) > 0);
        return ressult;
    }

    boolean arrayEquals(byte[] t1, byte[] t2) {
        int len = t1.length;
        for (int i = 0; i < len; i++) {
            if (t1[i] != t2[i]) {
                return false;
            }
        }
        return true;
    }

    /***
     * 切换野外环境
     * @throws IOException
     * @throws DecoderException
     * @throws InterruptedException
     */
    public void recoverToOverworld() throws IOException, DecoderException, InterruptedException {
        recoverToOverworld(3);
    }

    public void recoverToOverworld(int count) throws IOException, DecoderException, InterruptedException {
        count = count < 3 ? 3 : count;
        do {
            System.out.print("返回第" + count + "次。  ");
            GlobalLogger.append("返回第" + count + "次。  ");
            switchCommandApi.clickButton(SwitchButton.Button.B, globalClickInterval);
            switchCommandApi.clickButton(SwitchButton.Button.B, globalClickInterval);
            switchCommandApi.clickButton(SwitchButton.Button.B, globalClickInterval);
            switchCommandApi.clickButton(SwitchButton.Button.B, globalClickInterval);
            switchCommandApi.clickButton(SwitchButton.Button.A, globalClickInterval);
        } while (!IsOnOverworld() && count-- >= 0);
        System.out.println("\n");
        GlobalLogger.append("\n");
    }

    /***
     * 连线
     * @throws IOException
     * @throws DecoderException
     * @throws InterruptedException
     */
    public void tryOnline() throws IOException, DecoderException, InterruptedException {
        if (!IsConnectedOnline()) {
            System.out.println("返回野外界面->");
            GlobalLogger.append("返回野外界面->");
            recoverToOverworld();
            for (int i = 0; i < 3; i++) {
                if (IsOnOverworld()) {
                    System.out.print("开始联网->");
                    GlobalLogger.append("开始联网->");
                    switchCommandApi.clickButton(SwitchButton.Button.X, globalClickInterval);
                    switchCommandApi.clickButton(SwitchButton.Button.L, globalClickInterval);
                    switchCommandApi.clickButton(SwitchButton.Button.L);
                    for (int j = 0; j < 60; j++) {
                        Thread.sleep(globalClickInterval);
                        if (IsConnectedOnline()) {
                            System.out.print("连网成功:" + IsConnectedOnline());
                            GlobalLogger.append("连网成功:" + IsConnectedOnline());
                            Thread.sleep(globalClickInterval * 5);
                            System.out.print("->返回野外\n");
                            GlobalLogger.appendln("->返回野外");
                            recoverToOverworld();
                            return;
                        }
                    }
                }

            }
        }
        if (!IsConnectedOnline()) {
            GlobalLogger.appendln("联网失败。");
            System.out.println("联网失败。");
        }
    }

    /***
     * 进入门户
     * @throws IOException
     * @throws InterruptedException
     * @throws DecoderException
     */
    public void enterPokePortal() throws IOException, InterruptedException, DecoderException {

        for (int i = 0; i < 5; i++) {
            recoverToOverworld();
            Thread.sleep(1000L);
            System.out.println("X菜单-> 门户->");
            switchCommandApi.clickButton(SwitchButton.Button.X, globalClickInterval);
            switchCommandApi.clickButton(SwitchButton.Button.DLEFT, globalClickInterval);
            switchCommandApi.clickButton(SwitchButton.Button.DRIGHT, globalClickInterval);
            switchCommandApi.clickButton(SwitchButton.Button.DDOWN, globalClickInterval);
            switchCommandApi.clickButton(SwitchButton.Button.DDOWN, globalClickInterval);
            switchCommandApi.clickButton(SwitchButton.Button.A, globalClickInterval);
            Thread.sleep(globalClickInterval * 7);
            if (IsInPokePortal()) {
                System.out.println("进入pkm门户");
                GlobalLogger.append("进入pkm门户->");
                return;
            }
        }
        GlobalLogger.appendln("进入pkm门户失败。");
        throw new SwitchException("进入pkm门户失败。");
    }

    /***
     * 选择连接交换
     * @throws DecoderException
     * @throws IOException
     * @throws InterruptedException
     */
    public void selectTrademenu() throws DecoderException, IOException, InterruptedException {
        if (!IsConnectedOnline()) {
            tryOnline();
        }
        enterPokePortal();
        switchCommandApi.clickButton(SwitchButton.Button.DDOWN, globalClickInterval);
        switchCommandApi.clickButton(SwitchButton.Button.DDOWN, globalClickInterval);
        switchCommandApi.clickButton(SwitchButton.Button.A, globalClickInterval);
    }

    public void passCode(String password) throws InterruptedException, IOException {

        System.out.println("清空密语");
        GlobalLogger.append("清空密语->");
        switchCommandApi.clickButton(SwitchButton.Button.X, globalClickInterval);

        if (null == password || password.length() < 1) {
            //空密码
            switchCommandApi.clickButton(SwitchButton.Button.A, globalClickInterval);
            //空密码
            switchCommandApi.clickButton(SwitchButton.Button.A, globalClickInterval);
            return;
        }

//        switchCommandApi.clickButton(SwitchButton.Button.DDOWN, globalClickInterval);
//        switchCommandApi.clickButton(SwitchButton.Button.A, globalClickInterval + 600);
        switchCommandApi.clickButton(SwitchButton.Button.PLUS, globalClickInterval);
        for (int i = 0; i < password.length(); i++) {
            System.out.print(password.substring(i, i + 1));
            Thread.sleep(400L);
            switchCommandApi.keyboardKey(KeyboardKey.valueOf("NumPad" + password.substring(i, i + 1)));
            Thread.sleep(400L);
        }
        switchCommandApi.clickButton(SwitchButton.Button.PLUS, globalClickInterval);
//        switchCommandApi.clickButton(SwitchButton.Button.DUP, globalClickInterval);
        switchCommandApi.clickButton(SwitchButton.Button.A, globalClickInterval);
        switchCommandApi.clickButton(SwitchButton.Button.A, globalClickInterval);
    }


    /***
     * 等待交易方连接，并获取对方NID
     * @param waitTimeSecond
     * @return
     * @throws IOException
     * @throws DecoderException
     * @throws InterruptedException
     */
    public long waitForTradePartnerNID(int waitTimeSecond) throws IOException, DecoderException, InterruptedException {

        ClearTradePartnerNID();
        long waitTime = 1000 * waitTimeSecond;
        long interval = 500;
        int intervalClickKey = 2;
        int tempIntervalClickKey = intervalClickKey;
        long nid = 0;
        System.out.print("开始匹配用户......");
        GlobalLogger.append("开始匹配用户->");
        do {
            try {
                if ((nid = GetTradePartnerNID()) != 0) {
                    System.out.print("匹配用户NID:" + nid + "->");
                    GlobalLogger.append("匹配用户NID:" + nid + "->");
                    return new BigInteger(new String(switchCommandApi.pointerAll(PokeDataOffsetsSV.LinkTradePartnerPokemonPointer)), 16).longValue();
                }
                Thread.sleep(interval);
//                if (tempIntervalClickKey-- < 0) {
                switchCommandApi.clickButton(SwitchButton.Button.A);
//                    tempIntervalClickKey = intervalClickKey;
//                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } while ((waitTime -= interval) > 0);
        return 0L;
//        throw new SwitchException("交易末能完成。");
    }

    public boolean waitForBoxOpen(int waitTimeSecond) throws IOException, DecoderException, InterruptedException {
        int time = 1000 * waitTimeSecond;
        Thread.sleep(5000);
        while (!IsInBox() && (time -= 500) > 0) {
            switchCommandApi.clickButton(SwitchButton.Button.A, globalClickInterval);
        }
        return IsInBox();
    }

    @Deprecated
    public void ExitTradeToPortal(boolean unexpected) throws DecoderException, IOException, InterruptedException {
        if (IsInPokePortal()) return;

        if (unexpected) System.out.println("界面异常，开始重置。");

        // Ensure we're not in the box first.
        // Takes a long time for the Portal to load up, so once we exit the box, wait 5 seconds.
        System.out.println("定位当前位置：");
        var attempts = 0;
        boolean isInBox = false;
        do {
            if (!(isInBox = IsInBox())) {
                Thread.sleep(8000);
                break;
            }
            switchCommandApi.clickButton(SwitchButton.Button.B, 1000);
            if (!(isInBox = IsInBox())) {
                Thread.sleep(5000);
                break;
            }

            switchCommandApi.clickButton(SwitchButton.Button.A, 1000);
            if (!(isInBox = IsInBox())) {
                Thread.sleep(5000);
                break;
            }
            switchCommandApi.clickButton(SwitchButton.Button.B, 1000);
            if (!(isInBox = IsInBox())) {
                Thread.sleep(5000);
                break;
            }
        } while (isInBox);
        //@TODO 优化在门户界面
        //退出野外
        recoverToOverworld();
    }

    public void restrart() throws IOException, InterruptedException {
        GlobalLogger.append("关闭程序");
        switchCommandApi.clickButton(SwitchButton.Button.HOME, 1000);
        switchCommandApi.clickButton(SwitchButton.Button.X, 1000);
        switchCommandApi.clickButton(SwitchButton.Button.A, 8000);
        switchCommandApi.clickButton(SwitchButton.Button.B, 4000);
        switchCommandApi.clickButton(SwitchButton.Button.A, 4000);
        switchCommandApi.clickButton(SwitchButton.Button.A, 30000);
        switchCommandApi.clickButton(SwitchButton.Button.A, 20000);
        GlobalLogger.appendln("程序载入成功");
    }

    public static void main(String[] args) throws DecoderException, IOException, InterruptedException {
        SwitchCommandApi api = new SwitchCommandApi("192.168.1.125", 6000);
//        System.out.println(new SwitchService(api).IsConnectedOnline());
        SwitchService a = new SwitchService(api);
        api.SetScreen(ScreenState.On);
        a.passCode("5945");
//        new SwitchService(api).trade();
//        System.out.println("完成1");
//        BigInteger a = new BigInteger("E7EE7E148", 16);
//        long a1 = Long.parseLong("0xE7EE7E148");

    }
}
