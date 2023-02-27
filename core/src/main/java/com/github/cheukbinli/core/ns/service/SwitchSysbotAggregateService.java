package com.github.cheukbinli.core.ns.service;

import com.github.cheukbinli.core.GlobalLogger;
import com.github.cheukbinli.core.model.FunctionResult;
import com.github.cheukbinli.core.model.TradeElementModel;
import com.github.cheukbinli.core.ns.model.response.DecodeTradePartnerResponse;
import com.github.cheukbinli.core.ns.model.response.DecodeTrainerResponse;
import com.github.cheukbinli.core.ns.model.response.GeneratePokemonResponse;
import com.github.cheukbinli.core.ns.SwitchCommandApi;
import com.github.cheukbinli.core.ns.SysBotApi;
import com.github.cheukbinli.core.ns.constant.SwitchButton;
import com.github.cheukbinli.core.util.MessageTemplate;
import lombok.Getter;
import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.github.cheukbinli.core.ns.constant.PokeDataOffsetsSV.BOX_SLOT_SIZE;

public class SwitchSysbotAggregateService {

    private final String switchIp;
    private final int switchPort;
    private final String sysbotIp;
    private final int sysbotPort;
    //    private final String switchIp = "192.168.1.125";
//    private final int switchPort = 6000;
//    private final String sysbotIp = "127.0.0.1";
//    private final int sysbotPort = 1111;
    @Getter
    SwitchCommandApi switchCommandApi;
    @Getter
    SwitchService switchService;
    @Getter
    SysBotApi sysBotApi;
    @Getter
    private DecodeTrainerResponse.Data trainerInfo;
    private Map<String, Object> defaultAdditional = new HashMap<>();

    public SwitchSysbotAggregateService(String switchIp, int switchPort, String sysbotIp, int sysbotPort) {
        this.switchIp = switchIp;
        this.switchPort = switchPort;
        this.sysbotIp = sysbotIp;
        this.sysbotPort = sysbotPort;
        this.switchCommandApi = new SwitchCommandApi(switchIp, switchPort);
        this.switchService = new SwitchService(switchCommandApi);
        this.sysBotApi = new SysBotApi(sysbotIp, sysbotPort);
        try {
            trainerInfo = getMyTrainer().getData();
            defaultAdditional.put("GenerateOT", trainerInfo.getGenerateOT());
            defaultAdditional.put("DisplayTID", trainerInfo.getDisplayTID());
            defaultAdditional.put("DisplaySID", trainerInfo.getDisplaySID());
        } catch (Exception e) {
            GlobalLogger.append(e);
            throw new RuntimeException(e);
        }
        GlobalLogger.appendln("SwitchSysbotAggregateService初始化成功。");
    }

    private final Map<String, Object> Cache = new ConcurrentHashMap<>();

    public <T> T getCache(String key) {
        Object obj = Cache.get(key);
        return null == obj ? null : (T) obj;
    }

    public void putCache(String key, Object obj) {
        Cache.put(key, obj);
    }

    /***
     * 获取本机训练家
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws DecoderException
     */
    public DecodeTrainerResponse getMyTrainer() throws IOException, InterruptedException, DecoderException {
        DecodeTrainerResponse trainer = getCache("getMytrade");
        if (null == trainer || null == trainer.getData() || null == trainer.getData()) {
            trainer = sysBotApi.decodeFakeTrainerSAV(switchService.GetFakeTrainerSAV());
            if (null == trainer || null == trainer.getData()) {
                throw new IOException("末能读取训练家信息");
            }
            putCache("getMytrade", trainer);
        }
        return trainer;
    }

