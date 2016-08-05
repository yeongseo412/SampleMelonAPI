package com.example.user.samplemelonapi;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(mAdapter);

        // API 연결하는 Task 실행
        new MyMelonTask().execute(1, 50);
    }

    public static final String MELON_URL="http://apis.skplanetx.com/melon/charts/realtime?version=1&page=%d&count=%d";


    public class MyMelonTask extends AsyncTask<Integer, Integer, String> {

        // Ctrl + I
        // Background 에서 수행할 내용
        @Override
        protected String doInBackground(Integer... params) {
            // 접속... Melon API
            int page = params[0];
            int count = params[1];

            // %S를 썼던 자리에 page와 count가 들어가도록 format
            String urlText = String.format(MELON_URL, page, count);


            try {
                URL url = new URL(urlText);

                // java 에서 제공하는 접근 객체, 입력된 url로 connection
                // connection 객체에 나의 App Key와 필요한 정보들을 실어서 보냄
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                // 요청할 때의 속성을 설정하는 함수, 속성으로 appKey 부여
                // 인증에 관한 문서 참고
                conn.setRequestProperty("appKey", "1a501c60-4271-3d4f-b1d9-49ed9866f689");

                // code! 404 error... 을 Http의 response code
                // 이 코드를 확인해서 오류 등을 확인한다.
                int code = conn.getResponseCode();

                if(code == HttpURLConnection.HTTP_OK) {               // code == 200 (연결 성공일 때)
                    // response String을 받자.
                    // String 얼마나 길지 모르고 String 형태의 파일일 수도 있기 때문에 BufferedReader...
                    // conn에서 들어온 스트림을 reader로 읽고 buffer로 쪼개어 넣는다.
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();

                    // 줄단위로 분석하는데 이 줄을 세는 게 line (반복문의 i 개념)
                    String line;
                    while ((line = br.readLine()) != null) {           // 한 줄씩 line이 끝날때까지
                        // line을 StringBuilder에 줄바꿈 하면서 append로 추가
                        // 최종적으로 response 원문이 복사
                        sb.append(line).append("\n\r");
                    }

                    return sb.toString();

                    // Parsing! >> onPostExecute(String s)
                    //             s는 return 된 sb.toString();
                }

            } catch (MalformedURLException e){
                e.printStackTrace();
            }
                catch(Exception e) {
                e.printStackTrace();
            }

            // null이 return 됐다는 건 실패!
            return null;
        }

        @Override
        protected void onPostExecute(String responseString) {
            super.onPostExecute(responseString);
            // MelonTask가 execute된 이후에 실행되는 callback method

            // Parsing!
            Gson gson = new Gson();
            MelonResult result = gson.fromJson(responseString, MelonResult.class);

            // 배열을 순회하는 for문
            for(Song song : result.melon.songs.song) {
                mAdapter.add(song.songName);
            }
        }
    }
}
