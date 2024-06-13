package pt.up.fc.se.petfeeder;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
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
        Request request = new Request.Builder().url(bowlsListUrl).build();

        final BlockingQueue<JSONArray> blockingQueue = new ArrayBlockingQueue<>(1);

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        // Handle unsuccessful response
                        blockingQueue.add(new JSONArray());
                        return;
                    }

                    assert response.body() != null;
                    String responseString = response.body().string();
                    JSONObject responseBody = null;

                    responseBody = new JSONObject(responseString);
                    JSONArray bowlsArray = responseBody.getJSONArray("bowls");
                    blockingQueue.add(bowlsArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                    blockingQueue.add(new JSONArray());  // return an empty JSON array or handle as needed
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                // Handle the failure by adding a default or error value to the queue
                try {
                    blockingQueue.put(new JSONArray());  // return an empty JSON array or handle as needed
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        return blockingQueue;
    }

    // current dosage
    public BlockingQueue<Integer> getFoodAmount(String bowlName) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(base_url + "/get_food_amount")).newBuilder();
        urlBuilder.addQueryParameter("bowl_name", bowlName);
        String foodAmountUrl = urlBuilder.build().toString();

        Request request = new Request.Builder().url(foodAmountUrl).build();

        final BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(1);

        Call call = client.newCall(request);
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

    public BlockingQueue<Integer> getDailyGoal(String bowlName) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(base_url + "/get_daily_goal")).newBuilder();
        urlBuilder.addQueryParameter("bowl_name", bowlName);
        String dailyGoalUrl = urlBuilder.build().toString();
        Request request = new Request.Builder().url(dailyGoalUrl).build();

        final BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(1);

        Call call = client.newCall(request);
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

    public BlockingQueue<String> getLastFeedingTime(String bowlName) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(base_url + "/get_last_feeding_time")).newBuilder();
        urlBuilder.addQueryParameter("bowl_name", bowlName);
        String lastFeedingUrl = urlBuilder.build().toString();
        Request request = new Request.Builder().url(lastFeedingUrl).build();

        final BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(1);

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String responseString = response.body().string();
                JSONObject responseBody = null;
                try {
                    responseBody = new JSONObject(responseString);
                    String lastFeedingTime = responseBody.getString("last_feeding_time");
                    blockingQueue.add(lastFeedingTime);
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

    public BlockingQueue<Integer> getFoodPoured(String bowlName) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(base_url + "/get_bowl_weight")).newBuilder();
        urlBuilder.addQueryParameter("bowl_name", bowlName);
        String foodPouredUrl = urlBuilder.build().toString();

        Request request = new Request.Builder().url(foodPouredUrl).build();

        final BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(1);

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
                String responseString = response.body().string();
                JSONObject responseBody = null;
                try {
                    responseBody = new JSONObject(responseString);
                    int bowlWeight = responseBody.getInt("bowl_weight");
                    blockingQueue.add(bowlWeight);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return blockingQueue;
    }

    public BlockingQueue<Integer> getUndefinedBowlsCount() {
        String undefinedCountUrl = base_url + "/get_undefined_count";
        Request request = new Request.Builder().url(undefinedCountUrl).build();

        final BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(1);

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String responseString = response.body().string();
                JSONObject responseBody = null;
                try {
                    responseBody = new JSONObject(responseString);
                    int count = responseBody.getInt("count");
                    blockingQueue.add(count);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                call.cancel();
            }
        });

        return blockingQueue;
    }

    // -- POSTS --

    public void postAddBowl(String bowlName, String dailyGoal, String userID) {
        String addBowlUrl = base_url + "/add_bowl";
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", userID)
                .add("bowl_name", bowlName)
                .add("daily_goal", dailyGoal)
                .build();
        Request request = new Request.Builder().url(addBowlUrl).post(formBody).build();

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

    public BlockingQueue<Integer> postDailyGoal(String bowlName, String dailyGoal) {
        String dailyGoalUrl = base_url + "/set_daily_goal";
        RequestBody formBody = new FormBody.Builder()
                .add("bowl_name", bowlName)
                .add("daily_goal", dailyGoal)
                .build();
        Request request = new Request.Builder().url(dailyGoalUrl).post(formBody).build();

        final BlockingQueue<Integer> blockingQueue = new ArrayBlockingQueue<>(1);

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
                String responseString = response.body().string();
                JSONObject responseBody = null;
                try {
                    responseBody = new JSONObject(responseString);
                    int dailyGoal = responseBody.getInt("new_daily_goal");
                    blockingQueue.add(dailyGoal);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return blockingQueue;
    }

    public void postLastFeedingTime(String bowlName, String time) {
        String feedingTimeUrl = base_url + "/set_feeding_time";
        RequestBody formBody = new FormBody.Builder()
                .add("bowl_name", bowlName)
                .add("feeding_time", time)
                .build();
        Request request = new Request.Builder().url(feedingTimeUrl).post(formBody).build();

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

    // -- OTHER --

    public void changeMotorState(String bowlName, String state) {
        String motorStateUrl = base_url + "/control_motor";
        RequestBody formBody = new FormBody.Builder()
                .add("bowl_name", bowlName)
                .add("activate_motor", state)
                .build();
        Request request = new Request.Builder().url(motorStateUrl).post(formBody).build();

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

    public void resetBowl(String bowlName) {
        String resetBowlUrl = base_url + "/reset_bowl";
        RequestBody formBody = new FormBody.Builder()
                .add("bowl_name", bowlName)
                .build();
        Request request = new Request.Builder().url(resetBowlUrl).post(formBody).build();

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
