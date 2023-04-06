package com.example.cameraapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<MyViewHolder> {

    Context context;
    List<Pictures> pic;

    public Adapter(Context context, List<Pictures> pictures) {
        this.context = context;
        this.pic = pictures;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //Glide.with(context).load(pic.get(position).getImage_url()).placeholder(R.drawable.baseline_photo_camera_24).into(holder.image);
        String image = pic.get(position).getImage_url();
        System.out.println("image in Adapter                "+image);
        Picasso.get().load(pic.get(position).getImage_url()).into(holder.image);
//        Picasso.get().load(pic.get(position).getImage_url()).error(R.drawable.baseline_photo_camera_24).into(holder.image, new Callback() {
//            @Override
//            public void onSuccess() {
//                System.out.println("Image added to recyclerview");
//            }
//
//            @Override
//            public void onError(Exception e) {
//                System.out.println(e.getMessage());
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return pic.size();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder {
    public ImageView image;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.imagecard);
    }
}

