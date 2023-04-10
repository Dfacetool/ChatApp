package com.example.chatapp;


import static com.example.chatapp.ChatGptService.defaultClient;
import static com.example.chatapp.ChatGptService.defaultObjectMapper;
import static com.example.chatapp.ChatGptService.defaultRetrofit;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LruCache;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import android.content.SharedPreferences;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
//import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
//import com.theokanning.openai.service.OpenAiService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText editTextMessage;
    private Button buttonSend;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_clear_history:
                messages.clear();
                chatAdapter.notifyDataSetChanged();
                saveMessages(messages);
                Toast.makeText(MainActivity.this, "Clearing chat history...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_screenshot:
                Bitmap screenshot = getScreenshotFromRecyclerView(recyclerView,chatAdapter);
                if (screenshot != null) {
                    // 保存截图到设备存储
                    String savedImagePath = saveScreenshotToGallery(MainActivity.this, screenshot);
                    if (savedImagePath != null) {
                        Toast.makeText(MainActivity.this, "Screenshot saved to " + savedImagePath, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to save screenshot", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Unable to take a screenshot", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        recyclerView = findViewById(R.id.recyclerView);
        //Button btnClearHistory = findViewById(R.id.btn_clear_history);
        //Button btnScreenshot = findViewById(R.id.btn_screenshot);
        recyclerView.setEnabled(true);
        // Load chat history from SharedPreferences
        messages = loadMessages();

        chatAdapter = new ChatAdapter(messages);
        recyclerView.setAdapter(chatAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        buttonSend.setOnClickListener(view -> {
            String userMessage = editTextMessage.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                //List<ChatMessage> chat1 = getLastNItems(messages,20);
                List<ChatMessage> chat1 = new ArrayList<>();
                int minBound = 0;
                if(messages.size()>=21){
                    minBound = messages.size()-20;
                }
                else {
                    minBound = 0;
                }
                for(int i = minBound; i < messages.size();i++){
                    chat1.add(new ChatMessage(messages.get(i).getRole(),messages.get(i).getContent()));
                }
                // 将用户消息添加到聊天记录
                chat1.add(new ChatMessage("user",userMessage));
                chatAdapter.addMessage(new ChatMessage("user",userMessage));
                chatAdapter.notifyItemInserted(messages.size() - 1);
                recyclerView.scrollToPosition(messages.size() - 1);
                editTextMessage.setText("");

                // 调用ChatGPT API
                callChatGPTApi(chat1);
            }
        });
/*
        btnClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 添加清除聊天记录的代码
                messages.clear();
                chatAdapter.notifyDataSetChanged();
                saveMessages(messages);
                Toast.makeText(MainActivity.this, "Clearing chat history...", Toast.LENGTH_SHORT).show();
            }
        });

        btnScreenshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /// 获取截图
                Bitmap screenshot = getScreenshotFromRecyclerView(recyclerView,chatAdapter);

                if (screenshot != null) {
                    // 保存截图到设备存储
                    String savedImagePath = saveScreenshotToGallery(MainActivity.this, screenshot);
                    if (savedImagePath != null) {
                        Toast.makeText(MainActivity.this, "Screenshot saved to " + savedImagePath, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to save screenshot", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Unable to take a screenshot", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
    }

    private String saveScreenshotToGallery(Context context, Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmms", Locale.getDefault()).format(new Date());
        String imageFileName = "screenshot_" + timeStamp + ".jpg";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + File.separator + "Screenshots");
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }

        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            String savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            // Add the image to the system gallery
            galleryAddPic(context, savedImagePath);
            return savedImagePath;
        }
        return null;
    }
    private void galleryAddPic(Context context, String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }


        @RequiresApi(api = Build.VERSION_CODES.O)
    public void callChatGPTApi(List<ChatMessage> History) {
        try {
            Toast.makeText(MainActivity.this, "正在生成答案...", Toast.LENGTH_LONG).show();
            new FetchChatResponseTask().execute(History);
        }catch (Exception c){

        }
    }


    public String getBestAnswer(List<ChatCompletionChoice> choices) {
        if (choices != null && !choices.isEmpty()) {
            ChatCompletionChoice bestChoice = choices.get(0);
            return bestChoice.getMessage().getContent();
        }
        return null;
    }
    private void saveMessages(List<ChatMessage> messages) {
        SharedPreferences sharedPreferences = getSharedPreferences("chat_history", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(messages);
        editor.putString("messages", json);
        editor.apply();
    }

    private List<ChatMessage> loadMessages() {
        SharedPreferences sharedPreferences = getSharedPreferences("chat_history", MODE_PRIVATE);
        String json = sharedPreferences.getString("messages", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ChatMessage>>() {}.getType();
            List<ChatMessage> loadedMessages = gson.fromJson(json, type);

            for (ChatMessage message : loadedMessages) {
                if (message.getRole() == null) {
                    message.setRole("user");
                }
            }

            return loadedMessages;
            //return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    private Bitmap getScreenshotFromRecyclerView(RecyclerView view,ChatAdapter adapter) {
        Bitmap bigBitmap = null;
        if (adapter != null) {
            int size = adapter.getItemCount();
            int height = 0;
            Paint paint = new Paint();
            int iHeight = 0;
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;
            LruCache<String, Bitmap> bitmaCache = new LruCache<>(cacheSize);
            for (int i = 0; i < size; i++) {
                ChatAdapter.ChatViewHolder holder = adapter.createViewHolder(view, adapter.getItemViewType(i));
                adapter.onBindViewHolder(holder, i);
                holder.itemView.measure(
                        View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(),
                        holder.itemView.getMeasuredHeight());
                holder.itemView.setDrawingCacheEnabled(true);
                holder.itemView.buildDrawingCache();
                Bitmap drawingCache = holder.itemView.getDrawingCache();
                if (drawingCache != null) {

                    bitmaCache.put(String.valueOf(i), drawingCache);
                }
                height += holder.itemView.getMeasuredHeight();
            }

            bigBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas bigCanvas = new Canvas(bigBitmap);
            Drawable lBackground = view.getBackground();
            if (lBackground instanceof ColorDrawable) {
                ColorDrawable lColorDrawable = (ColorDrawable) lBackground;
                int lColor = lColorDrawable.getColor();
                bigCanvas.drawColor(lColor);
            }

            for (int i = 0; i < size; i++) {
                Bitmap bitmap = bitmaCache.get(String.valueOf(i));
                bigCanvas.drawBitmap(bitmap, 0f, iHeight, paint);
                iHeight += bitmap.getHeight();
                bitmap.recycle();
            }
        }
        return bigBitmap;
    }


    private class FetchChatResponseTask extends AsyncTask<List<ChatMessage>, Void, ChatMessage> {
        @Override
        protected ChatMessage doInBackground(List<ChatMessage>... params) {
            List<ChatMessage> history = params[0];

            // 在这里执行网络请求
            // ...
            String token = "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";
            ObjectMapper mapper = defaultObjectMapper();
            Duration timeout = Duration.ofSeconds(180);
            //Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("https://gpt-proxy.zeabur.app/proxy", 80));
            OkHttpClient client = defaultClient(token, timeout)
                    .newBuilder()
                    //.proxy(proxy)
                    .addInterceptor(new HttpLoggingInterceptor())
                    .build();
            Retrofit retrofit = defaultRetrofit(client, mapper);
            OpenAiApi api = retrofit.create(OpenAiApi.class);
            ChatGptService service = new ChatGptService(api);
            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .messages(history)
                    .model("gpt-3.5-turbo")
                    //.n(5)
                    //.maxTokens(3000)
                    .build();

            try{
                List<ChatCompletionChoice> response = service.createChatCompletion(completionRequest).getChoices();
                if (response!=null&& !response.isEmpty()) {
                    String chatGptAnswer = getBestAnswer(response);
                    return new ChatMessage(ChatMessageRole.ASSISTANT.value(), chatGptAnswer);
                } else {
                    return null;
                }
            }catch (Exception e){
                return null;
            }

        }

        @Override
        protected void onPostExecute(ChatMessage chatGptAnswer) {
            if (chatGptAnswer != null) {
                // 将ChatGPT回答添加到聊天记录
                messages.add(chatGptAnswer);
                chatAdapter.notifyItemInserted(messages.size() - 1);
                //chatAdapter.addMessage(chatGptAnswer);
                recyclerView.scrollToPosition(messages.size() - 1);
                // 保存聊天记录到SharedPreferences或SQLite数据库
                saveMessages(messages);
            } else {
                try{
                    editTextMessage.setText(messages.get(messages.size() - 1).getContent());
                    chatAdapter.removeMessage();
                    recyclerView.scrollToPosition(messages.size() - 1);
                    Toast.makeText(MainActivity.this, "网络错误，请重试", Toast.LENGTH_LONG).show();
                }catch (Exception e){

                }
            }
        }

        }



}