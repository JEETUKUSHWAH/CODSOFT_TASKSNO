package com.example.dayquote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView tvQuoteText, tvAuthor;
    private ImageButton btnFavorite, btnShare;
    private Button btnViewFavorites;

    private AppDatabase db;
    private Quote currentQuote;

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

        tvQuoteText = findViewById(R.id.tvQuoteText);
        tvAuthor = findViewById(R.id.tvAuthor);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnShare = findViewById(R.id.btnShare);
        btnViewFavorites = findViewById(R.id.btnViewFavorites);

        db = AppDatabase.getInstance(this);
        seedDatabaseIfEmpty();

        // Handle daily update logic
        loadDailyQuote();

        // Button Listeners
        btnShare.setOnClickListener(v -> shareQuote());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnViewFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

    }

    private void loadDailyQuote() {
        SharedPreferences prefs = getSharedPreferences("DailyQuotePrefs", MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastSavedDate = prefs.getString("last_date", "");
        int currentQuoteId = prefs.getInt("quote_id", -1);

        List<Quote> allQuotes = db.quoteDao().getAllQuotes();

        if (!today.equals(lastSavedDate) || currentQuoteId == -1) {
            // New day: Select a random new quote
            Random random = new Random();
            currentQuote = allQuotes.get(random.nextInt(allQuotes.size()));

            // Save state for today
            prefs.edit()
                    .putString("last_date", today)
                    .putInt("quote_id", currentQuote.getId())
                    .apply();
        } else {
            // Same day: Retrieve today's assigned quote
            for (Quote q : allQuotes) {
                if (q.getId() == currentQuoteId) {
                    currentQuote = q;
                    break;
                }
            }
        }

        displayQuote(currentQuote);
    }

    private void displayQuote(Quote quote) {
        tvQuoteText.setText("\"" + quote.getText() + "\"");
        tvAuthor.setText("- " + quote.getAuthor());
        updateFavoriteButtonState(quote.isFavorite());
    }

    private void toggleFavorite() {
        if (currentQuote != null) {
            boolean newState = !currentQuote.isFavorite();
            currentQuote.setFavorite(newState);
            db.quoteDao().updateQuote(currentQuote);

            updateFavoriteButtonState(newState);
            String message = newState ? "Added to Favorites" : "Removed from Favorites";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFavoriteButtonState(boolean isFav) {
        btnFavorite.setImageResource(isFav ?
                android.R.drawable.btn_star_big_on :
                android.R.drawable.btn_star_big_off);
    }

    private void shareQuote() {
        if (currentQuote == null) return;

        String shareBody = "\"" + currentQuote.getText() + "\"\n- " + currentQuote.getAuthor();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Quote of the Day");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

        startActivity(Intent.createChooser(shareIntent, "Share quote via"));
    }

    private void seedDatabaseIfEmpty() {
        if (db.quoteDao().getQuoteCount() == 0) {
            List<Quote> seedList = new ArrayList<>();
            seedList.add(new Quote("The only way to do great work is to love what you do.", "Steve Jobs", false));
            seedList.add(new Quote("In the middle of every difficulty lies opportunity.", "Albert Einstein", false));
            seedList.add(new Quote("Believe you can and you're halfway there.", "Theodore Roosevelt", false));
            seedList.add(new Quote("It always seems impossible until it's done.", "Nelson Mandela", false));
            seedList.add(new Quote("Act as if what you do makes a difference. It does.", "William James", false));
            seedList.add(new Quote("The unexamined life is not worth living.", "Socrates", false));
            seedList.add(new Quote("Know thyself.", "Ancient Greek maxim Temple of Apollo, Delphi", false));
            seedList.add(new Quote("The only true wisdom is in knowing you know nothing.", "Socrates", false));
            seedList.add(new Quote("Life is what happens when you're busy making other plans.", "John Lennon", false));
            seedList.add(new Quote("In three words I can sum up everything I've learned about life: it goes on.", "Robert Frost", false));
            seedList.add(new Quote("The purpose of our lives is to be happy.", "Dalai Lama", false));
            seedList.add(new Quote("Life is really simple, but we insist on making it complicated.", "Confucius", false));
            seedList.add(new Quote("The journey of a thousand miles begins with a single step.", "Laozi", false));
            seedList.add(new Quote("He who has a why to live can bear almost any how.", "Friedrich Nietzsche", false));
            seedList.add(new Quote("Not all those who wander are lost.", "J.R.R. Tolkien", false));
            seedList.add(new Quote("It is our choices that show what we truly are, far more than our abilities.", "J.K. Rowling", false));
            seedList.add(new Quote("To live is the rarest thing in the world. Most people exist, that is all.", "Oscar Wilde", false));
            seedList.add(new Quote("Life shrinks or expands in proportion to one's courage.", "Anaïs Nin", false));
            seedList.add(new Quote("The two most important days in your life are the day you are born and the day you find out why.", "Mark Twain", false));
            seedList.add(new Quote("Whatever you are, be a good one.", "Abraham Lincoln", false));
            seedList.add(new Quote("The greatest glory in living lies not in never falling, but in rising every time we fall.", "Nelson Mandela", false));
            seedList.add(new Quote("Turn your wounds into wisdom.", "Oprah Winfrey", false));
            seedList.add(new Quote("Life is 10% what happens to us and 90% how we react to it.", "Charles R. Swindoll", false));
            seedList.add(new Quote("The best way out is always through.", "Robert Frost", false));
            seedList.add(new Quote("What lies behind us and what lies before us are tiny matters compared to what lies within us.", "Ralph Waldo Emerson", false));
            seedList.add(new Quote("Do not go where the path may lead, go instead where there is no path and leave a trail.", "Ralph Waldo Emerson", false));
            seedList.add(new Quote("To be yourself in a world that is constantly trying to make you something else is the greatest accomplishment.", "Ralph Waldo Emerson", false));
            seedList.add(new Quote("The privilege of a lifetime is to become who you truly are.", "Carl Jung", false));
            seedList.add(new Quote("Everything you've ever wanted is on the other side of fear.", "George Addair", false));
            seedList.add(new Quote("You only live once, but if you do it right, once is enough.", "Mae West", false));
            seedList.add(new Quote("The mind is everything. What you think you become.", "Buddha", false));
            seedList.add(new Quote("Peace comes from within. Do not seek it without.", "Buddha", false));
            seedList.add(new Quote("Three things cannot be long hidden: the sun, the moon, and the truth.", "Buddha", false));
            seedList.add(new Quote("Well done is better than well said.", "Benjamin Franklin", false));
            seedList.add(new Quote("Life is really simple, but men insist on making it complicated.", "Confucius", false));
            seedList.add(new Quote("By three methods we may learn wisdom: reflection, imitation, and experience.", "Confucius", false));
            seedList.add(new Quote("Our greatest glory is not in never falling, but in rising every time we fall.", "Confucius", false));
            seedList.add(new Quote("Everything has beauty, but not everyone sees it.", "Confucius", false));
            seedList.add(new Quote("The gem cannot be polished without friction, nor man perfected without trials.", "Confucius", false));
            seedList.add(new Quote("It does not matter how slowly you go as long as you do not stop.", "Confucius", false));
            seedList.add(new Quote("Man cannot discover new oceans unless he has the courage to lose sight of the shore.", "André Gide", false));
            seedList.add(new Quote("Twenty years from now you will be more disappointed by the things you didn't do than by the ones you did.", "Mark Twain", false));
            seedList.add(new Quote("Success is not final, failure is not fatal: it is the courage to continue that counts.", "Winston Churchill", false));
            seedList.add(new Quote("I have not failed. I've just found 10,000 ways that won't work.", "Thomas Edison", false));
            seedList.add(new Quote("Our greatest weakness lies in giving up. The most certain way to succeed is always to try just one more time.", "Thomas Edison", false));
            seedList.add(new Quote("Genius is one percent inspiration and ninety-nine percent perspiration.", "Thomas Edison", false));
            seedList.add(new Quote("It always seems impossible until it's done.", "Nelson Mandela", false));
            seedList.add(new Quote("Success is walking from failure to failure with no loss of enthusiasm.", "Winston Churchill", false));
            seedList.add(new Quote("If you're going through hell, keep going.", "Winston Churchill", false));
            seedList.add(new Quote("Success usually comes to those who are too busy to be looking for it.", "Henry David Thoreau", false));
            seedList.add(new Quote("The only place where success comes before work is in the dictionary.", "Vidal Sassoon", false));
            seedList.add(new Quote("I never dreamed about success. I worked for it.", "Estée Lauder", false));
            seedList.add(new Quote("There are no shortcuts to any place worth going.", "Beverly Sills", false));
            seedList.add(new Quote("The road to success and the road to failure are almost exactly the same.", "Colin R. Davis", false));
            seedList.add(new Quote("Do not judge me by my success, judge me by how many times I fell down and got back up again.", "Nelson Mandela", false));
            seedList.add(new Quote("Fall seven times, stand up eight.", "Japanese proverb", false));
            seedList.add(new Quote("Perseverance is not a long race; it is many short races one after the other.", "Walter Elliot", false));
            seedList.add(new Quote("It's not that I'm so smart, it's just that I stay with problems longer.", "Albert Einstein", false));
            seedList.add(new Quote("I have no special talent. I am only passionately curious.", "Albert Einstein", false));
            seedList.add(new Quote("Try not to become a person of success, but rather try to become a person of value.", "Albert Einstein", false));
            seedList.add(new Quote("Failure is simply the opportunity to begin again, this time more intelligently.", "Henry Ford", false));
            seedList.add(new Quote("Whether you think you can or you think you can't, you're right.", "Henry Ford", false));
            seedList.add(new Quote("Obstacles are those frightful things you see when you take your eyes off your goal.", "Henry Ford", false));
            seedList.add(new Quote("Quality means doing it right when no one is looking.", "Henry Ford", false));
            seedList.add(new Quote("The only limit to our realization of tomorrow will be our doubts of today.", "Franklin D. Roosevelt", false));
            seedList.add(new Quote("It is hard to fail, but it is worse never to have tried to succeed.", "Theodore Roosevelt", false));
            seedList.add(new Quote("Far and away the best prize that life offers is the chance to work hard at work worth doing.", "Theodore Roosevelt", false));
            seedList.add(new Quote("Believe you can and you're halfway there.", "Theodore Roosevelt", false));
            seedList.add(new Quote("Nothing in the world can take the place of persistence.", "Calvin Coolidge", false));
            seedList.add(new Quote("Opportunities don't happen. You create them.", "Chris Grosser", false));
            seedList.add(new Quote("The way to get started is to quit talking and begin doing.", "Walt Disney", false));
            seedList.add(new Quote("All our dreams can come true, if we have the courage to pursue them.", "Walt Disney", false));
            seedList.add(new Quote("If you can dream it, you can do it.", "Walt Disney", false));
            seedList.add(new Quote("Don't be afraid to give up the good to go for the great.", "John D. Rockefeller", false));
            seedList.add(new Quote("I'm a great believer in luck, and I find the harder I work the more I have of it.", "Thomas Jefferson", false));
            seedList.add(new Quote("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier", false));
            seedList.add(new Quote("The difference between ordinary and extraordinary is that little extra.", "Jimmy Johnson", false));
            seedList.add(new Quote("Setting goals is the first step in turning the invisible into the visible.", "Tony Robbins", false));
            seedList.add(new Quote("A leader is one who knows the way, goes the way, and shows the way.", "John C. Maxwell", false));
            seedList.add(new Quote("The greatest leader is not necessarily the one who does the greatest things. He is the one that gets the people to do the greatest things.", "Ronald Reagan", false));
            seedList.add(new Quote("Leadership is the capacity to translate vision into reality.", "Warren Bennis", false));
            seedList.add(new Quote("Before you are a leader, success is all about growing yourself. When you become a leader, success is all about growing others.", "Jack Welch", false));
            seedList.add(new Quote("Management is doing things right; leadership is doing the right things.", "Peter Drucker", false));
            seedList.add(new Quote("A leader leads by example, not by force.", "Sun Tzu", false));
            seedList.add(new Quote("The art of leadership is saying no, not saying yes. It is very easy to say yes.", "Tony Blair", false));
            seedList.add(new Quote("Innovation distinguishes between a leader and a follower.", "Steve Jobs", false));
            seedList.add(new Quote("You manage things; you lead people.", "Grace Hopper", false));
            seedList.add(new Quote("A good leader takes a little more than his share of the blame, a little less than his share of the credit.", "Arnold H. Glasow", false));
            seedList.add(new Quote("The function of leadership is to produce more leaders, not more followers.", "Ralph Nader", false));
            seedList.add(new Quote("To handle yourself, use your head; to handle others, use your heart.", "Eleanor Roosevelt", false));
            seedList.add(new Quote("No man will make a great leader who wants to do it all himself.", "Andrew Carnegie", false));
            seedList.add(new Quote("Outstanding leaders go out of the way to boost the self-esteem of their personnel.", "Sam Walton", false));
            seedList.add(new Quote("Leadership is not about being in charge. It is about taking care of those in your charge.", "Simon Sinek", false));
            seedList.add(new Quote("Great leaders are almost always great simplifiers.", "Colin Powell", false));
            seedList.add(new Quote("The price of greatness is responsibility.", "Winston Churchill", false));
            seedList.add(new Quote("Leadership and learning are indispensable to each other.", "John F. Kennedy", false));
            seedList.add(new Quote("A true leader has the confidence to stand alone, the courage to make tough decisions.", "Douglas MacArthur", false));
            seedList.add(new Quote("If your actions inspire others to dream more, learn more, do more and become more, you are a leader.", "John Quincy Adams", false));
            seedList.add(new Quote("The best executive is the one who has sense enough to pick good men to do what he wants done.", "Theodore Roosevelt", false));
            seedList.add(new Quote("Lead and inspire people. Don't try to manage and manipulate people.", "Ross Perot", false));
            seedList.add(new Quote("Real leadership is being the person others choose to follow.", "John C. Maxwell", false));
            seedList.add(new Quote("Example is not the main thing in influencing others. It is the only thing.", "Albert Schweitzer", false));
            seedList.add(new Quote("The supreme quality for leadership is unquestionably integrity.", "Dwight D. Eisenhower", false));
            seedList.add(new Quote("It is the responsibility of leadership to work intelligently with what is given, and not waste time.", "Hyman Rickover", false));
            seedList.add(new Quote("Leadership is unlocking people's potential to become better.", "Bill Bradley", false));
            seedList.add(new Quote("You do not lead by hitting people over the head.", "Dwight D. Eisenhower", false));
            seedList.add(new Quote("Leaders think and talk about the solutions. Followers think and talk about the problems.", "Brian Tracy", false));
            seedList.add(new Quote("Earn your leadership every day.", "Michael Jordan", false));
            seedList.add(new Quote("Leadership is a choice, not a position.", "Stephen Covey", false));
            seedList.add(new Quote("The very essence of leadership is that you have to have a vision.", "Theodore Hesburgh", false));
            seedList.add(new Quote("People ask the difference between a leader and a boss. The leader works in the open, and the boss in covert.", "Theodore Roosevelt", false));
            seedList.add(new Quote("Effective leadership is putting first things first.", "Stephen Covey", false));
            seedList.add(new Quote("We are most alive when we're in love.", "John Updike", false));
            seedList.add(new Quote("Love is composed of a single soul inhabiting two bodies.", "Aristotle", false));
            seedList.add(new Quote("Where there is love there is life.", "Mahatma Gandhi", false));
            seedList.add(new Quote("The best thing to hold onto in life is each other.", "Audrey Hepburn", false));
            seedList.add(new Quote("Love does not dominate; it cultivates.", "Johann Wolfgang von Goethe", false));
            seedList.add(new Quote("To love and be loved is to feel the sun from both sides.", "David Viscott", false));
            seedList.add(new Quote("Love recognizes no barriers.", "Maya Angelou", false));
            seedList.add(new Quote("Being deeply loved by someone gives you strength, while loving someone deeply gives you courage.", "Lao Tzu", false));
            seedList.add(new Quote("Love is a friendship set to music.", "Joseph Campbell", false));
            seedList.add(new Quote("The greatest happiness of life is the conviction that we are loved.", "Victor Hugo", false));
            seedList.add(new Quote("There is always some madness in love. But there is also always some reason in madness.", "Friedrich Nietzsche", false));
            seedList.add(new Quote("Love yourself first and everything else falls into line.", "Lucille Ball", false));
            seedList.add(new Quote("A successful marriage requires falling in love many times, always with the same person.", "Mignon McLaughlin", false));
            seedList.add(new Quote("Whatever our souls are made of, his and mine are the same.", "Emily Brontë", false));
            seedList.add(new Quote("Love all, trust a few, do wrong to none.", "William Shakespeare", false));
            seedList.add(new Quote("The course of true love never did run smooth.", "William Shakespeare", false));
            seedList.add(new Quote("Love looks not with the eyes, but with the mind.", "William Shakespeare", false));
            seedList.add(new Quote("So, it's not gonna be easy. It's going to be really hard.", "Nicholas Sparks", false));
            seedList.add(new Quote("In real love you want the other person's good. In romantic love, you want the other person.", "Margaret Anderson", false));
            seedList.add(new Quote("Love is an irresistible desire to be irresistibly desired.", "Robert Frost", false));
            seedList.add(new Quote("You know you're in love when you can't fall asleep because reality is finally better than your dreams.", "Dr. Seuss", false));
            seedList.add(new Quote("Love is or it ain't. Thin love ain't love at all.", "Toni Morrison", false));
            seedList.add(new Quote("Life is the flower for which love is the honey.", "Victor Hugo", false));
            seedList.add(new Quote("The heart wants what it wants.", "Emily Dickinson", false));
            seedList.add(new Quote("Every child is an artist. The problem is how to remain an artist once we grow up.", "Pablo Picasso", false));
            seedList.add(new Quote("Creativity takes courage.", "Henri Matisse", false));
            seedList.add(new Quote("Art washes away from the soul the dust of everyday life.", "Pablo Picasso", false));
            seedList.add(new Quote("The chief enemy of creativity is good sense.", "Pablo Picasso", false));
            seedList.add(new Quote("You can't use up creativity. The more you use, the more you have.", "Maya Angelou", false));
            seedList.add(new Quote("Imagination is more important than knowledge.", "Albert Einstein", false));
            seedList.add(new Quote("Creativity is intelligence having fun.", "Albert Einstein", false));
            seedList.add(new Quote("To practice any art, no matter how well or badly, is a way to make your soul grow.", "Kurt Vonnegut", false));
            seedList.add(new Quote("Inspiration exists, but it has to find you working.", "Pablo Picasso", false));
            seedList.add(new Quote("The world always seems brighter when you've just made something that wasn't there before.", "Neil Gaiman", false));
            seedList.add(new Quote("Art is not what you see, but what you make others see.", "Edgar Degas", false));
            seedList.add(new Quote("Painting is easy when you don't know how, but very difficult when you do.", "Edgar Degas", false));
            seedList.add(new Quote("Design is not just what it looks like and feels like. Design is how it works.", "Steve Jobs", false));
            seedList.add(new Quote("Творчество requires the courage to let go of certainties.", "Erich Fromm", false));
            seedList.add(new Quote("A word after a word after a word is power.", "Margaret Atwood", false));
            seedList.add(new Quote("There is no greater agony than bearing an untold story inside you.", "Maya Angelou", false));
            seedList.add(new Quote("Either write something worth reading or do something worth writing.", "Benjamin Franklin", false));
            seedList.add(new Quote("The purpose of art is washing the dust of daily life off our souls.", "Pablo Picasso", false));
            seedList.add(new Quote("Music is the shorthand of emotion.", "Leo Tolstoy", false));
            seedList.add(new Quote("Without music, life would be a mistake.", "Friedrich Nietzsche", false));
            seedList.add(new Quote("Somewhere, something incredible is waiting to be known.", "Carl Sagan", false));
            seedList.add(new Quote("The universe is under no obligation to make sense to you.", "Neil deGrasse Tyson", false));
            seedList.add(new Quote("Science is a way of thinking much more than it is a body of knowledge.", "Carl Sagan", false));
            seedList.add(new Quote("Extraordinary claims require extraordinary evidence.", "Carl Sagan", false));
            seedList.add(new Quote("Nothing in life is to be feared, it is only to be understood.", "Marie Curie", false));
            seedList.add(new Quote("Be less curious about people and more curious about ideas.", "Marie Curie", false));
            seedList.add(new Quote("I am among those who think that science has great beauty.", "Marie Curie", false));
            seedList.add(new Quote("Equipped with his five senses, man explores the universe around him and calls the adventure Science.", "Edwin Hubble", false));
            seedList.add(new Quote("The good thing about science is that it's true whether or not you believe in it.", "Neil deGrasse Tyson", false));
            seedList.add(new Quote("An expert is a person who has made all the mistakes that can be made in a very narrow field.", "Niels Bohr", false));
            seedList.add(new Quote("If we knew what it was we were doing, it would not be called research, would it?", "Albert Einstein", false));
            seedList.add(new Quote("The important thing is not to stop questioning. Curiosity has its own reason for existing.", "Albert Einstein", false));
            seedList.add(new Quote("Look up at the stars and not down at your feet.", "Stephen Hawking", false));
            seedList.add(new Quote("Intelligence is the ability to adapt to change.", "Stephen Hawking", false));
            seedList.add(new Quote("However difficult life may seem, there is always something you can do and succeed at.", "Stephen Hawking", false));
            seedList.add(new Quote("The most exciting phrase to hear in science is not 'Eureka!' but 'That's funny...'", "Isaac Asimov", false));
            seedList.add(new Quote("Science knows no country, because knowledge belongs to humanity.", "Louis Pasteur", false));
            seedList.add(new Quote("Chance favors the prepared mind.", "Louis Pasteur", false));
            seedList.add(new Quote("Research is what I'm doing when I don't know what I'm doing.", "Wernher von Braun", false));
            seedList.add(new Quote("The scientist is not the person who gives the right answers, he's the one who asks the right questions.", "Claude Lévi-Strauss", false));
            seedList.add(new Quote("Your most unhappy customers are your greatest source of learning.", "Bill Gates", false));
            seedList.add(new Quote("Stay hungry, stay foolish.", "Steve Jobs", false));
            seedList.add(new Quote("The only way to do great work is to love what you do.", "Steve Jobs", false));
            seedList.add(new Quote("Your work is going to fill a large part of your life, and the only way to be truly satisfied is to do what you believe is great work.", "Steve Jobs", false));
            seedList.add(new Quote("It's not the customer's job to know what they want.", "Steve Jobs", false));
            seedList.add(new Quote("I skate to where the puck is going to be, not where it has been.", "Wayne Gretzky", false));
            seedList.add(new Quote("In the world of business, the people who are most successful are those who are doing what they love.", "Warren Buffett", false));
            seedList.add(new Quote("Price is what you pay. Value is what you get.", "Warren Buffett", false));
            seedList.add(new Quote("It takes 20 years to build a reputation and five minutes to ruin it.", "Warren Buffett", false));
            seedList.add(new Quote("Someone's sitting in the shade today because someone planted a tree a long time ago.", "Warren Buffett", false));
            seedList.add(new Quote("Risk comes from not knowing what you're doing.", "Warren Buffett", false));
            seedList.add(new Quote("The biggest risk is not taking any risk.", "Mark Zuckerberg", false));
            seedList.add(new Quote("Move fast and break things.", "Mark Zuckerberg", false));
            seedList.add(new Quote("If you set your goals ridiculously high and it's a failure, you will fail above everyone else's success.", "James Cameron", false));
            seedList.add(new Quote("Business opportunities are like buses, there's always another one coming.", "Richard Branson", false));
            seedList.add(new Quote("Screw it, let's do it.", "Richard Branson", false));
            seedList.add(new Quote("If you can't explain it simply, you don't understand it well enough.", "Albert Einstein", false));
            seedList.add(new Quote("The best way to predict the future is to invent it.", "Alan Kay", false));
            seedList.add(new Quote("Ideas are commodity. Execution of them is not.", "Michael Dell", false));
            seedList.add(new Quote("Fail fast, fail often.", "Silicon Valley business proverb", false));
            seedList.add(new Quote("If you double the number of experiments you do per year, you're going to double your inventiveness.", "Jeff Bezos", false));
            seedList.add(new Quote("Your brand is what other people say about you when you're not in the room.", "Jeff Bezos", false));
            seedList.add(new Quote("We see our customers as invited guests to a party, and we are the hosts.", "Jeff Bezos", false));
            seedList.add(new Quote("Good decisions come from experience, and experience comes from bad decisions.", "Rita Mae Brown", false));
            seedList.add(new Quote("The customer's perception is your reality.", "Kate Zabriskie", false));
            seedList.add(new Quote("I've missed more than 9,000 shots in my career. I've lost almost 300 games... I've failed over and over and over again in my life. And that is why I succe ed.", "Michael Jordan", false));
            seedList.add(new Quote("Champions keep playing until they get it right.", "Billie Jean King", false));
            seedList.add(new Quote("It's not whether you get knocked down, it's whether you get up.", "Vince Lombardi", false));
            seedList.add(new Quote("Winning isn't everything, but wanting to win is.", "Vince Lombardi", false));
            seedList.add(new Quote("The only way to prove you are a good sport is to lose.", "Ernie Banks", false));
            seedList.add(new Quote("You miss 100% of the shots you don't take.", "Wayne Gretzky", false));
            seedList.add(new Quote("Float like a butterfly, sting like a bee.", "Muhammad Ali", false));
            seedList.add(new Quote("I hated every minute of training, but I said, 'Don't quit. Suffer now and live the rest of your life as a champion.'", "Muhammad Ali", false));
            seedList.add(new Quote("It's not the size of the dog in the fight, it's the size of the fight in the dog.", "Mark Twain", false));
            seedList.add(new Quote("Hard work beats talent when talent doesn't work hard.", "Tim Notke", false));
            seedList.add(new Quote("The difference between the impossible and the possible lies in a person's determination.", "Tommy Lasorda", false));
            seedList.add(new Quote("You have to expect things of yourself before you can do them.", "Michael Jordan", false));
            seedList.add(new Quote("Talent wins games, but teamwork and intelligence win championships.", "Michael Jordan", false));
            seedList.add(new Quote("Champions aren't made in gyms. Champions are made from something they have deep inside them.", "Muhammad Ali", false));
            seedList.add(new Quote("Impossible is just a big word thrown around by small men.", "Muhammad Ali", false));
            seedList.add(new Quote("Age is no barrier. It's a limitation you put on your mind.", "Jackie Joyner-Kersee", false));
            seedList.add(new Quote("The strength of the team is each individual member. The strength of each member is the team.", "Phil Jackson", false));
            seedList.add(new Quote("A champion is defined not by their wins but by how they can recover when they fall.", "Serena Williams", false));
            seedList.add(new Quote("I don't like to lose. That's why I compete, to win.", "Serena Williams", false));
            seedList.add(new Quote("Injustice anywhere is a threat to justice everywhere.", "Martin Luther King Jr.", false));
            seedList.add(new Quote("Darkness cannot drive out darkness; only light can do that.", "Martin Luther King Jr.", false));
            seedList.add(new Quote("The time is always right to do what is right.", "Martin Luther King Jr.", false));
            seedList.add(new Quote("I have a dream that my four little children will one day live in a nation where they will not be judged by the color of their skin.", "Martin Luther King Jr.", false));
            seedList.add(new Quote("Our lives begin to end the day we become silent about things that matter.", "Martin Luther King Jr.", false));
            seedList.add(new Quote("A man who won't die for something is not fit to live.", "Martin Luther King Jr.", false));
            seedList.add(new Quote("Freedom is never given; it is won.", "A. Philip Randolph", false));
            seedList.add(new Quote("Freedom is not worth having if it does not include the freedom to make mistakes.", "Mahatma Gandhi", false));
            seedList.add(new Quote("An eye for an eye only ends up making the whole world blind.", "Mahatma Gandhi", false));
            seedList.add(new Quote("You must be the change you wish to see in the world.", "Mahatma Gandhi", false));
            seedList.add(new Quote("First they ignore you, then they laugh at you, then they fight you, then you win.", "Mahatma Gandhi", false));
            seedList.add(new Quote("It always seems impossible until it's done.", "Nelson Mandela", false));
            seedList.add(new Quote("Education is the most powerful weapon which you can use to change the world.", "Nelson Mandela", false));
            seedList.add(new Quote("May your choices reflect your hopes, not your fears.", "Nelson Mandela", false));
            seedList.add(new Quote("A nation should not be judged by how it treats its highest citizens, but its lowest ones.", "Nelson Mandela", false));
            seedList.add(new Quote("Liberty, when it begins to take root, is a plant of rapid growth.", "George Washington", false));
            seedList.add(new Quote("Give me liberty, or give me death!", "Patrick Henry", false));
            seedList.add(new Quote("The tree of liberty must be refreshed from time to time with the blood of patriots and tyrants.", "Thomas Jefferson", false));
            seedList.add(new Quote("Power concedes nothing without a demand. It never did and it never will.", "Frederick Douglass", false));
            seedList.add(new Quote("Once you learn to read, you will be forever free.", "Frederick Douglass", false));
            seedList.add(new Quote("I would rather be a free man in my grave than a slave.", "Malcolm X", false));
            seedList.add(new Quote("A right delayed is a right denied.", "Martin Luther King Jr.", false));
            seedList.add(new Quote("It was the best of times, it was the worst of times.", "Charles Dickens", false));
            seedList.add(new Quote("All happy families are alike; each unhappy family is unhappy in its own way.", "Leo Tolstoy", false));
            seedList.add(new Quote("It is a truth universally acknowledged, that a single man in possession of a good fortune, must be in want of a wife.", "Jane Austen", false));
            seedList.add(new Quote("So we beat on, boats against the current, borne back ceaselessly into the past.", "F. Scott Fitzgerald", false));
            seedList.add(new Quote("Not all who wander are lost.", "J.R.R. Tolkien", false));
            seedList.add(new Quote("There is no greater agony than bearing an untold story inside you.", "Maya Angelou", false));
            seedList.add(new Quote("There is nothing to writing. All you do is sit down at a typewriter and bleed.", "Ernest Hemingway", false));
            seedList.add(new Quote("We are all fools in love.", "Jane Austen", false));
            seedList.add(new Quote("It matters not what someone is born, but what they grow to be.", "J.K. Rowling", false));
            seedList.add(new Quote("Words are, in my not-so-humble opinion, our most inexhaustible source of magic.", "J.K. Rowling", false));
            seedList.add(new Quote("All we have to decide is what to do with the time that is given us.", "J.R.R. Tolkien", false));
            seedList.add(new Quote("Even the smallest person can change the course of the future.", "J.R.R. Tolkien", false));
            seedList.add(new Quote("It is our choices that show what we truly are, far more than our abilities.", "J.K. Rowling", false));
            seedList.add(new Quote("Fill your paper with the breathings of your heart.", "William Wordsworth", false));
            seedList.add(new Quote("A room without books is like a body without a soul.", "Marcus Tullius Cicero", false));
            seedList.add(new Quote("There is no friend as loyal as a book.", "Ernest Hemingway", false));
            seedList.add(new Quote("You can never get a cup of tea large enough or a book long enough to suit me.", "C.S. Lewis", false));
            seedList.add(new Quote("Some day you will be old enough to start reading fairy tales again.", "C.S. Lewis", false));
            seedList.add(new Quote("Happiness is not something ready-made. It comes from your own actions.", "Dalai Lama", false));
            seedList.add(new Quote("For every minute you are angry you lose sixty seconds of happiness.", "Ralph Waldo Emerson", false));
            seedList.add(new Quote("Folks are usually about as happy as they make their minds up to be.", "Abraham Lincoln", false));
            seedList.add(new Quote("Happiness is when what you think, what you say, and what you do are in harmony.", "Mahatma Gandhi", false));
            seedList.add(new Quote("Most folks are as happy as they make up their minds to be.", "Abraham Lincoln", false));
            seedList.add(new Quote("The happiness of your life depends upon the quality of your thoughts.", "Marcus Aurelius", false));
            seedList.add(new Quote("Very little is needed to make a happy life; it is all within yourself, in your way of thinking.", "Marcus Aurelius", false));
            seedList.add(new Quote("The most important thing is to enjoy your life, to be happy, it's all that matters.", "Audrey Hepburn", false));
            seedList.add(new Quote("Happiness is not a goal; it's a by-product of a life well lived.", "Eleanor Roosevelt", false));
            seedList.add(new Quote("The secret of happiness is not in doing what one likes, but in liking what one does.", "James M. Barrie", false));
            seedList.add(new Quote("Count your age by friends, not years. Count your life by smiles, not tears.", "John Lennon", false));
            seedList.add(new Quote("Happiness is a warm puppy.", "Charles M. Schulz", false));
            seedList.add(new Quote("Success is not the key to happiness. Happiness is the key to success.", "Albert Schweitzer", false));
            seedList.add(new Quote("Happiness depends upon ourselves.", "Aristotle", false));
            seedList.add(new Quote("Courage is not the absence of fear, but the triumph over it.", "Nelson Mandela", false));
            seedList.add(new Quote("I learned that courage was not the absence of fear, but the triumph over it.", "Nelson Mandela", false));
            seedList.add(new Quote("You gain strength, courage, and confidence by every experience in which you really stop to look fear in the face.", "Eleanor Roosevelt", false));
            seedList.add(new Quote("Do the thing you fear, and death of fear is certain.", "Ralph Waldo Emerson", false));
            seedList.add(new Quote("Fear is the main source of superstition, and one of the main sources of cruelty.", "Bertrand Russell", false));
            seedList.add(new Quote("The only thing we have to fear is fear itself.", "Franklin D. Roosevelt", false));
            seedList.add(new Quote("Cowards die many times before their deaths; the valiant never taste of death but once.", "William Shakespeare", false));

            db.quoteDao().insertAll(seedList);
        }
    }

}