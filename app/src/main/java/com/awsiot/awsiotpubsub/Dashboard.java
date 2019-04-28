
package com.awsiot.awsiotpubsub;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.xw.repo.BubbleSeekBar;

import org.json.JSONObject;

import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class Dashboard extends AppCompatActivity {


    static final String LOG_TAG = Dashboard.class.getCanonicalName();
    private Typeface tfLight,tfLabel;

    @Override
    public void onStart() {
        super.onStart();
        if(clientId==null)
            awsSetup();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }
    private static final String CUSTOMER_SPECIFIC_ENDPOINT = "a32de4gh5zk17w-ats.iot.us-east-2.amazonaws.com";
    private static final String COGNITO_POOL_ID = "us-east-2:01c0b7e0-d8c8-43aa-8fcc-d9b802edf4a3";
    private static final String AWS_IOT_POLICY_NAME = "mos-default";
    private static final Regions MY_REGION = Regions.US_EAST_2;
    private static final String KEYSTORE_NAME = "iot_keystore";
    private static final String KEYSTORE_PASSWORD = "password";
    private static final String CERTIFICATE_ID = "default";
    private ArrayList<Entry> values = new ArrayList<>();

    private BubbleSeekBar bubbleSeekBar;
    private float index=0.0f;
    private TextView ppmTxt,temptxt;
    private CardView dailyFacts;
    double resistance;
    int temp,humid,cppm,ppm,t=0,h=0,a=0;

    //    DataPoint[] dataPoints=new DataPoint[]{};
    LineChart graphTemp,graphHumidity,graphppm;

    TextView txtMessage;
    JSONObject jsonObj;
    ArrayList<xyValue> xyValueArrayList=new ArrayList<>();

    TextView tvClientId;
    TextView tvStatus;

    FloatingActionButton btnConnect,btnDisconnect;

    AWSIotClient mIotAndroidClient;
    AWSIotMqttManager mqttManager;
    String clientId=null;
    String keystorePath;
    String keystoreName;
    String keystorePassword;
    PointsGraphSeries<DataPoint> series;
    KeyStore clientKeyStore = null;
    String certificateId;

    CognitoCachingCredentialsProvider credentialsProvider;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ppmTxt=findViewById(R.id.ppmtxt);
        temptxt=findViewById(R.id.temperature);
        txtMessage =  findViewById(R.id.txtMessage);
        tvClientId = (TextView) findViewById(R.id.tvClientId);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        bubbleSeekBar=findViewById(R.id.seek);
        btnConnect =  findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(connectClick);
        btnConnect.setEnabled(false);
        graphTemp =  findViewById(R.id.graph);
        graphHumidity =  findViewById(R.id.graphHumid);
        graphppm =  findViewById(R.id.graphppm);
        dailyFacts=findViewById(R.id.daily_facts);
        dailyFacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard.this,Facts.class));
            }
        });

        tfLight=Typeface.createFromAsset(this.getAssets(),"font/comfortaa.ttf");
        tfLabel=Typeface.createFromAsset(this.getAssets(),"font/righteous.ttf");
        btnDisconnect =  findViewById(R.id.btnDisconnect);
        btnDisconnect.setOnClickListener(disconnectClick);
        btnDisconnect.setBackgroundTintList(ColorStateList
                .valueOf(getResources().getColor(R.color.colorPrimaryDark)));
        awsSetup();
        init();

    }

