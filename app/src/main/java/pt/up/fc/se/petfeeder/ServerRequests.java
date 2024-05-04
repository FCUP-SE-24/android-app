package pt.up.fc.se.petfeeder;

import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServerRequests {
    OkHttpClient client = new OkHttpClient();
    String base_url = "http://46.101.71.117:5000";

    // -- GETS --
    public BlockingQueue<JSONArray> getBowlsList() {
        String bowlsListUrl = base_url + "/get_bowls_list";
        Request bowlsListRequest = new Request.Builder().url(bowlsListUrl).build();

        final BlockingQueue<JSONArray> blockingQueue = new ArrayBlockingQueue<>(1);

        Call call = client.newCall(bowlsListRequest);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String responseString = response.body().string();
                JSONObject responseBody = null;
                try {
                    responseBody = new JSONObject(responseString);
                    JSONArray bowlsArray = responseBody.getJSONArray("bowls");
                    blockingQueue.add(bowlsArray);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        });

        return blockingQueue;
    }

    public BlockingQueue<Integer> getFoodAmount(String bowlName) {
        // TODO: send bowlName to server as param
        String foodAmountUrl = base_url + "/get_food_amount";
        Request bowlsListRequest = new Request.Builder().url(foodAmountUrl).build();

        final BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(1);

        Call call = client.newCall(bowlsListRequest);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String responseString = response.body().string();
                JSONObject responseBody = null;
                try {
                    responseBody = new JSONObject(responseString);
                    int foodAmount = responseBody.getInt("food_amount");
                    blockingQueue.add(foodAmount);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        });

        return blockingQueue;
    }

    // TODO: add this request to server
    public BlockingQueue<Integer> getDailyGoal(String bowlName) {
        // TODO: send bowlName to server as param
        String dailyGoalUrl = base_url + "/get_daily_goal";
        Request bowlsListRequest = new Request.Builder().url(dailyGoalUrl).build();

        final BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(1);

        Call call = client.newCall(bowlsListRequest);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String responseString = response.body().string();
                JSONObject responseBody = null;
                try {
                    responseBody = new JSONObject(responseString);
                    int dailyGoal = responseBody.getInt("daily_goal");
                    blockingQueue.add(dailyGoal);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
        });

        return blockingQueue;
    }

    // -- POSTS --

    public void postAddBowl(String bowlName) {
        String addBowlUrl = base_url + "/add_bowl";
        String postBody = "{\n" +
                "   \"bowl_name\": \"" + bowlName + "\"\n" +
                "}";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, postBody);
        Request request = new Request.Builder().url(addBowlUrl).post(body).build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                call.cancel();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                Log.d("TAG", response.body().string());
            }
        });
    }


    public void postDailyGoal(String bowlName, String dailyGoal) {
        String addBowlUrl = base_url + "/set_daily_goal";
        String postBody = "{\n" +
                "   \"bowl_name\": \"" + bowlName + "\",\n" +
                "   \"daily_goal\": \"" + dailyGoal + "\"\n" +
                "}";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, postBody);
        Request request = new Request.Builder().url(addBowlUrl).post(body).build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                call.cancel();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                Log.d("TAG", response.body().string());
            }
        });
    }
}
