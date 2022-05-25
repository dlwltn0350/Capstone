package com.android.aifoodapp.activity;

import static android.graphics.Color.*;

import static com.android.aifoodapp.interfaceh.baseURL.url;

import static java.lang.Thread.sleep;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.aifoodapp.ProgressDialog;
import com.android.aifoodapp.R;
import com.android.aifoodapp.adapter.MealAdapter;
import com.android.aifoodapp.domain.dailymeal;
import com.android.aifoodapp.domain.fooddata;
import com.android.aifoodapp.domain.meal;
import com.android.aifoodapp.domain.user;
import com.android.aifoodapp.interfaceh.OnCameraClick;
import com.android.aifoodapp.interfaceh.OnEditMealHeight;
import com.android.aifoodapp.interfaceh.OnGalleryClick;
import com.android.aifoodapp.interfaceh.RetrofitAPI;
import com.android.aifoodapp.vo.MealMemberVo;

import com.android.aifoodapp.vo.SubItem;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.kakao.sdk.user.UserApiClient;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Query;

public class MainActivity<Unit> extends AppCompatActivity implements SensorEventListener {

    Activity activity;
    Button btn_logout;
    TextView tv_userId, stepCountView;
    ImageView tv_userPhoto;

    GoogleSignInClient mGoogleSignInClient;

    TextView tv_month, tv_dialog_result;
    LinearLayout layout_date, layout_calories;
    RecyclerView rv_meal;
    Button btn_select_calendar;
    ProgressBar pb_total_calories, pb_carbohydrate, pb_protein, pb_fat;
    TextView tv_total_calories, tv_gram_of_carbohydrate, tv_gram_of_protein, tv_gram_of_fat;
    ImageView iv_user_setting_update;
    //Button btn_meal1_detail;

    RadarChart radarChart;
    SensorManager sensorManager;
    Sensor stepCountSensor;

    @SerializedName("prediction")
    String foodName="";
    fooddata foodAI;

    int percent_of_carbohydrate;
    int percent_of_protein;
    int percent_of_fat;
    Integer[] calories=new Integer[7];
    user user;
    dailymeal dailymeal;
    long dailymealid;
    String date_string="";
    int time=0;
    int currentSteps = 0 , firstSteps=0, totalSteps=0 ;
    HashMap<String, RequestBody> mapAi = new HashMap<>();

    private RecyclerView rv_item;
    private MealAdapter mealAdapter;
    private ArrayList<MealMemberVo> memberList;
    private List<SubItem> subItemList;
    FloatingActionButton fab_add_meal;
    private int meal_num = 1; // 식사 레이아웃 추가 시 증가되는 값

    ArrayList<fooddata> foodList=new ArrayList<>();
    ArrayList<Double> intakeList=new ArrayList<>();
    ArrayList<String> photoList=new ArrayList<>();
    List<fooddata> list;
    List<meal> ml;

