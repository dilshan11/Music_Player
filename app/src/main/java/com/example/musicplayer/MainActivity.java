package com.example.musicplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;

    ListView listView;
    String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.listViewSong);

        // Check and request permissions
        if (checkPermissions()) {
            displaySongs();
        } else {
            requestPermissions();
        }

    }

    // Method to check permissions
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Request permissions at runtime
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    // Handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displaySongs();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public ArrayList<File> getAllAudioFiles(Context context) {
        ArrayList<File> audioFiles = new ArrayList<>();

        String[] projection = {
                MediaStore.Audio.Media.DATA,  // File path
                MediaStore.Audio.Media.DISPLAY_NAME  // File name
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"; // Ensures only music files
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC"; // Sort by newest first

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
        );

        if (cursor != null) {
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(dataColumn);
                File file = new File(filePath);
                if (file.exists()) {
                    audioFiles.add(file);
                }
            }
            cursor.close();
        }
        return audioFiles;
    }

    public void displaySongs() {
      //  final ArrayList<File> arrayList = findSong(Environment.getExternalStorageDirectory());
        final ArrayList<File> arrayList = getAllAudioFiles(this);
        items = new String[arrayList.size()];
        for (int i=0; i<items.length; i++) {
           items[i] = arrayList.get(i).getName().toString().replace(".mp3","").replace(".wav","");
        }
        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(stringArrayAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String songName = parent.getItemAtPosition(position).toString();
            Toast.makeText(this, "File path " + arrayList.get(position).getName(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
            intent.putExtra("songs", arrayList);
            intent.putExtra("songname", songName);
            intent.putExtra("position", position);
            startActivity(intent);
        });
    }

}