package com.github.cheukbinli.core.im.dodo;

import com.github.cheukbinli.core.GlobalLogger;
import com.github.cheukbinli.core.im.dodo.model.Authorization;
import com.github.cheukbinli.core.im.dodo.model.dto.ApiResponseModel;
import com.github.cheukbinli.core.im.dodo.model.dto.GetWebSocketConnection;
import com.github.cheukbinli.core.im.dodo.model.dto.request.GetChannelListRequest;
import com.github.cheukbinli.core.im.dodo.model.dto.request.SetChannelMessageSendRequest;
import com.github.cheukbinli.core.im.dodo.model.dto.request.SetPersonalMessageSendRequest;
import com.github.cheukbinli.core.im.dodo.model.dto.resopnse.GetBotInfoApiResponse;
import com.github.cheukbinli.core.im.dodo.model.dto.resopnse.GetChannelListResponse;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//@RequiredArgsConstructor
public class DodoApi {

    private final String domain;
    private final Authorization authorization;
    private DodoApiService dodoApiService = null;

    public DodoApi(String domain, Authorization authorization) {
        this.domain = domain;
        this.authorization = authorization;
    }

    public void init() {
        if (null == dodoApiService) {
            synchronized (this) {
                if (null == dodoApiService) {
                    System.out.println("Dodo API初始化");
                    GlobalLogger.appendln("Dodo API初始化");
                    //https://botopen.imdodo.com
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(domain)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    dodoApiService = retrofit.create(DodoApiService.class);
                }
            }
        }
    }

    public String getAuthorization(String client, String token) {
        return String.format("Bot %s.%s", client, token);
    }

    public String getAuthorization(Authorization authorization) {
        return getAuthorization(authorization.getClientId(), authorization.getToken());
    }

    public Map<String, String> getHeader(Authorization authorization) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", getAuthorization(authorization));
        return headers;
    }

    public DodoApiService getService() {
        return dodoApiService;
    }

    public String getWebSocketConnection() throws IOException {
        Call<GetWebSocketConnection> call = getService().GetWebSocketConnection(getHeader(authorization));
        Response<GetWebSocketConnection> response = call.execute();
        if (response.body().getStatus() != 0) {
            throw new IOException(response.body().getMessage());
        }
        return response.body().getData().getEndpoint();
    }

    public GetBotInfoApiResponse GetBotInfo() throws IOException {
        Call<GetBotInfoApiResponse> call = getService().GetBotInfo(getHeader(authorization));
        Response<GetBotInfoApiResponse> response = call.execute();
        if (response.body().getStatus() != 0) {
            throw new IOException(response.body().getMessage());
        }
        return response.body();
    }

    /***
     * 私信
     * @param request
     * @return
     * @throws IOException
     */
    public ApiResponseModel SetPersonalMessageSend(SetPersonalMessageSendRequest request) throws IOException {
        Call<ApiResponseModel> call = getService().SetPersonalMessageSend(getHeader(authorization), request);
        Response<ApiResponseModel> response = call.execute();
        if (response.body().getStatus() != 0) {
            throw new IOException(response.body().getMessage());
        }
        return response.body();
    }

    /***
     * 私信
     * @param request
     * @return
     * @throws IOException
     */
    public ApiResponseModel SetChannelMessageSend(SetChannelMessageSendRequest request) throws IOException {
        Call<ApiResponseModel> call = getService().SetChannelMessageSend(getHeader(authorization), request);
        Response<ApiResponseModel> response = call.execute();
        if (response.body().getStatus() != 0) {
            throw new IOException(response.body().getMessage());
        }
        return response.body();
    }

    /***
     * 私信
     * @param islandSourceId
     * @return
     * @throws IOException
     */
    public GetChannelListResponse GetChannelList(String islandSourceId) throws IOException {
        Call<GetChannelListResponse> call = getService().GetChannelList(getHeader(authorization), new GetChannelListRequest(islandSourceId));
        Response<GetChannelListResponse> response = call.execute();
        if (response.body().getStatus() != 0) {
            throw new IOException(response.body().getMessage());
        }
        return response.body();
    }

    public interface DodoApiService {

        /***
         * 获取WEBSOCKET 连接
         * @param headers
         * @return
         */
        @POST("/api/v2/websocket/connection")
        Call<GetWebSocketConnection> GetWebSocketConnection(@HeaderMap Map<String, String> headers);

        /***
         * 机器人信息
         * @param headers
         * @return
         */
        @POST("/api/v2/bot/info")
        Call<GetBotInfoApiResponse> GetBotInfo(@HeaderMap Map<String, String> headers);

        /***
         * 私信
         * @param headers
         * @param request
         * @return
         */
        @POST("/api/v2/personal/message/send")
        Call<ApiResponseModel> SetPersonalMessageSend(@HeaderMap Map<String, String> headers, @Body SetPersonalMessageSendRequest request);

        /***
         * 私信
         * @param headers
         * @param request
         * @return
         */
        @POST("/api/v2/channel/message/send")
        Call<ApiResponseModel> SetChannelMessageSend(@HeaderMap Map<String, String> headers, @Body SetChannelMessageSendRequest request);

        /***
         * 私信
         * @param headers
         * @param request
         * @return
         */
        @POST("/api/v2/channel/list")
        Call<GetChannelListResponse> GetChannelList(@HeaderMap Map<String, String> headers, @Body GetChannelListRequest request);

    }
}