    private String flag;
    public static Activity _MainActivity;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); //데이트 포맷(sql)
    //Camera & Gallery
    private final int REQUEST_WRITE_EXTERNAL_STORAGE = 1005;
    private final int REQUEST_READ_EXTERNAL_STORAGE = 1006;

    private Uri photoUri;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public interface OnSetImage {
        void onSetImage(Uri photoUri);
    }

    private OnSetImage onSetImage;

    public void setOnSetImageListener(OnSetImage onSetImageListener) {
        this.onSetImage = onSetImageListener;
    }

    //카메라 촬영 회전시 오류 해결
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private Button btn_weekly_report;
    //주간 통계 버튼(임시로 위치시킴 --> 후에 푸시알림으로 확인가능하도록)
    private Button btn_recommend_meal;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        _MainActivity = MainActivity.this;
        Intent intent = getIntent();
        user = intent.getParcelableExtra("user");
        setting();
        addListener();
        //settingFoodListAdapter();

        /* 걸음수 센서 측정 */
        // TYPE_STEP_COUNTER : 앱 종료와 관계없이 계속 기존의 값을 가지고 있다 +1
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepCountSensor == null) {
            Log.e("sensor","없음");
        }

        flag=intent.getStringExtra("flag"); //현재 계정이 구글인지 카카오인지

        if(flag.equals("kakao")){
            btn_logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UserApiClient.getInstance().logout((throwable) -> {
                        if (throwable != null) {
                            Toast.makeText(getApplicationContext(),"카카오 로그아웃 실패", Toast.LENGTH_SHORT);
                            Log.e("[카카오] 로그아웃", "실패", throwable);
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"카카오 로그아웃", Toast.LENGTH_SHORT);
                            Log.i("[카카오] 로그아웃", "성공");
                            ((LoginActivity)LoginActivity.mContext).updateKakaoLoginUi();
                        }
                        return null;
                    });

                    //Intent backIntent = new Intent(getApplicationContext(), LoginActivity.class); // 로그인 화면으로 이동
                    Intent backIntent = new Intent(activity, LoginActivity.class); // 로그인 화면으로 이동
                    startActivity(backIntent);
                    finish();

                }
            });
        }
        else if(flag.equals("google")){
            /*
             modify*/
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            // Build a GoogleSignInClient with the options specified by gso.
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


            //로그아웃 코드
            btn_logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.btn_logout:
                            signOut();
                            Intent backIntent = new Intent(activity, LoginActivity.class); // 로그인 화면으로 이동
                            startActivity(backIntent);
                            finish();
                            break;
                    }
                }
            });
        }
        /*juhee modify--fin*/

        //로그인 닉네임
        tv_userId.setText(user.getNickname());

    }

    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(MainActivity.this,"Signed Out ok ",Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    //변수 초기화
    private void initialize(){
        activity = this;
        btn_logout = findViewById(R.id.btn_logout);
        tv_userId=findViewById(R.id.tv_userId);

        layout_date = findViewById(R.id.layout_date);
        tv_month = findViewById(R.id.tv_month);
        layout_calories = findViewById(R.id.layout_calories);
        btn_select_calendar = findViewById(R.id.btn_select_calendar);
        tv_dialog_result = findViewById(R.id.tv_dialog_result);
        pb_total_calories = findViewById(R.id.pb_total_calories);
        pb_carbohydrate = findViewById(R.id.pb_carbohydrate);
        pb_protein = findViewById(R.id.pb_protein);
        pb_fat = findViewById(R.id.pb_fat);
        tv_total_calories = findViewById(R.id.tv_total_calories);
        tv_gram_of_carbohydrate = findViewById(R.id.tv_gram_of_carbohydrate);
        tv_gram_of_protein = findViewById(R.id.tv_gram_of_protein);
        tv_gram_of_fat = findViewById(R.id.tv_gram_of_fat);
        iv_user_setting_update = findViewById(R.id.iv_user_setting_update);
        //btn_meal1_detail = findViewById(R.id.btn_meal1_detail);

        radarChart = (RadarChart) findViewById(R.id.radarchart);

        rv_item = findViewById(R.id.rv_item);
        memberList = new ArrayList<>();
        mealAdapter = new MealAdapter(activity, memberList);
        fab_add_meal = findViewById(R.id.fab_add_meal);

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), cameraResultCallback);
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), galleryResultCallback);

        btn_weekly_report = findViewById(R.id.btn_weekly_report);
        btn_recommend_meal = findViewById(R.id.btn_recommend_meal);

        tv_userPhoto=findViewById(R.id.tv_userPhoto);
        Glide.with(this).load(R.drawable.heart).into(tv_userPhoto);
        //stepCountView=findViewById(R.id.stepCountView);
    }

    //설정
    private void setting(){
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);//loading
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//배경투명하게
        dialog.setCanceledOnTouchOutside(false); //주변 터치 방지
        dialog.setCancelable(false);
        dialog.show();

        setDailymeal();
        settingWeeklyCalendar();
        settingNutriProgress();
        settingBalanceGraph();
        settingMealAdapter();
        settingInitialMeal();

        dialog.dismiss();
    }

    //리스너 추가
    private void addListener(){
        btn_select_calendar.setOnClickListener(listener_select_calendar);
        iv_user_setting_update.setOnClickListener(listener_setting_update);
        //btn_meal1_detail.setOnClickListener(listener_meal1_detail);
        fab_add_meal.setOnClickListener(listener_add_meal);
        btn_weekly_report.setOnClickListener(listener_weekly_report);
        btn_recommend_meal.setOnClickListener(listener_recommend_meal);
    }

    private final View.OnClickListener listener_recommend_meal = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(activity, RecommendFoodActivity.class);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create()).build();

            RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);


            //비동기
            retrofitAPI.getOneDayMealCount(user.getId(),dailymeal.getDatekey()).enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if(response.body()==0){
                        //경고창 띠우고 넘어가지 않게
                        //Toast.makeText("음식을 하나 이상 입력하여야 식단 추천이 가능합니다.",)
                        Toast.makeText(getApplicationContext(),"음식을 하나 이상 입력하여야 식단 추천이 가능합니다.", Toast.LENGTH_SHORT).show();
                        Log.d("size가 0임 ",Integer.toString(response.body()));

                    }
                    else{
                        Log.d("size ",Integer.toString(response.body()));
                        intent.putExtra("user",user);
                        intent.putExtra("dailymeal",dailymeal);
                        startActivity(intent);
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {

                }
            });


        }
    };

    private final View.OnClickListener listener_weekly_report = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(activity, WeeklyReportActivity.class);
            intent.putExtra("user",user);
            startActivity(intent);
        }
    };

    private final View.OnClickListener listener_add_meal = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addMeal();
            setListViewHeightBasedOnChildren(rv_item);
        }
    };

    private final View.OnClickListener listener_setting_update = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(activity, UserSettingActivity.class);
            intent.putExtra("user",user);
            intent.putExtra("flag",flag);
            startActivity(intent);
        }
    };

    /*private final View.OnClickListener listener_meal1_detail = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(activity, FoodAnalysisActivity.class);
            intent.putExtra("dailymeal",dailymeal);
            startActivity(intent);
            finish();
        }
    };*/

    private final View.OnClickListener listener_select_calendar = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SelectCalendarDialog customDialog =  new SelectCalendarDialog(activity);
            customDialog.setCalendarDialogClickListener(new SelectCalendarDialog.OnCalendarDialogClickListener() {
                @Override
                public void onDoneClick(Date selectDate) {
//                  tv_dialog_result.setText(simpleDateFormat.format(selectDate) + "을 선택하셨습니다.");

                    Calendar selected_past_day = Calendar.getInstance();
                    selected_past_day.setTime(selectDate);
                    //Log.i("check1",selected_past_day.get(Calendar.YEAR)+"/"+(selected_past_day.get(Calendar.MONTH)+1)+"/"+selected_past_day.get(Calendar.DATE)+"/"+selected_past_day.get(Calendar.DAY_OF_WEEK));


                    Intent intent = new Intent(activity, MainActivity.class);


                    intent.putExtra("main_flag", true);
                    intent.putExtra("selected_year", selected_past_day.get(Calendar.YEAR));
                    intent.putExtra("selected_month", selected_past_day.get(Calendar.MONTH));
                    intent.putExtra("selected_date", selected_past_day.get(Calendar.DATE));
                    intent.putExtra("kakao_userNickName", tv_userId.getText().toString());


                    Log.e("date",dateFormat.format(selectDate));
                    intent.putExtra("selected_day",dateFormat.format(selectDate));
                    intent.putExtra("user",user);
                    intent.putExtra("flag",flag);
                    //TODO: flag는 무슨 의민인가여여ㅛ.. ㅠㅜ 카카오?구글? flase..?! 일단 flag 이름 하나 바꿈
                    /*
                        hanbyul comment:
                        flag는 초기값이 false이고 이 값은 캘린더에서 날짜를 선택한 경우에 true가 됩니다.
                        그래서 settingWeeklyCalendar()에서 이 값이 false이면 현재 오늘 날짜를 세팅하고,
                        true인 경우에는 캘린더에서 선택한 값을 전달하여 받아온 날짜를 세팅합니다.
                        일반적인 변수 flag로 변수명을 설정해서 다른 부분에서도 flag라는 변수를 이용하여 부가 데이터로 전달하여
                        충돌(?)이 발생하여, 날짜 선택 시 오류가 발생한게 아닌가 싶습니다..
                     */
                    /*
                        jisoo
                        카카오 로그인한건지 구글 로그인한건지 구분하려고 flag="kakao" or "google"로 넘겨주고 받고 있는데
                        날짜 flag 변수가 겹친것 같습니다..?
                    * */
                    startActivity(intent);

                    finish();
                }
            });
            customDialog.show();
        }
    };

    private void settingWeeklyCalendar(){
        //juhee
        String startDate = null,endDate=null;

        Calendar today = Calendar.getInstance();
        today.setFirstDayOfWeek(Calendar.MONDAY);

        Intent get_selected_data = getIntent();
        if(get_selected_data.getBooleanExtra("main_flag", false)) {
            today.set(Calendar.YEAR, get_selected_data.getIntExtra("selected_year",0));
            today.set(Calendar.MONTH, get_selected_data.getIntExtra("selected_month",0));
            today.set(Calendar.DATE, get_selected_data.getIntExtra("selected_date",0));
            tv_dialog_result.setText(String.format(String.format(today.get(Calendar.YEAR) + "/" + (today.get(Calendar.MONTH) + 1) + "/" + today.get(Calendar.DATE) + "/" + today.get(Calendar.DAY_OF_WEEK))));
            //Log.i("check2",today.get(Calendar.YEAR)+"/"+(today.get(Calendar.MONTH)+1)+"/"+today.get(Calendar.DATE)+"/"+today.get(Calendar.DAY_OF_WEEK));
        }

        Calendar day_of_this_week = Calendar.getInstance();
        day_of_this_week.setFirstDayOfWeek(Calendar.MONDAY);
        day_of_this_week.set(Calendar.WEEK_OF_YEAR, today.get(Calendar.WEEK_OF_YEAR));
        day_of_this_week.set(Calendar.YEAR, today.get(Calendar.YEAR));

        LinearLayout row_date = new LinearLayout(activity);
        row_date.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row_date.setOrientation(LinearLayout.HORIZONTAL);

        /*
            12월 마지막 주의 날짜 선택 시, Year 값은 12월의 연도, WEEK_OF_YEAR 값은 1이 되어,
            주간 캘린더가 올바르게 출력되지 않는 문제 발생 (전년도의 날짜가 출력됨)
            ex) 2021년 12월 30일 선택 --> 실제로 주간 캘린더에는 2020년 12월 - 2021년 1월의 날짜가 출력됨
            --> 이 경우 수동으로 Year 값을 수동으로 1을 증가시켜줌으로써 해결 (이하 9 라인)
            ex) 2021년 12월 30일 선택 --> 주간 캘린더에는 2021년 12월 - 2022년 1월의 날짜가 출력됨
        */
        int start = today.get(Calendar.MONTH) + 1;
        day_of_this_week.set(Calendar.DAY_OF_WEEK, 2);
        int start_of_this_week = day_of_this_week.get(Calendar.MONTH) +1;
        day_of_this_week.set(Calendar.DAY_OF_WEEK, 1);
        int end_of_this_week= day_of_this_week.get(Calendar.MONTH) +1;
        if(start == 12 && start_of_this_week == 12 && end_of_this_week == 1){
            day_of_this_week.set(Calendar.YEAR, today.get(Calendar.YEAR)+1);
        }

        int month1 = 1, month2 = 1;
        for(int i = 0 ; i < 7 ; i++) {

            day_of_this_week.set(Calendar.DAY_OF_WEEK, i+2);
            //Log.i("check3",day_of_this_week.get(Calendar.YEAR)+"/"+(day_of_this_week.get(Calendar.MONTH)+1)+"/"+day_of_this_week.get(Calendar.DATE)+"/"+day_of_this_week.get(Calendar.DAY_OF_WEEK));

            Date dat = day_of_this_week.getTime();
            if(i==0) {
                month1 = day_of_this_week.get(Calendar.MONTH) + 1;
                startDate = dateFormat.format(dat);
            }
            if(i==6) {
                month2 = day_of_this_week.get(Calendar.MONTH) + 1;
                endDate = dateFormat.format(dat);
            }

            LinearLayout col = new LinearLayout(activity);
            col.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            col.setOrientation(LinearLayout.HORIZONTAL);
            col.setGravity(Gravity.CENTER);

            TextView textView = new TextView(activity);
            textView.setText(Integer.toString(day_of_this_week.get(Calendar.DATE)));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(20);
            /*Log.i("check4",today.get(Calendar.YEAR)+"/"+(today.get(Calendar.MONTH)+1)+"/"+today.get(Calendar.DATE)+"/"+today.get(Calendar.DAY_OF_WEEK));
            Log.i("check5", String.valueOf(day_of_this_week.compareTo(today)));
            Log.i("check5", String.valueOf(day_of_this_week));
            Log.i("check5", String.valueOf(today));*/

            /* hanbyul :
                day_of_this_week.compareTo(today) == 0 사용 시,
                모두 동일하나, MILLISECOND가 1 차이나서 다르다고 나오는 문제 발생
                (발생 원인은 알아내지 못했고,  15번의 테스트 당 1번 정도씩 발생)
                --> Calendar를 Date로 변환하여 시간을 제외하고 (연도,월,일)만 비교하도록 수정함으로써 문제 해결
            */
            Date date1 = today.getTime();
            Date date2 = day_of_this_week.getTime();
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            if(dateFormat.format(date1).equals(dateFormat.format(date2))) {
                textView.setBackground(getDrawable(R.drawable.roundtv));
            }
            if(day_of_this_week.compareTo(Calendar.getInstance()) != 1)
                textView.setTextColor(WHITE);

            col.addView(textView);

            row_date.addView(col);
        }

        //https://www.daleseo.com/js-async-callback/

        /*juhee */
        //setWeelklyCalories(startDate,endDate);

        //calorie 초기화
        for(int i=0;i<7;i++)calories[i]=0;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create()).build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        try{
            calories=new calorieNetworkCall().execute(retrofitAPI.getWeeklyCalories(user.getId(),startDate,endDate)).get().toArray(new Integer[7]);
            Log.e("weekly--test", String.valueOf(calories));
        }catch(Exception e){
            e.printStackTrace();
        }

        layout_date.addView(row_date);

        int year = day_of_this_week.get(Calendar.YEAR);//마지막 날의 연도
        if(month1 == month2)
            tv_month.setText(year+"년 " + month1 + "월");
        else{// 일주일의 첫 날과 마지막 날의 달이 다른 경우
            if(month1 == 12)// 첫 날이 12월인 경우, 마지막 날의 달은 1월
                tv_month.setText( (year - 1) +"년 " +month1 + "월 - " + year + "년 "+ month2+"월");
            else
                tv_month.setText(year+"년 " +month1 + "월 - " + month2+"월");
        }

        //DB에서 하루 총 섭취칼로리 받아오기
        //int[] calories = new int[]{2100, 3800, 1200, 2200, 1000, 0, 0};
        int target_calorie = user.getTarget_calories();

        LinearLayout row_calories = new LinearLayout(activity);
        row_calories.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row_calories.setOrientation(LinearLayout.HORIZONTAL);

        for(int i = 0 ; i < 7 ; i++) {

            LinearLayout col = new LinearLayout(activity);
            col.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            col.setOrientation(LinearLayout.HORIZONTAL);
            col.setGravity(Gravity.CENTER);

            TextView textView = new TextView(activity);
            textView.setText(Integer.toString(calories[i]));
            textView.setGravity(Gravity.CENTER);
            textView.setTypeface(Typeface.createFromAsset(getAssets(), "cafe24ssurroundair.ttf"), Typeface.BOLD);
            textView.setTextSize(10);

            if(target_calorie - 50 <= calories[i] && calories[i] <= target_calorie + 50)
                textView.setTextColor(parseColor("#7CFC00"));
            else if(target_calorie - 50 > calories[i])
                textView.setTextColor(parseColor("#FFFF00"));
            else
                textView.setTextColor(parseColor("#FF4500"));

            col.addView(textView);

            row_calories.addView(col);
        }
        layout_calories.addView(row_calories);
    }

    private void settingNutriProgress(){
        /*
            회원가입 후, 초기 개인정보 화면에서 넘어온 경우, 값을 바로 받아서 적용한다.(InitialSurveyActivity에서 DB에 저장되어야 함)
            DB 생성 후 이하 getIntent()에서 받아오는 HashMap은 사용하지 않아도 됨
         */
        int target_calorie, gram_of_carbohydrate, gram_of_protein, gram_of_fat;

        Intent intent = getIntent();
        /*
        Log.i("HashMapTest", user.pringStirng());
        if(intent.getSerializableExtra("survey_result") != null){
            HashMap<String, Integer> survey_result = (HashMap<String, Integer>)intent.getSerializableExtra("survey_result");
            Log.i("HashMapTest", String.valueOf(survey_result));

            target_calorie = survey_result.get("target_calorie");
            gram_of_carbohydrate = survey_result.get("gram_of_carbohydrate");
            gram_of_protein = survey_result.get("gram_of_protein");
            gram_of_fat = survey_result.get("gram_of_fat");


        }else{
            //이미 회원인 상황에서 로그인을 한 경우에, DB에서 받아와야하는 값
            target_calorie = 2000;
            gram_of_carbohydrate = 100;
            gram_of_protein = 100;
            gram_of_fat = 100;
        }

        int current_calories = 1000;
        int current_carbohydrate = 50;
        int current_protein = 30;
        int current_fat = 20;
        */
        target_calorie = user.getTarget_calories();

        gram_of_carbohydrate = (int) (target_calorie * 0.5 / 4);
        gram_of_protein = (int) (target_calorie * 0.3 / 4);
        gram_of_fat = (int) (target_calorie * 0.2 / 9);

        int current_calories = dailymeal.getCalorie();
        int current_carbohydrate = dailymeal.getCarbohydrate();
        int current_protein = dailymeal.getProtein();
        int current_fat = dailymeal.getFat();


        tv_total_calories.setText(current_calories + " / " + target_calorie + "kcal");
        tv_gram_of_carbohydrate.setText(current_carbohydrate + " / " + gram_of_carbohydrate + "g");
        tv_gram_of_protein.setText(current_protein + " / " + gram_of_protein + "g");
        tv_gram_of_fat.setText(current_fat + " / " + gram_of_fat + "g");

        int percent_of_total_calories = (int) ((current_calories*100.0)/target_calorie);
        pb_total_calories.setProgress(percent_of_total_calories);

        if(percent_of_total_calories > 100)
            pb_total_calories.setProgressTintList(ColorStateList.valueOf(Color.RED));

        percent_of_carbohydrate = (int) ((current_carbohydrate*100.0)/gram_of_carbohydrate);
        percent_of_protein = (int) ((current_protein*100.0)/gram_of_protein);
        percent_of_fat = (int) ((current_fat*100.0)/gram_of_fat);

/*        //Test Data
        percent_of_carbohydrate = 150;
        percent_of_protein = 80;
        percent_of_fat = 101;*/

        pb_carbohydrate.setProgress(percent_of_carbohydrate);
        pb_protein.setProgress(percent_of_protein);
        pb_fat.setProgress(percent_of_fat);

        if(percent_of_carbohydrate > 100)
            pb_carbohydrate.setProgressTintList(ColorStateList.valueOf(Color.RED));
        if(percent_of_protein > 100)
            pb_protein.setProgressTintList(ColorStateList.valueOf(Color.RED));
        if(percent_of_fat > 100)
            pb_fat.setProgressTintList(ColorStateList.valueOf(Color.RED));

    }

    private void settingBalanceGraph(){

        /*밸런스 그래프 출력*/
        ArrayList<RadarEntry> visitors = new ArrayList<>();

        visitors.add(new RadarEntry(percent_of_carbohydrate));
        visitors.add(new RadarEntry(percent_of_fat));
        visitors.add(new RadarEntry(percent_of_protein));

/*      // yAxis 최댓값을 100으로 지정한 경우, 영양소 섭취 퍼센트가 100% 초과 시 100이 되도록 지정
        visitors.add(new RadarEntry((percent_of_carbohydrate > 100)? 100 : percent_of_carbohydrate));
        visitors.add(new RadarEntry((percent_of_fat > 100)? 100 : percent_of_fat));
        visitors.add(new RadarEntry((percent_of_protein > 100)? 100 : percent_of_protein));
*/

        // 영양소 섭취량이 권장량을 초과하는 경우, Red Color로 변경 (일반 BLUE)
        int color_of_carbonhydrate = (percent_of_carbohydrate > 100)? RED : BLUE;
        int color_of_fat = (percent_of_fat > 100)? RED : BLUE;
        int color_of_protein = (percent_of_protein > 100)? RED : BLUE;

        RadarDataSet radarDataSet = new RadarDataSet(visitors, "visitors");
        radarDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return super.getFormattedValue((int)value);
            }
        });

        radarDataSet.setHighlightLineWidth(2f);
        radarDataSet.setColor(parseColor("#3A531C"));
        radarDataSet.setLineWidth(2f);
        radarDataSet.setValueTextSize(16f);
        radarDataSet.setDrawFilled(true);

        IntegerPercentFormatter integerPercentFormatter = new IntegerPercentFormatter(radarChart);
        radarDataSet.setValueFormatter(integerPercentFormatter);
        //radarDataSet.setFillColor(parseColor("#3A531C"));
        //Log.i("check", percent_of_carbohydrate+","+percent_of_fat+","+percent_of_protein);
        // 모든 영양소 섭취량이 권장량을 초과하는 경우, Red Color로 변경 (일반 Dark Green)
        if(percent_of_carbohydrate>100 && percent_of_fat>100 && percent_of_protein > 100){
            radarDataSet.setFillColor(RED);
        }else{
            radarDataSet.setFillColor(parseColor("#3A531C"));
        }

        RadarData radarData = new RadarData();
        radarData.addDataSet(radarDataSet);
        radarData.setValueTextColors(Arrays.asList(new Integer[]{color_of_carbonhydrate, color_of_fat, color_of_protein}));
        //radarData.setValueTextSize(10f);

        String[] labels = {"탄수화물","지방","단백질"};

        XAxis xAxis = radarChart.getXAxis();
        xAxis.setTextSize(13f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setTypeface(Typeface.createFromAsset(getAssets(), "cafe24ssurroundair.ttf"));
        YAxis yAxis = radarChart.getYAxis();

        yAxis.setAxisMinimum(0f);
        //yAxis.setAxisMaximum(100f); // yAxis 최대값 지정 (100 초과 시, chart 영역 벗어남)
        //yAxis.setLabelCount(5, true); // web 개수 강제 지정
        yAxis.setDrawLabels(false); //yAxis 기준 값 표시

        radarChart.setTouchEnabled(false);
        // chart 영역 위치 조정
        radarChart.setScaleX(1.4f);
        radarChart.setScaleY(1.4f);
        radarChart.setY(80f);

        radarChart.getDescription().setEnabled(false);
        radarChart.getLegend().setEnabled(false);

        radarChart.setWebColorInner(parseColor("#3A531C"));
        radarChart.setWebLineWidthInner(1f);
        radarChart.setWebColor(parseColor("#3A531C"));

        radarChart.setData(radarData);
        radarChart.invalidate();

    }

    private void settingMealAdapter(){

        /* 상위 리사이클러뷰 설정*/
        rv_item.setAdapter(mealAdapter);
        GridLayoutManager gManager = new GridLayoutManager(activity, 1);
        rv_item.setLayoutManager(gManager);

        // adapter 콜백리스너 등록
        if (mealAdapter != null) {
            mealAdapter.setOnEditMealHeightListener(new OnEditMealHeight() {
                @Override
                public void onEditMealHeight() {
                    setListViewHeightBasedOnChildren(rv_item);
                }
            });

            mealAdapter.setOnCameraClickListener(new OnCameraClick() {
                @Override
                public void onCameraClick() {

                    if(
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    ){
                        String[] permissions = {
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        };
                        requestPermissions(permissions, REQUEST_WRITE_EXTERNAL_STORAGE);
                    }else{
                        loadCameraImage();
                    }
                }
            });

            mealAdapter.setOnGalleryClickListener(new OnGalleryClick() {
                @Override
                public void onGalleryClick() {

                    if(
                            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    ){
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, REQUEST_READ_EXTERNAL_STORAGE);
                    }else{
                        loadGalleryImage();
                    }
                }
            });

            mealAdapter.setItemClickListener(new MealAdapter.ItemClickListener() {
                @Override
                public void onDetailButtonClicked(int position){//position은 0부터 시작

                    //해당 list position에 저장되어 있는 foodList 불러오기
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(url)
                            .addConverterFactory(GsonConverterFactory.create()).build();

                    RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

                    /*
                    try{
                        list=new NetworkCall().execute(retrofitAPI.getFoodFromMeal(dailymeal.getUserid(),dailymeal.getDatekey(),position)).get();
                        for(fooddata fd : list){
                            foodList.add(fd);
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                    }*/

                    retrofitAPI.getFoodFromMeal(dailymeal.getUserid(),dailymeal.getDatekey(),position).enqueue(new Callback<List<fooddata>>() {
                        @Override
                        public void onResponse(Call<List<fooddata>> call, Response<List<fooddata>> response) {
                            if(response.isSuccessful()) {
                                list=response.body();
                                //Log.e("@@@@",list.toString());
                                for(fooddata fd : list){
                                    foodList.add(fd);
                                }

                                Intent intent = new Intent(activity, FoodAnalysisActivity.class);
                                intent.putExtra("dailymeal",dailymeal);
                                intent.putExtra("position",position);
                                //intent.putExtra("intakeList",intakeList);
                                //intent.putExtra("photoList",photoList);
                                intent.putParcelableArrayListExtra("foodList",foodList);
                                startActivity(intent);
                                finish();
                            }
                        }
                        @Override
                        public void onFailure(Call<List<fooddata>> call, Throwable t) {
                            Log.e("food search","실패"+t);
                        }
                    });

                }
                @Override
                public void removeButtonClicked(int position){

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();

                    RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

                    Log.e("날짜",date_string);
                    retrofitAPI.InitPositionMeal(user.getId(),date_string,position).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if(response.isSuccessful()) {
                                Log.e("main 삭제 식사 : ",Integer.toString(position));

                            }
                            else{
                                Log.e("main 식사","삭제실패");
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("","call 실패"+t);
                        }
                    });

                    Toast.makeText(getApplicationContext(),"삭제되었습니다!", Toast.LENGTH_SHORT).show();

                    /* activity 새로고침하기 */
                    finish();//인텐트 종료
                    overridePendingTransition(0, 0);//인텐트 효과 없애기
                    Intent intent = getIntent(); //인텐트
                    startActivity(intent); //액티비티 열기
                    overridePendingTransition(0, 0);//인텐트 효과 없애기
                }
                @Override
                public void mealSaveFromPhoto(byte[] byteArray, int position, Bitmap compressedBitmap, Uri photoUri, String tmp){
                    // AI 통신 (음식 이름을 넘겨 받는다)
                    Gson gson=new GsonBuilder().setLenient().create();

                    Retrofit retrofit2 = new Retrofit.Builder()
                            .baseUrl("http://hjb-desktop.asuscomm.com:12345/").addConverterFactory(GsonConverterFactory.create(gson)).build();

                    RetrofitAPI retrofitAPI2 = retrofit2.create(RetrofitAPI.class);

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();

                    RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

                    File postFile;

                    if(tmp.equals("gallery")){
                        //상대경로에서 절대경로로 변경 https://crazykim2.tistory.com/441
                        String imgPath=getRealPathFromURI(photoUri);
                        postFile=new File(imgPath);
                        //File postFile=new File(photoUri.getPath());
                        //Log.e("jaja",postFile.getName());
                    /*
                    OutputStream out = null;
                    try{
                        postFile.createNewFile();
                        out=new FileOutputStream(postFile);
                        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

                    }catch(Exception e){
                        e.printStackTrace();
                    }finally{
                        try{
                            out.close();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }*/
                    }
                    else{ //camera
                        postFile=new File(photoUri.getPath());
                    }

                    ProgressDialog dialog = new ProgressDialog(MainActivity.this);//loading
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));//배경투명하게
                    dialog.setCanceledOnTouchOutside(false); //주변 터치 방지
                    dialog.setCancelable(false);
                    dialog.show();

                    RequestBody rb = RequestBody.create(MediaType.parse("multipart/form-data"),postFile);
                    MultipartBody.Part Bmp = MultipartBody.Part.createFormData("file", postFile.getName(), rb);

                    retrofitAPI2.postFoodNameFromAI(Bmp).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if(response.isSuccessful()) {
                                foodName=response.body();

                                // AI에서 넘겨받은 음식이름으로 영양소 정보를 얻기 위한 fooddata 받아오기
                                retrofitAPI.getFoodFromFoodName(foodName).enqueue(new Callback<fooddata>() {
                                    @Override
                                    public void onResponse(Call<fooddata> call, Response<fooddata> response) {
                                        foodAI=response.body();

                                        //해당 position 위치에 이미 저장되어 있는 foodList 불러오기
                                        retrofitAPI.getFoodFromMeal(dailymeal.getUserid(),dailymeal.getDatekey(),position).enqueue(new Callback<List<fooddata>>() {
                                            @Override
                                            public void onResponse(Call<List<fooddata>> call, Response<List<fooddata>> response) {
                                                if(response.isSuccessful()) {
                                                    list=response.body();
                                                    //Log.e("@@@@",list.toString());
                                                    for(fooddata fd : list){
                                                        foodList.add(fd);
                                                    }
                                                    foodList.add(foodAI); //기존 저장되어 있는 foodList에 추가
                                                    dialog.dismiss();

                                                    Intent intent = new Intent(activity, FoodAnalysisActivity.class);
                                                    intent.putExtra("dailymeal",dailymeal);
                                                    intent.putExtra("position",position);
                                                    //intent.putExtra("intakeList",intakeList);
                                                    intent.putExtra("photoAI",photoUri);//uri로 옮기기
                                                    //intent.putExtra("image",byteArray); //사진 넘기기
                                                    intent.putParcelableArrayListExtra("foodList",foodList);
                                                    intent.putExtra("activity","MainActivity");//어느 액티비티에서 넘어왔는지
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                            @Override
                                            public void onFailure(Call<List<fooddata>> call, Throwable t) {
                                                Log.e("food search","실패"+t);
                                            }
                                        });
                                    }
                                    @Override
                                    public void onFailure(Call<fooddata> call, Throwable t) {
                                        Log.e("",t.toString());
                                    }
                                });

                            }
                            else{
                                Log.e("","!response.isSuccessful()");
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.e("AI 모델 통신",t.toString());
                            dialog.dismiss();
                            Toast toast;
                            toast = Toast.makeText(activity, t.toString(), Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });

                }
            });
        }
    }
    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(okhttp3.MultipartBody.FORM, descriptionString);
    }

    //갤러리에서 불러오기
    private String getRealPathFromURI(Uri contentUri){
//        if(contentUri.getPath().startsWith("/storage")){
//            return contentUri.getPath();
//        }
        String id= DocumentsContract.getDocumentId(contentUri).split(":")[1];
        String[] columns={MediaStore.Files.FileColumns.DATA};
        String selection=MediaStore.Files.FileColumns._ID+" = "+id;
        Cursor cursor=activity.getContentResolver().query(MediaStore.Files.getContentUri("external"),columns,selection,null,null);
        try{
            int columnIndex=cursor.getColumnIndex(columns[0]);
            if(cursor.moveToFirst()){
                return cursor.getString(columnIndex);
            }
        }finally{
            cursor.close();
        }
        return null;
    }

    private void settingInitialMeal(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create()).build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        //addMeal();
        try{
            time=new timeFlagNetworkCall().execute(retrofitAPI.getTimeFlag(user.getId(),date_string)).get();
        }catch(Exception e){
            e.printStackTrace();
        }

        for(int i=0;i<=time;i++){
            addMeal();
        }
    }

    private void addMeal(){
        //mealAdapter.addItem(new MealMemberVo());
        mealAdapter.addItem(new MealMemberVo("  식사 " + meal_num, buildSubItemList(meal_num)));
        meal_num++;
        setListViewHeightBasedOnChildren(rv_item);
    }

    /* 중첩 리사이클러뷰 : https://stickode.tistory.com/271
     * 현재 식사 위치에 저장된 음식 목록을 출력하기 위함.
     * */
    private List<SubItem> buildSubItemList(int meal_num) {
        subItemList=new ArrayList<>();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create()).build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        try{
            ml=new MealNetworkCall().execute(retrofitAPI.getMeal(user.getId(),date_string,meal_num-1)).get();
            for (meal repo : ml) {
                SubItem subItem=new SubItem(repo.getMealname(), repo.getMealphoto());
                subItemList.add(subItem);
                //Log.e("mealname",repo.getMealname());
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        mealAdapter.notifyDataSetChanged();

        return subItemList;
    }


    public void setListViewHeightBasedOnChildren(RecyclerView recyclerView) {

        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();

        int cnt = recyclerView.getAdapter().getItemCount();
        params.height = (int) getResources().getDimension(R.dimen.meal_item_size) * cnt;
        recyclerView.requestLayout();
    }

    public void setDailymeal(){

        //https://m.blog.naver.com/PostView.naver?isHttpsRedirect=true&blogId=gracefulife&logNo=220992369673
        //https://velog.io/@ptm0304/Android-Retrofit%EC%9C%BC%EB%A1%9C-DateTime-%ED%98%95%EC%8B%9D-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EB%B0%9B%EC%95%84%EC%98%A4%EA%B8%B0
        //GsonBuilder gson = new GsonBuilder();
        //gson.registerTypeAdapter(Date.class , new GsonDateFormatAdapter());

        //Gson gson = new GsonBuilder()
        //        .setDateFormat("yyyy-MM-dd")
        //        .create();
        //"2022-04-16T15:00:00.000+00:00"//"2022-04-16T15:00:00.000+00:00",
        Intent intent = getIntent();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create()).build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        //String date_string="";
        //위에서 캘린더 세팅부분에서 오늘날짜를 보내주면 이렇게 하지 않고 세팅 가능 (selecday intent 에 넣는거 삭제해야함)
        if(intent.getStringExtra("selected_day")!=null){
            date_string = intent.getStringExtra("selected_day");
        }
        else{
            //현재 시간을 yyyy-MM-dd HH:mm:ss 포맷으로 저장하는 코드
            Date now = new Date(); //Date타입으로 변수 선언
            date_string = dateFormat.format(now); //날짜가 string으로 저장
        }

        try{
            dailymeal=new getDailyMealNetworkCall().execute(retrofitAPI.getDailyMeal(user.getId(),date_string)).get();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private ActivityResultCallback<ActivityResult> cameraResultCallback = new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK){
                activity.getWindow().getDecorView().setEnabled(false);

                Toast toast;
                toast = Toast.makeText(activity, "잠시만 기다려주세요!", Toast.LENGTH_LONG);
                toast.show();
                //img_photo.setImageURI(photoUri);
                onSetImage.onSetImage(photoUri);
                toast.cancel();
                activity.getWindow().getDecorView().setEnabled(true);

/*                Intent intent = new Intent(activity, FoodAnalysisActivity.class);
                intent.putExtra("full_meal_img", photoUri);
                startActivity(intent);*/

            }
        }
    };

    private ActivityResultCallback<ActivityResult> galleryResultCallback = new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            if(result.getResultCode() == RESULT_OK){
                activity.getWindow().getDecorView().setEnabled(false);

                Toast toast;
                toast = Toast.makeText(activity, "잠시만 기다려주세요!", Toast.LENGTH_LONG);
                toast.show();

                Uri uri = result.getData().getData();
                //img_photo.setImageURI(uri);
                onSetImage.onSetImage(uri);
                toast.cancel();
                activity.getWindow().getDecorView().setEnabled(true);
            }
        }
    };

    private void loadCameraImage(){
        try{

            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String tmpFileName = timestamp;

            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File photoFile = File.createTempFile(tmpFileName, ".jpg", storageDir);

            photoUri = FileProvider.getUriForFile(activity, getPackageName(),photoFile);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            cameraLauncher.launch(intent);

            Log.i("file_name", Environment.getExternalStorageDirectory().getAbsolutePath()+"/Pictures/"+photoFile.getName());

        }catch(Exception ex){

        }
    }

    private void loadGalleryImage(){
        try{
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            galleryLauncher.launch(intent);

        }catch(Exception ex){

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0){

            if(requestCode == REQUEST_WRITE_EXTERNAL_STORAGE){

                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    loadCameraImage();
                }else{
                    requestUserPermission();
                }

            }else if(requestCode == REQUEST_READ_EXTERNAL_STORAGE){

                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    loadGalleryImage();
                }else{
                    requestUserPermission();
                }
            }
        }
    }

    private void requestUserPermission(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);

        alertDialog.setIcon(R.drawable.alert_icon);
        alertDialog.setTitle("권한");
        alertDialog.setMessage("권한 허용하시겠습니까?");

        alertDialog.setPositiveButton("허용",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){

                //알림창의 확인 버튼 클릭
                Intent appDetail = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:"+getPackageName()));
                appDetail.addCategory(Intent.CATEGORY_DEFAULT);
                appDetail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(appDetail);
            }
        });

        alertDialog.setNegativeButton("거부",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int which){

            }
        });

        alertDialog.show();
    }

    /* 비동기적 처리
    public void setWeelklyCalories(String startDate, String endDate){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create()).build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

        retrofitAPI.getWeeklyCalories(user.getId(),startDate,endDate).enqueue(new Callback<List<Integer>>() {
            @Override
            public void onResponse(Call<List<Integer>> call, Response<List<Integer>> response) {
                calories = response.body().toArray(new Integer[7]);
                try {
                    Log.e("weekly", String.valueOf(calories));
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<List<Integer>> call, Throwable t) {
                Log.e("Error! call msg",call.toString());
                Log.e("Error! t messge",t.getMessage());
            }
        });
    }*/

    /* AsyncTask 처리 부분*/
    private class calorieNetworkCall extends AsyncTask<Call, Void, List<Integer>>{
        @Override
        protected List<Integer> doInBackground(Call[] params) {

            try {
                Call<List<Integer>> call = params[0];
                Response<List<Integer>> response = call.execute();
                return response.body();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class MealNetworkCall extends AsyncTask<Call, Void, List<meal>>{
        @Override
        protected List<meal> doInBackground(Call[] params) {
            try {
                Call<List<meal>> call = params[0];
                Response<List<meal>> response = call.execute();
                return response.body();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private class timeFlagNetworkCall extends AsyncTask<Call, Void, Integer>{
        @Override
        protected Integer doInBackground(Call[] params) {
            try {
                Call<Integer> call = params[0];
                Response<Integer> response = call.execute();
                return response.body();


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class getDailyMealNetworkCall extends AsyncTask<Call, Void, dailymeal > {
        @Override
        protected dailymeal doInBackground(Call[] params) {
            try {
                Call<dailymeal> call = params[0];
                Response<dailymeal> response = call.execute();
                return response.body();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    int p = 1;
    public class IntegerPercentFormatter extends ValueFormatter {

        private final RadarChart chart;

        public IntegerPercentFormatter(RadarChart chart) {
            this.chart = chart;
        }

        @Override
        public String getFormattedValue(float value) {
            String pr = "";
            int val = (int) value;

            switch(p){
                case 1://탄수화물
                    if(val == 0)
                        pr = "              c:0";
                    else if(val <= 5)
                        pr = "              c:"+ val +"%";
                    else
                        pr = "      c:"+ val +"%";
                    break;
                case 2://지방
                    if(val == 0)
                        pr = "       f:0";
                    else if(val <= 5)
                        pr = "       f:"+ val +"%";
                    else
                        pr = "      f:"+ val +"%";
                    break;
                case 3://단백질
                    if(val == 0)
                        pr = "p:0";
                    else
                        pr = "p:"+val +"%";
                    break;
                default:
                    break;
            }
            p++;

            /*
                한번씩 삼각그래프 탄단지 값이 출력되지 않는 이슈
                case 1 ) 메인페이지에서 주간통계 페이지로 이동했다가 다시 돌아온 경우, 값이 사라져있었음
                        --> 해결
            */
            if(p == 4)
                p =1;


            return pr;
        }
    }

    public void onStart() {
        super.onStart();
        if(stepCountSensor !=null) {
            // 센서 속도 설정
            // * 옵션
            // - SENSOR_DELAY_NORMAL: 20,000 초 딜레이
            // - SENSOR_DELAY_UI: 6,000 초 딜레이
            // - SENSOR_DELAY_GAME: 20,000 초 딜레이
            // - SENSOR_DELAY_FASTEST: 딜레이 없음
            //
            sensorManager.registerListener(this,stepCountSensor,SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 걸음 센서 이벤트 발생시
        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            if(firstSteps<1){
                firstSteps=(int) event.values[0];
            }

            // 현재 값 = (리셋 안 된 값 + 현재 값) - 리셋 안 된 값 + db에 저장된 그날의 걸음수 불러와서
            //currentSteps = (int)event.values[0] - firstSteps + dailymeal.getStepcount();
            currentSteps = (int)event.values[0] - firstSteps;
            startSensorService();

            //stepCount db에 저장
            /*
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create()).build();
            RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);

            retrofitAPI.setStepCount(dailymeal.getUserid(),dailymeal.getDatekey(),currentSteps).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e("오류",t.toString());
                }
            });*/
            //stepCountView.setText(String.valueOf(currentSteps));
        }

    }

    public void startSensorService(){
        Intent serviceIntent = new Intent(this, stepService.class);
        startService(serviceIntent);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