//    @Override
//    public View onCreateVi(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View v = inflater.inflate(R.layout.dashboard, container, false);
//
//
//        return v;
//    }

    private void awsSetup() {
        clientId = UUID.randomUUID().toString();
//        tvClientId.setText(clientId);

        credentialsProvider = new CognitoCachingCredentialsProvider(
                getBaseContext(), // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );

        Region region = Region.getRegion(MY_REGION);

        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT);
        mqttManager.setKeepAlive(10);
        AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament("capstone",
                "Android client lost connection", AWSIotMqttQos.QOS0);
        mqttManager.setMqttLastWillAndTestament(lwt);
        mIotAndroidClient = new AWSIotClient(credentialsProvider);
        mIotAndroidClient.setRegion(region);

        keystorePath =getBaseContext().getFilesDir().getPath();
        keystoreName = KEYSTORE_NAME;
        keystorePassword = KEYSTORE_PASSWORD;
        certificateId = CERTIFICATE_ID;

        try {
            if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, keystoreName)) {
                if (AWSIotKeystoreHelper.keystoreContainsAlias(certificateId, keystorePath,
                        keystoreName, keystorePassword)) {
                    Log.i(LOG_TAG, "Certificate " + certificateId
                            + " found in keystore - using for MQTT.");
                    // load keystore from file into memory to pass on connection
                    clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                            keystorePath, keystoreName, keystorePassword);
                    btnDisconnect.setBackgroundTintList(ColorStateList
                            .valueOf(getResources().getColor(R.color.colorPrimaryDark)));
                    btnDisconnect.setEnabled(false);
                    btnConnect.setEnabled(true);
                    btnConnect.setImageResource(R.drawable.ic_cloud_black_24dp);
                } else {
                    Log.i(LOG_TAG, "Key/cert " + certificateId + " not found in keystore.");
                }
            } else {
                Log.i(LOG_TAG, "Keystore " + keystorePath + "/" + keystoreName + " not found.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occurred retrieving cert/key from keystore.", e);
        }

        if (clientKeyStore == null) {
            Log.i(LOG_TAG, "Cert/key was not found in keystore - creating new key and certificate.");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        CreateKeysAndCertificateRequest createKeysAndCertificateRequest =
                                new CreateKeysAndCertificateRequest();
                        createKeysAndCertificateRequest.setSetAsActive(true);
                        final CreateKeysAndCertificateResult createKeysAndCertificateResult;
                        createKeysAndCertificateResult =
                                mIotAndroidClient.createKeysAndCertificate(createKeysAndCertificateRequest);
                        Log.i(LOG_TAG,
                                "Cert ID: " +
                                        createKeysAndCertificateResult.getCertificateId() +
                                        " created.");

                        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(certificateId,
                                createKeysAndCertificateResult.getCertificatePem(),
                                createKeysAndCertificateResult.getKeyPair().getPrivateKey(),
                                keystorePath, keystoreName, keystorePassword);

                        clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                                keystorePath, keystoreName, keystorePassword);

                        AttachPrincipalPolicyRequest policyAttachRequest =
                                new AttachPrincipalPolicyRequest();
                        policyAttachRequest.setPolicyName(AWS_IOT_POLICY_NAME);
                        policyAttachRequest.setPrincipal(createKeysAndCertificateResult
                                .getCertificateArn());
                        mIotAndroidClient.attachPrincipalPolicy(policyAttachRequest);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnConnect.setEnabled(true);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(LOG_TAG,
                                "Exception occurred when generating new private key and certificate.",
                                e);
                    }
                }
            }).start();
        }
    }

    View.OnClickListener connectClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Log.d(LOG_TAG, "clientId = " + clientId);

            try {
                mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                    @Override
                    public void onStatusChanged(final AWSIotMqttClientStatus status,
                                                final Throwable throwable) {
                        Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (status == AWSIotMqttClientStatus.Connecting) {
                                    tvStatus.setText("Connecting...");
                                    btnDisconnect.setBackgroundTintList(ColorStateList
                                            .valueOf(getResources().getColor(R.color.colorPrimaryDark)));
                                    btnConnect.setEnabled(false);
                                    btnDisconnect.setEnabled(false);
                                    btnConnect.setImageResource(R.drawable.ic_sync_black_24dp);

                                } else if (status == AWSIotMqttClientStatus.Connected) {
                                    tvStatus.setText("Connected");
                                    subscribe();
                                    btnConnect.setImageResource(R.drawable.ic_cloud_done_black_24dp);
                                    btnConnect.setBackgroundTintList(ColorStateList
                                            .valueOf(getResources().getColor(R.color.colorPrimaryDark)));
                                    btnDisconnect.setEnabled(true);
                                    btnDisconnect.setBackgroundTintList(ColorStateList
                                            .valueOf(getResources().getColor(R.color.red)));

                                } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                                    if (throwable != null) {
                                        Log.e(LOG_TAG, "Connection error.", throwable);
                                    }
                                    tvStatus.setText("Reconnecting");
                                } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                                    if (throwable != null) {
                                        Log.e(LOG_TAG, "Connection error.", throwable);
                                    }
                                    tvStatus.setText("Disconnected");
                                } else {
                                    tvStatus.setText("Disconnected");
                                    btnConnect.setEnabled(true);
                                    btnConnect.setImageResource(R.drawable.ic_cloud_black_24dp);
                                    btnDisconnect.setBackgroundTintList(ColorStateList
                                            .valueOf(getResources().getColor(R.color.colorPrimaryDark)));
                                    btnDisconnect.setEnabled(false);
                                }
                            }
                        });
                    }
                });
            } catch (final Exception e) {
                Toast.makeText(getBaseContext(), "Error connecting to cloud", Toast.LENGTH_SHORT).show();
                tvStatus.setText("Error! " + e.getMessage());
            }
        }
    };


    View.OnClickListener disconnectClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            disconnect();

        }
    };


    void subscribe()
    {
        final String topic = "Capstone/iot";

        Log.d(LOG_TAG, "topic = " + topic);

        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                    new AWSIotMqttNewMessageCallback() {
                        @Override
                        public void onMessageArrived(final String topic, final byte[] data) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String message = new String(data, "UTF-8");
                                        jsonObj = new JSONObject(message);
                                        temp=(int)jsonObj.get("temperature");
                                        humid=(int)jsonObj.get("humidity");
                                        resistance=(double)jsonObj.get("resistance");
                                        ppm=(int)jsonObj.get("ppm");
                                        cppm=(int)jsonObj.get("correctedPPM");
                                        bubbleSeekBar.setProgress((float)cppm);

                                        tvClientId.setText(humid+" %");
                                        ppmTxt.setText(cppm+" ppm");
                                        temptxt.setText(temp+" °C");

                                        plot(temp,graphTemp,Color.WHITE,t);
                                        plot(humid,graphHumidity,Color.CYAN,h);
                                        plot(cppm,graphppm,Color.GREEN,a);

                                        t+=5;h+=5;a+=5;


                                    } catch (Exception e) {
                                        Log.e(LOG_TAG, "Message encoding error.", e);
                                    }
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error.", e);
        }
    }
    void disconnect()
    {
        try {
            mqttManager.disconnect();
            btnConnect.setEnabled(true);
            btnConnect.setBackgroundTintList(ColorStateList
                    .valueOf(getResources().getColor(R.color.colorPrimary)));
            btnConnect.setImageResource(R.drawable.ic_cloud_black_24dp);
            btnDisconnect.setBackgroundTintList(ColorStateList
                    .valueOf(getResources().getColor(R.color.colorPrimaryDark)));
            btnDisconnect.setEnabled(false);

        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Unable to disconnect "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    void init()
    {
        btnDisconnect.setEnabled(false);
        bubbleSeekBar.setEnabled(false);
        bubbleSeekBar.setThumbColor(getResources().getColor(R.color.green));
        bubbleSeekBar.setProgress(index);

        setupGraph(graphTemp,50f,-10f);
        setupGraph(graphHumidity,30f,0f);
        setupGraph(graphppm,400f,0f);
    }


    void plot(int y,LineChart graph,int color,int cnt)
    {

        LineData data = graph.getData();

        if (data == null) {
            data = new LineData();
            graph.setData(data);
        }

        ILineDataSet set = data.getDataSetByIndex(0);

        if (set == null) {
            set = createSet(color);
            data.addDataSet(set);
        }

        data.addEntry(new Entry(cnt, y), 0);
        data.setValueTypeface(tfLabel);
        data.notifyDataChanged();
        graph.notifyDataSetChanged();


//        graph.animateX(500);
//        graph.moveViewTo(data.getEntryCount() - 3, y, YAxis.AxisDependency.LEFT);

        graph.invalidate();
    }
    private LineDataSet createSet(int color) {

        LineDataSet set = new LineDataSet(null, "");
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setCircleColor(color);
        set.setColor(color);
        set.setHighLightColor(Color.WHITE);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);
        set.setValueTextColor(color);
        return set;
    }
    void setupGraph(LineChart graph,float max,float min){

        graph.setNoDataText("No data available!");
        graph.getDescription().setEnabled(false);
        graph.setNoDataTextColor(Color.WHITE);
        graph.setTouchEnabled(true);
        graph.setDragDecelerationFrictionCoef(0.9f);
        graph.setDragEnabled(true);
        graph.setScaleEnabled(true);
        graph.setDrawGridBackground(false);
        graph.setHighlightPerDragEnabled(true);
        graph.setViewPortOffsets(60f, 40f, 60f, 40f);


        XAxis xAxis = graph.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setTypeface(tfLight);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.LTGRAY);
        xAxis.setGranularity(1f);
//        xAxis.setValueFormatter(new ValueFormatter() {
//            private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
//            @Override
//            public String getFormattedValue(float value) {
//
//                long millis = TimeUnit.HOURS.toMillis((long) value);
//                return mFormat.format(new Date(millis));
//            }
//        });


        YAxis leftAxis = graph.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setDrawLabels(true);
        leftAxis.setTypeface(tfLight);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMinimum(min);
        leftAxis.setAxisMaximum(max);
        leftAxis.setYOffset(-9f);
        leftAxis.setTextColor(Color.WHITE);

        YAxis rightAxis = graph.getAxisRight();
        rightAxis.setEnabled(false);
        graph.invalidate();


    }
}
