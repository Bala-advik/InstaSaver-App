package com.record.android.instasaver;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;

public class MainActivity extends AppCompatActivity {

    EditText edt;
    private int PERMISSION_STORAGE_CODE = 1000;
    long queueValue = 0;
    int postNo = 0;
    ImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgView = findViewById(R.id.imgView);

        Button b = (Button) findViewById(R.id.btnJur);
        Button bStop = (Button) findViewById(R.id.btnStop);
        edt = findViewById(R.id.edtTxt);
        edt.setText("Insta Copied Link");

        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (queueValue == 0) {
                    Toast.makeText(MainActivity.this, "Nothing in Queue", Toast.LENGTH_SHORT).show();
                } else {
                    DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    manager.remove(queueValue);
                }

            }
        });

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (edt.getText().toString().equals("")) {
                        Toast.makeText(MainActivity.this, "Fill Some", Toast.LENGTH_SHORT).show();
                    } else {
                        String str = edt.getText().toString();
                        int index = str.lastIndexOf('/') + 1;
                        String str1 = str.substring(0, index) + "?__a=1";
                        AsyncTaskRunner runner = new AsyncTaskRunner();
                        runner.execute(str1);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_STORAGE_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }

    }

    public class AsyncTaskRunner extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            try {
                return Jsoup.connect(params[0]).ignoreContentType(true).execute().body();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation

            if (result == null || result.equals("")) {
                Toast.makeText(MainActivity.this, "Nothing Provide Correct Words", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            } else {
                try {
                    JSONParser jsonParser = new JSONParser();
                    JSONObject instaList = (JSONObject) jsonParser.parse(result);
                    JSONObject instaList1 = (JSONObject) instaList.get("graphql");
                    JSONObject instaList2 = (JSONObject) instaList1.get("shortcode_media");
                    if (!(instaList2.get("edge_sidecar_to_children") == null)) {
                        JSONObject instaList3 = (JSONObject) instaList2.get("edge_sidecar_to_children");
                        JSONArray instaList4 = (JSONArray) instaList3.get("edges");
                        for (int n = 0; n < instaList4.size(); n++) {
                            JSONObject instaList5 = (JSONObject) instaList4.get(n);
                            JSONObject instaList6 = (JSONObject) instaList5.get("node");
                            System.out.print(instaList6.get("__typename") + " \n");
                            if (instaList6.get("__typename").equals("GraphVideo")) {
                                postNo++;
                                vidAdd(instaList6.get("video_url") + " \n" + "::" + " -" + postNo + " \n");
                            } else {
                                JSONArray instaList7 = (JSONArray) instaList6.get("display_resources");
                                postNo++;
                                for (int m = 0; m < instaList7.size(); m++) {
                                    JSONObject instaList8 = (JSONObject) instaList7.get(m);
                                    imgAdd(instaList8.get("src") + " \n" + "::" + instaList8.get("config_width") + " x " + instaList8.get("config_height") + " -" + postNo + " \n");
                                }
                            }
                        }
                    } else {
                        if (instaList2.get("__typename").equals("GraphVideo")) {
                            postNo++;
                            vidAdd(instaList2.get("video_url") + " \n" + "::" + " -" + postNo + " \n");
                        } else {
                            JSONArray instaList7 = (JSONArray) instaList2.get("display_resources");
                            postNo++;
                            for (int m = 0; m < instaList7.size(); m++) {
                                JSONObject instaList8 = (JSONObject) instaList7.get(m);
                                imgAdd(instaList8.get("src") + " \n" + "::" + instaList8.get("config_width") + " x " + instaList8.get("config_height") + " -" + postNo + " \n");
                            }
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            progressDialog.dismiss();
        }

        private void imgAdd(String addStr) {
            LinearLayout item = (LinearLayout) findViewById(R.id.linear_child);
            LinearLayout.LayoutParams lprams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            String[] parts = addStr.split("::");
            String part1 = parts[0];
            String part2 = parts[1];
            final Button btn = new Button(MainActivity.this);
            btn.setText(part2);
            btn.setTag(part1);
            btn.setLayoutParams(lprams);
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Picasso.get().load(btn.getTag().toString()).into(imgView);
                }
            });
            btn.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    Toast.makeText(MainActivity.this,"Long CLicked",Toast.LENGTH_SHORT).show();
                    checkPermit(btn.getTag().toString(), ".jpg");
                    return true;
                }
            });
            item.addView(btn);
        }

        private void vidAdd(String addStr) {
            LinearLayout item = (LinearLayout) findViewById(R.id.linear_childVid);
            LinearLayout.LayoutParams lprams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            String[] parts = addStr.split("::");
            String part1 = parts[0];
            String part2 = parts[1];
            final Button btn = new Button(MainActivity.this);
            btn.setText("Video" + part2);
            btn.setTag(part1);
            btn.setLayoutParams(lprams);
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
//                    Picasso.get().load(btn.getTag().toString()).into(imgView);
                    Toast.makeText(MainActivity.this, btn.getTag().toString(), Toast.LENGTH_SHORT).show();
                }
            });
            btn.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    Toast.makeText(MainActivity.this,"Long CLicked",Toast.LENGTH_SHORT).show();
                    checkPermit(btn.getTag().toString(), ".mp4");
                    return true;
                }
            });
            item.addView(btn);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Progressing",
                    "Wait");
        }

        @Override
        protected void onProgressUpdate(String... text) {
        }
    }

    private void startDownloading(String result, String type) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(result));
        int v = (int) (System.currentTimeMillis() + System.nanoTime());
        String name = ""+System.nanoTime() +"_"+ v ;
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle("Download");
        request.setDescription("Downloading ...");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS+"/Images", "" + name + type);
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        queueValue = manager.enqueue(request);
    }

    private void checkPermit(String rest, String type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, PERMISSION_STORAGE_CODE);
            } else {
                Toast.makeText(getApplicationContext(), "" + rest, Toast.LENGTH_SHORT).show();
                startDownloading(rest, type);
            }
        } else {
            Toast.makeText(getApplicationContext(), "" + rest, Toast.LENGTH_SHORT).show();
            startDownloading(rest, type);
        }
    }

}