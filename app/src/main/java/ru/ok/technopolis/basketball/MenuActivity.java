package ru.ok.technopolis.basketball;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private MediaPlayer musicPlayer;
    private ImageView panel;
    private Vibrator vibrator;
    private ImageView logo;
    private Button playButton;
    private Button optionsButton;
    private Button paintButton;
    private Button backFromOpt;
    private Switch musicView;
    private Switch wallsView;
    private Switch vibroView;
    private Button backFromCus;
    private Button left;
    private Button right;
    private ImageView ballView;
    private Button leaderboard;
    private boolean music;
    private boolean walls;
    private boolean vibro;
    private int chosenBall;
    private List<Integer> balls;
    private SharedPreferences sp;
    private static final String SP_NAME = "settings";
    private static final int RC_LEADERBOARD_UI = 9004;
    private static final int RC_SIGN_IN = 9001;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        GoogleSignInOptions gso = new

                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestEmail()
                .build();

        //Делаю вход в Google игры
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

//Start the sign-in intent with startActivityForResult//

        startActivityForResult(signInIntent, RC_SIGN_IN);
        getSupportActionBar().hide();
        initUI();
        initValues();
        initListeners();
    }

    private void initValues() {
        sp = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        music = sp.getBoolean(MainActivity.MUSIC_KEY, true);
        musicView.setChecked(music);
        vibro = sp.getBoolean(MainActivity.VIBRO_KEY, false);
        vibroView.setChecked(vibro);
        walls = sp.getBoolean(MainActivity.WALL_KEY, true);
        wallsView.setChecked(walls);
        loadBalls();
        chosenBall = sp.getInt(MainActivity.BALL_KEY, 0);
        ballView.setImageResource(balls.get(chosenBall));
    }

    private void loadBalls() {
        balls = new ArrayList<>();
        balls.add(R.drawable.ball2);
        balls.add(R.drawable.ball);
        balls.add(R.drawable.ball3);
    }

    private void initListeners() {
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadGame();
            }
        });
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMenuInterface(View.GONE);
                setOptionInterface(View.VISIBLE);
            }
        });
        paintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPaintInterface(View.VISIBLE);
                setMenuInterface(View.GONE);
            }
        });
        backFromOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOptionInterface(View.GONE);
                setMenuInterface(View.VISIBLE);
            }
        });
        backFromCus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPaintInterface(View.GONE);
                setMenuInterface(View.VISIBLE);
            }
        });
        musicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                music = musicView.isChecked();
                sp.edit().putBoolean("music", music).apply();
                musicPlayer.setVolume(music ? 0.2f : 0, music ? 0.2f : 0);
            }
        });
        vibroView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibro = vibroView.isChecked();
                sp.edit().putBoolean("vibro", vibro).apply();
                if (vibro && vibrator.hasVibrator()) {
                    vibrator.vibrate(100L);
                }
            }
        });
        wallsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                walls = wallsView.isChecked();
                sp.edit().putBoolean("walls", walls).apply();
            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenBall++;
                if (chosenBall > balls.size()) {
                    chosenBall = 0;
                }
                updateBall();
            }
        });
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenBall--;
                if (chosenBall < 0) {
                    chosenBall = balls.size();
                }
                updateBall();
            }
        });

        leaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Пытаюсь загрузить список лидеров
                if (GoogleSignIn.getLastSignedInAccount(getApplicationContext()) != null) {
                    Games.getLeaderboardsClient(getApplicationContext(), GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
                            .getLeaderboardIntent(getString(R.string.leaderboard_id))
                            .addOnSuccessListener(new OnSuccessListener<Intent>() {
                                @Override
                                public void onSuccess(Intent intent) {
                                    startActivityForResult(intent, RC_LEADERBOARD_UI);
                                }
                            });
                }

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {

//Once the user is signed in, do something, for example hide the ‘Sign In’ button//
//TO DO//
        } else {
        }
    }

    private void updateBall() {
        ballView.setImageResource(balls.get(chosenBall));
        sp.edit().putInt("ball", chosenBall).apply();
    }

    private void loadGame() {
        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.MUSIC_KEY, music);
        intent.putExtra(MainActivity.WALL_KEY, walls);
        intent.putExtra(MainActivity.VIBRO_KEY, vibro);
        intent.putExtra(MainActivity.BALL_KEY, balls.get(chosenBall));
        MenuActivity.this.startActivity(intent);
    }

    private void initUI() {
        panel = findViewById(R.id.backPanel);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        logo = findViewById(R.id.logo);
        playButton = findViewById(R.id.playButton);
        paintButton = findViewById(R.id.paintButton);
        optionsButton = findViewById(R.id.optionsButton);
        musicView = findViewById(R.id.musicButton);
        wallsView = findViewById(R.id.ball_wall);
        vibroView = findViewById(R.id.vibrate);
        backFromOpt = findViewById(R.id.back);
        backFromCus = findViewById(R.id.backFromCus);
        left = findViewById(R.id.left);
        right = findViewById(R.id.right);
        ballView = findViewById(R.id.choose_ball);
        leaderboard = findViewById(R.id.leaderboardsButton);
    }

    private void setMenuInterface(int mode) {
        playButton.setVisibility(mode);
        optionsButton.setVisibility(mode);
        paintButton.setVisibility(mode);
        logo.setVisibility(mode);
    }

    private void setOptionInterface(int mode) {
        musicView.setVisibility(mode);
        wallsView.setVisibility(mode);
        vibroView.setVisibility(mode);
        panel.setVisibility(mode);
        backFromOpt.setVisibility(mode);
    }

    private void setPaintInterface(int mode) {
        left.setVisibility(mode);
        right.setVisibility(mode);
        ballView.setVisibility(mode);
        backFromCus.setVisibility(mode);
        panel.setVisibility(mode);
    }

    private void initPlayer() {
        musicPlayer = MediaPlayer.create(this, R.raw.menusong);
        musicPlayer.setVolume(music ? 0.2f : 0, music ? 0.2f : 0);
        musicPlayer.start();
        musicPlayer.setLooping(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (musicPlayer != null) {
            musicPlayer.release();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}
