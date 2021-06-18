package com.example.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ImageView celebrityImageView;
    ArrayList<String> celebUrls = new ArrayList<String>();        //ctrl+D -repeat same line in next one
    ArrayList<String> celebNames = new ArrayList<String>();
    int rand;
    int locationOfCorrectAns = 0;
    String[] answers = new String[4];
    Button b0, b1, b2, b3;

    //Buttons
    public void CelebChosen(View v) {

        int tagOfButton = Integer.parseInt((String) v.getTag());
        if (tagOfButton == locationOfCorrectAns) {
            Toast.makeText(MainActivity.this, "Yes!You are correct.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "No!That's Wrong.The celeb is " + answers[locationOfCorrectAns], Toast.LENGTH_SHORT).show();
        }
        mainplay();
    }

    public void mainplay(){

        rand = (int) (Math.random() * 100);  //one time at oncreate
        DownloadImg imgTsk = new DownloadImg();
        Bitmap celebImg;
        try {
            celebImg = imgTsk.execute(celebUrls.get(rand)).get();
            celebrityImageView.setImageBitmap(celebImg);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        locationOfCorrectAns = (int) (Math.random() * 3);
        String correctAns = celebNames.get(rand);  //bcoz index of celebnames and celeburls are equal and related to one another
        String incorrectAns = "";
        for (int i = 0; i < 4; i++) {
            if (i == locationOfCorrectAns)
                answers[i] = correctAns;
            else {
                while (incorrectAns == correctAns) {
                    incorrectAns = celebNames.get((int) (Math.random() * 100));
                }
                answers[i] = incorrectAns=celebNames.get((int) (Math.random() * 100));
            }
        }

        b0 = (Button) findViewById(R.id.button0);
        b1 = (Button) findViewById(R.id.button1);
        b2 = (Button) findViewById(R.id.button2);
        b3 = (Button) findViewById(R.id.button3);

        b0.setText(answers[0]);
        b1.setText(answers[1]);
        b2.setText(answers[2]);
        b3.setText(answers[3]);

    }

    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            //For lading entire web content as string

            String result = "";
            URL url;  //URL is a datatype for strings in url format
            HttpURLConnection urlConnection = null;  //initialising the browser
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();  //loads the web-browser
                InputStream in = urlConnection.getInputStream();              //to hold the input stream of data from url[o]

                //1.
                // InputStreamReader reader=new InputStreamReader(in);
//                int data = reader.read();
//                while (data!=-1){  //till the end pf input stream
//                    char current=(char) data;
//                    result+=current;     //appending each char in empty result
//                    data=reader.read();  //read next char

                //2.
//                StringReader initialReader = new StringReader();
//                char[] arr = new char[8 * 1024];
//                StringBuilder buffer = new StringBuilder();
//                int numCharsRead;
//                while ((numCharsRead = initialReader.read(arr, 0, arr.length)) != -1) {
//                    buffer.append(arr, 0, numCharsRead);
//                }
//                initialReader.close();
//                String targetString = buffer.toString();

                //3.
//                HttpClient client = HttpClient.newHttpClient();
//                HttpRequest request = HttpRequest.newBuilder()
//                        .uri(URI.create("http://webcode.me"))
//                        .GET() // GET is default
//                        .build();
//
//                HttpResponse<String> response = client.send(request,
//                        HttpResponse.BodyHandlers.ofString());
//
//                System.out.println(response.body());

                //4. Buffer REader is better option as it reads in bulk,not each char!!!!
                try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                    String line;
                    StringBuilder sb = new StringBuilder();

                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                        sb.append(System.lineSeparator());
                    }
                    result = sb.toString();


                } catch (MalformedURLException malformedURLException) {
                    malformedURLException.printStackTrace();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                return result;  //return is always string(parameter 3)
            } catch (Exception e) {
                e.printStackTrace();
                return "Failed";
            }
        }
    }

    public class DownloadImg extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {

                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream is = connection.getInputStream();

                Bitmap bitmap = BitmapFactory.decodeStream(is);
                return bitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        celebrityImageView = (ImageView) findViewById(R.id.imageView);

        DownloadTask task = new DownloadTask();
        String result = null;
        String[] splitResult = null;
        String[] finalSplit = null;
        try {
            result = task.execute("https://www.bollywoodhungama.com/celebrities/top-100/").get();     //to load the website on starting the application
            splitResult = result.split("class=\"bh-mc-pager top-100-movies-tab top-100-celebs-tab\"></div></footer>");
            finalSplit = splitResult[0].split("title=\"Top 100 Hindi Bollywood Celebrities\" >");

            Pattern p = Pattern.compile("role=\"img\" title=\"(.*?)\" srcset");
            Matcher m = p.matcher(finalSplit[1]);
            while (m.find()) {
                celebNames.add(m.group(1));
            }

            p = Pattern.compile("\" src=\"(.*?)\" class");
            m = p.matcher(finalSplit[1]);
            while (m.find()) {
                celebUrls.add(m.group(1));
            }

            mainplay();


        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}



