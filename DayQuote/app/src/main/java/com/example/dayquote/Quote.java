package com.example.dayquote;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quotes")
public class Quote {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String text;
    private String author;
    private boolean isFavorite;

    public Quote(String text, String author, boolean isFavorite) {
        this.text = text;
        this.author = author;
        this.isFavorite = isFavorite;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getText() { return text; }
    public String getAuthor() { return author; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}