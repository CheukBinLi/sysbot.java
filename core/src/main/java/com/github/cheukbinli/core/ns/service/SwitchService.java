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
        System.out.println("Switch?????????");
        GlobalLogger.appendln("Switch???????????????");
    }

    /***
     * PKM????????????
     * @param pkm
     * @throws IOException
     * @throws InterruptedException
     */
    public void pushPKM(byte[] pkm, int slot) throws IOException, InterruptedException {
        switchCommandApi.pushPKM(pkm, BoxStartOffset + ((slot > 0 ? --slot : slot) * BOX_SLOT_SIZE));
    }

    /***
     * PKM????????????
     * @param
     * @throws IOException
     * @throws InterruptedException
     */
    public byte[] pullPKM(int slot, int size) throws IOException, InterruptedException, DecoderException {
        byte[] data = switchCommandApi.peekAbsolute(size, BoxStartOffset + ((slot > 0 ? --slot : slot) * size));
        return Hex.decodeHex(new String(data));
    }

    //???????????????WEB??????
    // Usually 0x9-0xA if fully loaded into Pok?? Portal.
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

    //????????????
    public int GetTextSpeed() throws IOException {
        var data = switchCommandApi.pointerPeek(1, PokeDataOffsetsSV.ConfigPointer);
        return data[0] & 3;
    }

    //    ????????????BOX????????????1??? box=0
    public void SetCurrentBox(byte box) throws IOException {
        switchCommandApi.pointerPoke(new byte[]{box}, PokeDataOffsetsSV.CurrentBoxPointer);
    }

    //  ????????????
    public boolean IsConnectedOnline() throws IOException, DecoderException {
        var data = switchCommandApi.peekAbsolute(1, ConnectedOffset);
        data = Hex.decodeHex(new String(data));
        return data[0] == 1;
    }

    //    //???????????????NID
    public long GetTradePartnerNID() throws IOException, DecoderException {
        byte[] data = switchCommandApi.peekAbsolute(8, TradePartnerNIDOffset);
        return new BigInteger(new String(data), 16).longValue();
    }

    //??????
    public void ClearTradePartnerNID() throws IOException {
        switchCommandApi.pokeAbsolute(new byte[8], TradePartnerNIDOffset);
    }

    //????????????
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
     * ?????????????????????
     * @return
     * @throws IOException
     */
    public byte[] GetFakeTrainerSAV() throws IOException {
        return switchCommandApi.pointerPeek(PokeDataOffsetsSV.FAKE_TRAINER_SAV_INFO_LENGTH, PokeDataOffsetsSV.MyStatusPointer);
    }

    /***
     * ??????????????????????????????
     * @return
     * @param first  0-1
     * @throws IOException
     */
    public byte[] GetTradePartnerInfo(boolean first) throws IOException {
        return switchCommandApi.pointerPeek(PokeDataOffsetsSV.TradeMyStatusInfoLength, first ? PokeDataOffsetsSV.Trader1MyStatusPointer : PokeDataOffsetsSV.Trader2MyStatusPointer);
    }

    /***
     *
     * @param slot ????????????
     * @param referenceData ????????????
     * @param watchTimeSC ??????????????????
     * @param intervalMs ?????????????????????
     * @param loopAction -IN ?????????????????????(??????)
     * @param loopAction -OUT ??????????????????(??????)
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
                System.out.println("????????????");
                GlobalLogger.appendln("????????????");
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
     * ??????????????????
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
            System.out.print("?????????" + count + "??????  ");
            GlobalLogger.append("?????????" + count + "??????  ");
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
     * ??????
     * @throws IOException
     * @throws DecoderException
     * @throws InterruptedException
     */
    public void tryOnline() throws IOException, DecoderException, InterruptedException {
        if (!IsConnectedOnline()) {
            System.out.println("??????????????????->");
            GlobalLogger.append("??????????????????->");
            recoverToOverworld();
            for (int i = 0; i < 3; i++) {
                if (IsOnOverworld()) {
                    System.out.print("????????????->");
                    GlobalLogger.append("????????????->");
                    switchCommandApi.clickButton(SwitchButton.Button.X, globalClickInterval);
                    switchCommandApi.clickButton(SwitchButton.Button.L, globalClickInterval);
                    switchCommandApi.clickButton(SwitchButton.Button.L);
                    for (int j = 0; j < 60; j++) {
                        Thread.sleep(globalClickInterval);
                        if (IsConnectedOnline()) {
                            System.out.print("????????????:" + IsConnectedOnline());
                            GlobalLogger.append("????????????:" + IsConnectedOnline());
                            Thread.sleep(globalClickInterval * 5);
                            System.out.print("->????????????\n");
                            GlobalLogger.appendln("->????????????");
                            recoverToOverworld();
                            return;
                        }
                    }
                }

            }
        }
        if (!IsConnectedOnline()) {
            GlobalLogger.appendln("???????????????");
            System.out.println("???????????????");
        }
    }

    /***
     * ????????????
     * @throws IOException
     * @throws InterruptedException
     * @throws DecoderException
     */
    public void enterPokePortal() throws IOException, InterruptedException, DecoderException {

        for (int i = 0; i < 5; i++) {
            recoverToOverworld();
            Thread.sleep(1000L);
            System.out.println("X??????-> ??????->");
            switchCommandApi.clickButton(SwitchButton.Button.X, globalClickInterval);
            switchCommandApi.clickButton(SwitchButton.Button.DLEFT, globalClickInterval);
            switchCommandApi.clickButton(SwitchButton.Button.DRIGHT, globalClickInterval);
            switchCommandApi.clickButton(SwitchButton.Button.DDOWN, globalClickInterval);
            switchCommandApi.clickButton(SwitchButton.Button.DDOWN, globalClickInterval);
            switchCommandApi.clickButton(SwitchButton.Button.A, globalClickInterval);
            Thread.sleep(globalClickInterval * 7);
            if (IsInPokePortal()) {
                System.out.println("??????pkm??????");
                GlobalLogger.append("??????pkm??????->");
                return;
            }
        }
        GlobalLogger.appendln("??????pkm???????????????");
        throw new SwitchException("??????pkm???????????????");
    }

    /***
     * ??????????????????
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

        System.out.println("????????????");
        GlobalLogger.append("????????????->");
        switchCommandApi.clickButton(SwitchButton.Button.X, globalClickInterval);

        if (null == password || password.length() < 1) {
            //?????????
            switchCommandApi.clickButton(SwitchButton.Button.A, globalClickInterval);
            //?????????
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
     * ???????????????????????????????????????NID
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
        System.out.print("??????????????????......");
        GlobalLogger.append("??????????????????->");
        do {
            try {
                if ((nid = GetTradePartnerNID()) != 0) {
                    System.out.print("????????????NID:" + nid + "->");
                    GlobalLogger.append("????????????NID:" + nid + "->");
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
//        throw new SwitchException("?????????????????????");
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

        if (unexpected) System.out.println("??????????????????????????????");

        // Ensure we're not in the box first.
        // Takes a long time for the Portal to load up, so once we exit the box, wait 5 seconds.
        System.out.println("?????????????????????");
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
        //@TODO ?????????????????????
        //????????????
        recoverToOverworld();
    }

    public void restrart() throws IOException, InterruptedException {
        GlobalLogger.append("????????????");
        switchCommandApi.clickButton(SwitchButton.Button.HOME, 1000);
        switchCommandApi.clickButton(SwitchButton.Button.X, 1000);
        switchCommandApi.clickButton(SwitchButton.Button.A, 8000);
        switchCommandApi.clickButton(SwitchButton.Button.B, 4000);
        switchCommandApi.clickButton(SwitchButton.Button.A, 4000);
        switchCommandApi.clickButton(SwitchButton.Button.A, 30000);
        switchCommandApi.clickButton(SwitchButton.Button.A, 20000);
        GlobalLogger.appendln("??????????????????");
    }

    public static void main(String[] args) throws DecoderException, IOException, InterruptedException {
//        SwitchCommandApi api = new SwitchCommandApi("192.168.1.125", 6000);
        SwitchCommandApi api = new SwitchCommandApi("192.168.50.220", 6000);
//        System.out.println(new SwitchService(api).IsConnectedOnline());
        SwitchService a = new SwitchService(api);
        api.SetScreen(ScreenState.On);
        a.passCode("5945");
//        new SwitchService(api).trade();
//        System.out.println("??????1");
//        BigInteger a = new BigInteger("E7EE7E148", 16);
//        long a1 = Long.parseLong("0xE7EE7E148");

    }
}
