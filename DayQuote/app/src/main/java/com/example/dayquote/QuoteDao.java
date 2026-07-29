package com.example.dayquote;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface QuoteDao {
    @Insert
    void insertAll(List<Quote> quotes);

    @Query("SELECT * FROM quotes")
    List<Quote> getAllQuotes();

    @Query("SELECT * FROM quotes WHERE isFavorite = 1")
    List<Quote> getFavoriteQuotes();

    @Update
    void updateQuote(Quote quote);

    @Query("SELECT COUNT(*) FROM quotes")
    int getQuoteCount();
}