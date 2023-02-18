package com.github.cheukbinli.core.ns;

import com.alibaba.fastjson2.JSON;
import com.github.cheukbinli.core.GlobalLogger;
import com.github.cheukbinli.core.ns.model.CommandModel;
import com.github.cheukbinli.core.ns.model.request.DecodeTrainerPartnerRequest;
import com.github.cheukbinli.core.ns.model.request.DecodeTrainerRequest;
import com.github.cheukbinli.core.ns.model.request.GeneratePokemonRequest;
import com.github.cheukbinli.core.ns.model.request.PKMValidityVerificationRequest;
import com.github.cheukbinli.core.ns.model.response.*;
import com.github.cheukbinli.core.util.NetUtil;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

import static com.github.cheukbinli.core.ns.model.request.GeneratePokemonRequest.Param.FILE_DATATYPE;
import static com.github.cheukbinli.core.ns.model.request.GeneratePokemonRequest.Param.TXT_DATATYPE;

@RequiredArgsConstructor
public class SysBotApi extends BufferPoolUtil {

    SocketChannel socketChannel;
    private final String ip;
    private final int port;
    ByteBuffer byteBuffer = ByteBuffer.allocate(51200);
    private String rowSplitChar = "+";
    private String columnSplitChar = " ";
    private Map<String, Integer> pkmSpecies;
    private List<String> pkmSpeciesList;

    public SocketChannel getSocketChannel() throws IOException {
        if (null == socketChannel) {
            synchronized (this) {
                if (null == socketChannel) {
                    this.socketChannel = connection();
                    System.out.println("sysbot初始化成功");
                    GlobalLogger.appendln("sysbot初始化成功");
                }
            }
        }
        return socketChannel;
    }

    public SocketChannel connection() throws IOException {
        return connection(ip, port);
    }