    /***
     * 获取交易训练家信息
     * @throws IOException
     * @throws InterruptedException
     * @throws DecoderException
     */
    public DecodeTradePartnerResponse getTradePartnerInfo() throws IOException, InterruptedException, DecoderException {
        List<byte[]> trainers = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            trainers.add(switchService.GetTradePartnerInfo(i > 0));
        }
        return sysBotApi.getTradePartnerInfo(trainers);
    }

    /***
     * 生成宝可梦
     * @throws IOException
     * @throws InterruptedException
     * @throws DecoderException
     */
    public GeneratePokemonResponse generatePokemon(String content, Map<String, Object> additional) throws IOException, InterruptedException, DecoderException {
        return sysBotApi.generatePokemon(content, additional, 1);
    }

    public GeneratePokemonResponse generatePokemon(String content, Map<String, Object> additional, int pkmLimit) throws IOException, InterruptedException, DecoderException {
        return sysBotApi.generatePokemon(content, additional, pkmLimit);
    }

    /***
     * 生成宝可梦
     * @throws IOException
     * @throws InterruptedException
     * @throws DecoderException
     */
    public GeneratePokemonResponse generatePokemon(List<byte[]> file, Map<String, Object> additional) throws IOException, InterruptedException, DecoderException {
        return sysBotApi.generatePokemonByFile(file, additional);
    }

    public void trade() throws DecoderException, IOException, InterruptedException {
        trade((TradeElementModel) new TradeElementModel().setData(new TradeElementModel.Data("", "", "振翼发 6v 闪光 梦境球 大个子之证 全奖章 全技能 + 甲贺忍蛙 闪光 6V 全技能 小个子 终极球+喵头目 闪光 6V 全技能 小个子 终极球+赛富豪 6V 全技能 大个子 终极球 + 呆壳兽 闪光 6V 全技能 大个子 终极球", null, 1, 1, null)), null, null);
    }

    public void trade(final TradeElementModel tradeElementModel, Function<TradeElementModel, String> noticeFunction, Function<DecodeTrainerResponse.Data, FunctionResult<Boolean>> verification) throws DecoderException, IOException, InterruptedException {

        Map<String, Object> additional = defaultAdditional;
        DecodeTrainerResponse trainerResponse = getMyTrainer();
        List<byte[]> pkmList = new ArrayList<>();
        String pkmContent = tradeElementModel.getData().getContent();
        String randomCode = tradeElementModel.getData().getRandomCode();

        //连线->>选菜单->>清空密语
        switchService.selectTrademenu();
        //输入蜜语
        switchService.passCode(randomCode);

        noticeFunction.apply((TradeElementModel) tradeElementModel.setOperationMessage(
                MessageTemplate.messageTemplate(tradeElementModel.getIdentity(), tradeElementModel.getUserName(), tradeElementModel.getData().getPkmName(), trainerInfo.getGenerateOT(), null, 0, null, MessageTemplate.MessageTemplateStatus.CONNECTION)
        ));

        long partnerOffset = switchService.waitForTradePartnerNID(120);
        System.out.println("交易箱OFFSET:" + partnerOffset);
        GlobalLogger.appendln("交易箱OFFSET:" + partnerOffset);
        if (partnerOffset < 1) {
//            throw new SwitchServiceException("匹配用户失败。");
            noticeFunction.apply((TradeElementModel) tradeElementModel.setOperationMessage(
                    MessageTemplate.messageTemplate(tradeElementModel.getIdentity(), null, null, null, null, 0, "完成没有链接到对方", MessageTemplate.MessageTemplateStatus.FAIL)
            ));
            switchCommandApi.clickButton(SwitchButton.Button.B, 1000);
            switchCommandApi.clickButton(SwitchButton.Button.A, 1000);
            switchService.recoverToOverworld();
            return;
        }

        //获取训练家信息（自ID）
        DecodeTradePartnerResponse partnersResponse = getTradePartnerInfo();
        for (DecodeTradePartnerResponse.Data data : partnersResponse.getData()) {
            if (!(trainerResponse.getData().getGenerateOT().equals(data.getOT()) && trainerResponse.getData().getDisplaySID().equals(data.getDisplaySID()) && trainerResponse.getData().getDisplayTID().equals(data.getDisplayTID()))) {
                additional = new HashMap<>();
                additional.put("GenerateOT", data.getOT());
                additional.put("DisplayTID", data.getDisplayTID());
                additional.put("DisplaySID", data.getDisplaySID());
                GlobalLogger.appendln("连接成功。NID:" + switchService.GetTradePartnerNID() + " 客户:" + data.getOT() + " TID:" + data.getDisplayTID() + " SID:" + data.getDisplaySID());
                System.out.println("连接成功。NID:" + switchService.GetTradePartnerNID() + " 客户:" + data.getOT() + " TID:" + data.getDisplayTID() + " SID:" + data.getDisplaySID());
                //noticeFunction.apply((TradeElementModel) tradeElementModel.setOperationMessage("连接成功。NID:" + switchService.GetTradePartnerNID() + " 客户:" + data.getOT() + " TID:" + data.getDisplayTID() + " SID:" + data.getDisplaySID()));
                break;
            }
        }

        FunctionResult<Boolean> verificationResult = verification.apply(new DecodeTrainerResponse.Data().setNintendoId(switchService.GetTradePartnerNID()).setAdditional(tradeElementModel.getData().getAdditional()));
        if (!verificationResult.isSuccess()) {
            noticeFunction.apply((TradeElementModel) tradeElementModel.setOperationMessage(
                    MessageTemplate.messageTemplate(tradeElementModel.getIdentity(), null, null, null, null, 0, verificationResult.getMsg(), MessageTemplate.MessageTemplateStatus.ERROR_MSG))
            );
            return;
        }

        //@TODO 查询用户黑名单


        //生成PKM
        List<String> pkms =
                (
                        tradeElementModel.getData().isFile() ?
                                generatePokemon(Arrays.asList(tradeElementModel.getData().getDataStream().toByteArray()), additional)
                                :
                                generatePokemon(pkmContent, additional, tradeElementModel.getData().getPkmLimit())
                ).getData().getData();
        for (String pkmData : pkms) {
            pkmList.add(Base64.getDecoder().decode(pkmData));
        }
        //PKM写入slot-测试有，批量写入N格
//        for (int b = 0; b < pkmList.size(); b++) {
//            switchService.pushPKM(pkmList.get(b), b + 1);
//        }
        System.out.println(pkms.size() + "_" + pkmList.size());
        for (int p = 0; p < pkmList.size(); p++) {
            //只写入第一格
            switchService.pushPKM(pkmList.get(p), 1);
            switchService.pushPKM(pkmList.get(p), 2 + p);

            if (!switchService.waitForBoxOpen(60)) {
//                throw new SwitchServiceException("连接异常，无法进入交易箱。");
                System.out.println("连接异常，无法进入交易箱。");
                GlobalLogger.appendln("连接异常，无法进入交易箱。");
                noticeFunction.apply((TradeElementModel) tradeElementModel.setOperationMessage(
                        MessageTemplate.messageTemplate(tradeElementModel.getIdentity(), null, null, null, null, 0, "连接异常，无法进入交易箱。", MessageTemplate.MessageTemplateStatus.FAIL)
                ));
                return;
            }

            //等待用户输入
            //读取交换箱数据
            //读取交易箱PKM信息-->>校验合法性
            //        partnerOffset
            //Base64.getEncoder().encodeToString(Hex.decodeHex(new String(switchCommandApi.peekAbsolute(PokeDataOffsetsSV.BOX_SLOT_SIZE, partnerOffset))));
            //new PK9(data)
            //pk.Species != 0 && pk.ChecksumValid
            //通过返回在
            //失败继续等待用户切换新PKM

            byte[] slotOne = switchService.pullPKM(1, BOX_SLOT_SIZE);
            //直接交易
            switchCommandApi.clickButton(SwitchButton.Button.A);
            Thread.sleep(1000);
            //
            switchCommandApi.clickButton(SwitchButton.Button.A);
            Thread.sleep(1000);

            if (!switchService.monitorSlotChanges(1, slotOne, 60, 300, new Function<Long, Integer>() {
                @Override
                public Integer apply(Long s) {
                    try {
                        switchCommandApi.clickButton(SwitchButton.Button.A);
                        Thread.sleep(s);
                    } catch (Exception e) {
                        GlobalLogger.append(e);
                        e.printStackTrace();
                    }
                    return 1;
                }
            })) {
                System.out.println("交易失败，用户操作超时/结束交易。");
                GlobalLogger.appendln("交易失败，用户操作超时/结束交易。");
                noticeFunction.apply((TradeElementModel) tradeElementModel.setOperationMessage(
                        MessageTemplate.messageTemplate(tradeElementModel.getIdentity(), null, null, null, null, 0, "交易失败，用户操作超时/结束交易。", MessageTemplate.MessageTemplateStatus.FAIL)
                ));
//                noticeFunction.apply((TradeElementModel) tradeElementModel.setOperationMessage("交易失败，用户操作超时。"));
                break;
            }
            //校验
            System.out.println("交易完成，等待动画结束打开盒子。");
            GlobalLogger.appendln("交易完成，等待动画结束打开盒子。");
            switchService.waitForBoxOpen(50);
//            Thread.sleep(30000);
        }
        System.out.println("交易完成，退回野外。");
        GlobalLogger.appendln("交易完成，退回野外。");
//        Thread.sleep(10000);
        if (!switchService.IsInPokePortal()) {
            switchService.waitForBoxOpen(60);
            switchCommandApi.clickButton(SwitchButton.Button.B, 1000);
            switchCommandApi.clickButton(SwitchButton.Button.A, 1000);
        }
        switchService.recoverToOverworld(10);
        noticeFunction.apply((TradeElementModel) tradeElementModel.setOperationMessage(
                MessageTemplate.messageTemplate(tradeElementModel.getIdentity(), null, null, null, null, 0, null, MessageTemplate.MessageTemplateStatus.SUCCESS)
        ));
//        switchService.enterPokePortal();
    }

    public static void main(String[] args) throws DecoderException, IOException, InterruptedException {
        //    private final String switchIp = "192.168.1.125";
        //    private final int switchPort = 6000;
        //    private final String sysbotIp = "127.0.0.1";
        //    private final int sysbotPort = 1111;
        SwitchSysbotAggregateService service = new SwitchSysbotAggregateService("192.168.1.125", 6000, "127.0.0.1", 1111);

//        byte[] slotOne = service.switchService.pullPKM(1, 8);
//
//        boolean aaaaaaa;
//        if ((aaaaaaa = service.switchService.monitorSlotChanges(1, slotOne, 60, 300, new Function<Long, Integer>() {
//            @Override
//            public Integer apply(Long s) {
//                try {
////                    service.switchCommandApi.clickButton(SwitchButton.Button.A);
//                    System.out.println("1000");
//                    Thread.sleep(s);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//                return 1000;
//            }
//        }))) {
//            System.out.println("交易完成");
//        }
//        System.out.println("失败完成");
//        System.out.println(service.switchService.IsInBox());
//        System.out.println(service.switchService.waitForBoxOpen(10));
//        service.switchService.ClearTradePartnerNID();
//        service.generatePokemon("赛富豪 6V 全技能 大个子 终极球 + 润水鸭 6V 全技能 小个子 终极球");
//        DecodeTrainerPartnerResponse response = service.getTradePartnerInfo();

//        service.switchService.IsOnOverworld();
//        service.switchService.IsConnectedOnline();
//        service.switchService.ClearTradePartnerNID();
//        System.out.println(service.switchService.GetTradePartnerNID());
//        service.getMyTrainer();
//        service.switchCommandApi.SetScreen(ScreenState.Off);
//        service.trade();
//        service.switchService.enterPokePortal();
        DecodeTrainerResponse myStatus = service.getMyTrainer();
        Map<String, Object> additional = new HashMap<>();
        additional.put("GenerateOT", myStatus.getData().getGenerateOT());
        additional.put("GenerateOT", "伍六七");
        additional.put("DisplayTID", "800001");
        additional.put("DisplaySID", 249781);

//        GeneratePokemonResponse pokemon0 = service.generatePokemon("赛富豪 6V 全技能 大个子 大师球", null, 1);
        GeneratePokemonResponse pokemon0 = service.generatePokemon("百变怪 日文 6V 全技能 大师球 携带特性膏药+喷火龙  闪光 6V 全技能 全奖章 大师球 携带特性膏药", additional, 2);
        for (int i = 0; i < pokemon0.getData().getData().size(); i++) {
            service.switchService.pushPKM(Base64.getDecoder().decode(pokemon0.getData().getData().get(i)), 5 + i);
        }
//        GeneratePokemonResponse pokemon0 = service.generatePokemon("小火龙 5V0速 全技能 大个子 大师球 全奖章 公 固执", additional);
//        service.switchService.pushPKM(Base64.getDecoder().decode(pokemon0.getData().get(0)),10);
//        GeneratePokemonResponse pokemon = service.generatePokemon("固拉多 6V 闪光 全技能");
//        GeneratePokemonResponse pokemon = service.generatePokemon("梦幻");
//        GeneratePokemonResponse pokemon = service.generatePokemon("水箭龟");
//        GeneratePokemonResponse pokemon1 = service.generatePokemon("甲贺忍蛙 闪光 大师球 全奖章 固执 5V0速", additional);
//        service.switchService.pushPKM(Base64.getDecoder().decode(pokemon1.getData().get(0)), 3);
//        GeneratePokemonResponse pokemon2 = service.generatePokemon("小火龙 6V 闪光 全技能 小不点", additional);
//        service.switchService.pushPKM(Base64.getDecoder().decode(pokemon2.getData().get(0)),12);
//        GeneratePokemonResponse pokemon = service.generatePokemon("烈空坐 6V 闪光");g
//        byte[] pkm = Base64.getDecoder().decode(pokemon.getData().get(0));
//        service.switchService.pushPKM(pkm, 0);
//        System.out.println(pokemon.getError());
//        byte[] readPkm = service.switchService.pullPKM(3, 344);
//        service.switchService.pushPKM(pkm, 0);
//        service.sysBotApi.pkmValidityVerification(Arrays.asList(readPkm));
//        System.out.println(pkm.equals(readPkm));
        System.out.println(1);
//
//
//        DecodeTrainerResponse trainer = service.getMyTrainer();
//        DecodeTrainerPartnerResponse trainerPartner = service.getTradePartnerInfo();
//
//        service.switchService.IsInBox();
//        service.switchService.SetCurrentBox((byte) 0);
//
//        System.out.println(service.switchService.GetTradePartnerNID());
//        service.switchService.ClearTradePartnerNID();
//        System.out.println(service.switchService.GetTradePartnerNID());
//
//        System.out.println(1);
    }

}
