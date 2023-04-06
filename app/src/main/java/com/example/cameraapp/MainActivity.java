package com.example.cameraapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.text.Transliterator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private final int request_code = 100;
    private FloatingActionButton camerabtn;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference("imageurl");
    String imageFileName;
    private RecyclerView recyclerView;
    private ArrayList<Pictures> list;

    ArrayList<String> keylist = new ArrayList<>();


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // mImageView = findViewById(R.id.imgcamera);
        camerabtn = findViewById(R.id.camera);

        storageReference = FirebaseStorage.getInstance().getReference();

        recyclerView= findViewById(R.id.recycleviewcam);
        //recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);

        //getting image from database
        list = new ArrayList<>();
        Adapter adapter = new Adapter(this,list);
        recyclerView.setAdapter(adapter);

                DatabaseReference dbget = FirebaseDatabase.getInstance().getReference("imageurl");

//                dbget.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        for (DataSnapshot ds : snapshot.getChildren()) {
//                            Pictures picture = ds.getValue(Pictures.class);
//                            list.add(picture);
//                        }
//                        adapter.notifyDataSetChanged();
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });

                dbget.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Pictures picture = dataSnapshot.getValue(Pictures.class);
                            list.add(picture);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Database error", Toast.LENGTH_LONG).show();
                    }
                });



                //camera intent
        camerabtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //imageuri = getImageUri();
                startActivityForResult(cameraIntent,request_code);
            }
        });

    }


//
//    private Uri getImageUri() {
//        Uri m_imgUri = null;
//        File m_file;
//        try {
//            SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
//            String m_curentDateandTime = m_sdf.format(new Date());
//            String m_imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + m_curentDateandTime + ".jpg";
//            m_file = new File(m_imagePath);
//            m_imgUri = Uri.fromFile(m_file);
//        } catch (Exception p_e) {
//        }
//        return m_imgUri;
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri contenturi = data.getData();
        if (requestCode == request_code){

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            imageFileName = "JPG_"+timeStamp;
            Bitmap img = (Bitmap) (data.getExtras()).get("data");
            // mImageView.setImageBitmap(img);

            uploadImage(imageFileName,data);

            saveimage(img);

            //mongodpConnection(data);
        }
    }
//
//    private void getImage(String imageFileName) {
//
//        System.out.println(imageFileName);
//        //String getfilename = bb.toString();
//
//        storageReference.child("image/"+imageFileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                String uridown = uri.toString();
//                Toast.makeText(MainActivity.this,"Successfully to get image",Toast.LENGTH_LONG).show();
//                System.out.println(uridown);
//                mImageView.setImageURI(Uri.parse(uridown));
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(MainActivity.this,"Failed to get image",Toast.LENGTH_LONG).show();
//            }
//        });
//    }
    private void uploadImage(String imageFileName, Intent data) {
        Bitmap map = (Bitmap) (data.getExtras()).get("data");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        map.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte bb[] = byteArrayOutputStream.toByteArray();

        uploadToFirebase(imageFileName,bb);

    }

    private void uploadToFirebase(String imageFileName, byte[] bb) {

        //String filename = bb.toString();
        System.out.println(imageFileName);

        StorageReference sr = storageReference.child("image/"+imageFileName);
        sr.putBytes(bb).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, "Successfully upload", Toast.LENGTH_LONG).show();
                final Task<Uri> firebaseuri = taskSnapshot.getStorage().getDownloadUrl();
                firebaseuri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //String mDownloadUrl = uri.toString();

                        Pictures pic = new Pictures(uri.toString());
                        String key = db.push().getKey();
                        keylist.add(key);
                       db.child(key).setValue(pic);
                    }
                });

                //getImage(imageFileName);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to upload", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void saveimage(Bitmap img) {

        Uri images;
        ContentResolver contentResolver = getContentResolver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            images = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }else {
            images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() +".jpg");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*");
        Uri uri = contentResolver.insert(images, contentValues);
        try {
            OutputStream outputStream = contentResolver.openOutputStream(Objects.requireNonNull(uri));
            img.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            Objects.requireNonNull(outputStream);

            // Toast.makeText(MainActivity.this,"Store in mobile",Toast.LENGTH_LONG);

            //

        }catch (Exception e){
            //
            e.printStackTrace();

        }
    }
}