    public SocketChannel connection(String ip, int port) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(ip, port));
        socketChannel.configureBlocking(true);
        return socketChannel;
    }

    public void send(SocketChannel channel, byte[] data) throws IOException {
        synchronized (byteBuffer) {
            byteBuffer.rewind();
            byteBuffer.put("@#".getBytes());
            byteBuffer.put(NetUtil.littleEndianIntToByteArray(data.length));
            byteBuffer.put(data);
            channel.write(byteBuffer.slice(0, byteBuffer.position()));
            byteBuffer.rewind();
        }
    }

    public <T extends CommandModel> T receive(SocketChannel channel, T t) throws IOException {
        synchronized (byteBuffer) {
            try {
                byteBuffer.rewind();
                int len = channel.read(byteBuffer);
                byte[] responseBytes = new byte[len];
                byteBuffer.get(0, responseBytes);
                return (T) JSON.parseObject(responseBytes, t.getClass());
            } finally {
                byteBuffer.rewind();
            }
        }
    }

    public void send(SocketChannel channel, String data) throws IOException {
        send(channel, data.getBytes("UTF-8"));
    }

    public void send(SocketChannel channel, Object obj) throws IOException {
        send(channel, JSON.toJSONBytes(obj));
    }

    public DecodeTrainerResponse decodeFakeTrainerSAV(byte[] data) throws IOException {
        String requestData = Base64.getEncoder().encodeToString(data);
        DecodeTrainerRequest commandModel = new DecodeTrainerRequest();
        commandModel.setParam(new DecodeTrainerRequest.Param(requestData)).setCommand("DecodeFakeTrainerSAV");
        send(getSocketChannel(), commandModel);
        return receive(getSocketChannel(), new DecodeTrainerResponse());
    }

    public DecodeTradePartnerResponse getTradePartnerInfo(List<byte[]> data) throws IOException {
        List<String> dataList = new ArrayList<>();
        for (byte[] b : data) {
            dataList.add(Base64.getEncoder().encodeToString(b));
        }
        DecodeTrainerPartnerRequest commandModel = new DecodeTrainerPartnerRequest();
        commandModel.setParam(new DecodeTrainerPartnerRequest.Param(dataList)).setCommand("DecodeTradePartnerMyStatus");
        send(getSocketChannel(), commandModel);
        return receive(getSocketChannel(), new DecodeTradePartnerResponse());
    }

    public LoadResourceResponse loadResource() throws IOException {
        DecodeTrainerPartnerRequest commandModel = new DecodeTrainerPartnerRequest();
        commandModel.setCommand("LoadResource");
        send(getSocketChannel(), commandModel);
        return receive(getSocketChannel(), new LoadResourceResponse());
    }

    /***
     * 翻译+清除没用参数
     * @param content
     * @param rowSplitChar
     * @param columnSplitChar
     * @return
     */
    private List<String> converAndSplitContent(String content, String rowSplitChar, String columnSplitChar, int pkmLimit) {
        if (content.contains(":")) {
            return Arrays.asList(Base64.getEncoder().encodeToString(content.getBytes()));
        }
        StringTokenizer rowTokenizer = new StringTokenizer(content, rowSplitChar);
        List<String> result = new ArrayList<>();
        while (rowTokenizer.hasMoreTokens()) {
            String rowContent = rowTokenizer.nextToken();
            StringBuilder converRowContent = new StringBuilder();

            StringTokenizer columnTokenizer = new StringTokenizer(rowContent, columnSplitChar);
            while (columnTokenizer.hasMoreTokens()) {
                String columnContent = columnTokenizer.nextToken();
                //@TODO 核心接口实现
                //中转英
                //清除没用参数
                converRowContent.append(" ").append(columnContent.trim());
            }
            result.add(Base64.getEncoder().encodeToString(converRowContent.substring(1).getBytes()));
            //限制PKM数量
            if (result.size() >= pkmLimit) {
                break;
            }
        }
        return result;
    }

    /***
     * 门面方法
     * @param data
     * @param additional
     * @return
     * @throws IOException
     */
    private GeneratePokemonResponse generatePokemon(List<String> data, String dataType, Map<String, Object> additional) throws IOException {
        GeneratePokemonRequest commandModel = new GeneratePokemonRequest();
        commandModel.setParam(new GeneratePokemonRequest.Param().setData(data).setDataType(dataType).setAdditional(additional)).setCommand("GeneratePokemon");
        send(getSocketChannel(), commandModel);
        return receive(getSocketChannel(), new GeneratePokemonResponse());
    }

    public GeneratePokemonResponse generatePokemon(String content, Map<String, Object> additional, int pkmLimit) throws IOException {
        return generatePokemon(converAndSplitContent(content, rowSplitChar, columnSplitChar, pkmLimit), TXT_DATATYPE, additional);
    }

    public GeneratePokemonResponse generatePokemonByFile(List<byte[]> files, Map<String, Object> additional) throws IOException {
        List<String> dataList = new ArrayList<>(files.size());
        for (byte[] data : files) {
            dataList.add(Base64.getEncoder().encodeToString(data));
        }
        return generatePokemon(dataList, FILE_DATATYPE, additional);
    }

    /***
     * PKM合法性校验
     * @return
     * @param pkmData SLOT数据
     * @throws IOException
     */
    public PKMValidityVerificationResponse pkmValidityVerification(List<byte[]> pkmData) throws IOException {

        PKMValidityVerificationRequest commandModel = new PKMValidityVerificationRequest();
        List<String> dataList = new ArrayList<>(pkmData.size());
        for (byte[] data : pkmData) {
            dataList.add(Base64.getEncoder().encodeToString(data));
        }
        commandModel.setParam(new PKMValidityVerificationRequest.Param().setData(dataList).setAdditional(null)).setCommand("PKMValidityVerification");
        send(getSocketChannel(), commandModel);
        return receive(getSocketChannel(), new PKMValidityVerificationResponse());
    }

    private void speciesInit() throws IOException {
        if (null == pkmSpecies) {
            synchronized (this) {
                if (null == pkmSpecies) {
                    LoadResourceResponse response = loadResource();
                    pkmSpecies = new HashMap<>(response.getData().size());
                    pkmSpeciesList = new ArrayList<>();
                    String temp = null;
                    for (int i = 0; i < response.getData().size(); i++) {
                        pkmSpecies.put(temp = response.getData().get(i), i);
                        pkmSpeciesList.add(temp);
                    }
                }
            }
        }
    }

    /***
     * PKM名称提取
     * @param species
     * @return
     * @throws IOException
     */
    public String pkmNameExtraction(List<Integer> species) throws IOException {
        speciesInit();
        StringBuilder sb = new StringBuilder();
        species.forEach(i -> {
            sb.append(" ").append(pkmSpeciesList.get(i));
        });
        return sb.substring(1);
    }

    public String pkmNameExtraction(String name) throws IOException {
        speciesInit();
        StringBuilder sb = new StringBuilder();
        pkmSpecies.forEach((k, v) -> {
            if (name.contains(k)) {
                sb.append(" ").append(k);
            }
        });
        return sb.length() > 0 ? sb.substring(1) : "";
    }

    public static void main(String[] args) throws IOException {

        System.out.println("海豚侠 闪光 6V 全技能 全奖章 高级球".contains("闪光"));

        SysBotApi api = new SysBotApi("127.0.0.1", 1111);
        Object o = api.loadResource();
        Object o1 = api.generatePokemon("呆壳兽", null, 5);

        System.out.println(api.pkmNameExtraction("海豚侠 闪光 6V 全技能 全奖章 高级球"));

        System.out.println(1);
    }

}
