package me.drakeet.inmessage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;

import com.badoo.mobile.util.WeakHandler;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.umeng.analytics.MobclickAgent;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.drakeet.inmessage.adapter.MainMessageAdapter;
import me.drakeet.inmessage.api.OnItemClickListener;
import me.drakeet.inmessage.events.BusProvider;
import me.drakeet.inmessage.model.Message;
import me.drakeet.inmessage.ui.SwipeRefreshBaseActivity;
import me.drakeet.inmessage.utils.SmsUtils;
import me.drakeet.inmessage.utils.TaskUtils;
import me.drakeet.inmessage.utils.ToastUtils;
import me.drakeet.inmessage.utils.VersionUtils;

public class MainActivity extends SwipeRefreshBaseActivity {

    @InjectView(R.id.message_rv)
    ObservableRecyclerView mRecyclerView;

    NumberProgressBar mNumberProgressBar;
    MainMessageAdapter mMainMessageAdapter;
    WeakHandler mHandler;
    private int mCurrentCaptchasCount = 0;
    private List<Message> mMessages;
    private boolean mIsRefreshing = false;
    private boolean mStopDelete;
    private boolean mShowResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        BusProvider.getInstance().register(this);
        mHandler = new WeakHandler();
        mShowResult = false;
        setupActionBar();
        initRecyclerView();

        MobclickAgent.updateOnlineConfig(this);

        checkFirstTimeUse();
    }

    private void checkFirstTimeUse() {
        SharedPreferences sharedPreferences = getSharedPreferences(
                "userinfo", Context.MODE_PRIVATE
        );
        boolean isFirstStart = sharedPreferences.getBoolean("is_first_start", true);
        if (isFirstStart) {
            sharedPreferences.edit().putBoolean("is_first_start", false).apply();
            startActivity(new Intent(this, AboutActivity.class));
        }
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
    }

    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return mIsRefreshing;
                    }
                }
        );
    }

    private void setAdapter() {
        mMainMessageAdapter = new MainMessageAdapter(this, mMessages);
        mMainMessageAdapter.setShowResult(mShowResult);
        mRecyclerView.setAdapter(mMainMessageAdapter);
        mMainMessageAdapter.setOnItemClickListener(
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (mMessages.get(position).getIsMessage()) {
                            showDetailSMS(mMessages.get(position));
                        }
                    }
                }
        );
    }


    private void getAllMessage() {
        setRefreshing(true);
        mIsRefreshing = true;
        TaskUtils.executeAsyncTask(
                new AsyncTask<Object, Object, Object>() {
                    @Override
                    protected Object doInBackground(Object... params) {
                        SmsUtils smsUtils = new SmsUtils(MainActivity.this);
                        if (mMessages != null) {
                            mMessages.clear();
                            mCurrentCaptchasCount = 0;
                        }
                        mMessages = smsUtils.getAllCaptchMessages();
                        if (mMessages != null && mMessages.size() != 0) {
                            mCurrentCaptchasCount = getMessageCount();
                        }
                        return null;
                    }


                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        setAdapter();
                        setRefreshing(false);
                        mIsRefreshing = false;
                    }
                }
        );

    }

    private int getMessageCount() {
        int amount = 0;
        for (Message message : mMessages) {
            if (message.getIsMessage()) {
                amount += 1;
            }
        }
        return amount;
    }

    @OnClick(R.id.fab_delete_all)
    public void deleteAll() {
        if (VersionUtils.IS_MORE_THAN_LOLLIPOP) {
            ToastUtils.showShort("Android 5.0 以上暂不支持清空验证码短信！");
        } else {
            showConfirmDialog();
        }
    }

    private void showConfirmDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).
                setTitle("清空验证码短信？")
                .
                        setMessage(getString(R.string.str_clear_messages_hint))
                .
                        setPositiveButton(
                                "确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                        showDeleteDialog();
                                    }
                                }
                        )
                .
                        setNegativeButton(
                                "取消",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {

                                    }
                                }
                        )
                .create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    private void showDeleteDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View addLayout = layoutInflater.inflate(R.layout.view_delete_messages, null);
        mNumberProgressBar = (NumberProgressBar) addLayout.findViewById(R.id.number_progress_bar);
        AlertDialog deleteDialog = new AlertDialog.Builder(MainActivity.this).
                setTitle(
                        "正在删除验证码短信..."
                )
                .setView(addLayout)
                .
                        setNegativeButton(
                                "停止",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                        mStopDelete = true;
                                    }
                                }
                        )
                .create();
        deleteDialog.setCanceledOnTouchOutside(false);
        deleteDialog.setOnKeyListener(
                new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                            return true;
                        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                            return true;
                        } else {
                            return false; //默认返回 false

                        }
                    }
                }
        );

        deleteDialog.show();
        deleteAllCaptchasMessage(deleteDialog);
    }


    private void showBussinessDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).
                setTitle(getString(R.string.str_check_bussiness_hint))
                .
                        setMessage(getString(R.string.str_doing_hint))
                .
                        setPositiveButton(
                                "确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                    }
                                }
                        )
                .
                        create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        TaskUtils.executeAsyncTask(
                new AsyncTask<Object, Object, String>() {
                    @Override
                    protected String doInBackground(Object... params) {
                        return getBussiness();
                    }

                    @Override
                    protected void onPostExecute(String o) {
                        super.onPostExecute(o);
                        if (!o.equals("")) {
                            alertDialog.setMessage(o);
                        } else {
                            alertDialog.setMessage(getString(R.string.str_no_bussiness));
                        }
                    }
                }
        );
    }

    private String getBussiness() {
        List<String> companys = new ArrayList<>();
        String bussinessStr = "";
        List<Message> messages = DataSupport.select("companyName").find(Message.class);
        if (mMessages != null && mMessages.size() != 0) {
            for (Message message : mMessages) {
                messages.add(message);
            }
        }
        int mark = 0;
        for (Message message : messages) {
            if (message.getCompanyName() != null) {
                if (!isExist(message.getCompanyName(), companys)) {
                    if (mark != 0) {
                        bussinessStr += "\n";
                    }
                    companys.add(message.getCompanyName());
                    bussinessStr += message.getCompanyName();
                    mark += 1;
                }
            }
        }
        return bussinessStr;
    }


    private Boolean isExist(String company, List<String> companys) {
        Boolean exist = false;
        for (String c : companys) {
            if (c.equals(company)) {
                exist = true;
                break;
            }
        }
        return exist;
    }

    private void showDetailSMS(Message message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).
                setTitle(message.getSender())
                .
                        setMessage(message.getContent())
                .
                        setPositiveButton(
                                "确定",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                    }
                                }
                        )
                .
                        create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void deleteAllCaptchasMessage(final AlertDialog deleteDialog) {
        mStopDelete = false;
        final SmsUtils smsUtils = new SmsUtils(MainActivity.this);
        TaskUtils.executeAsyncTask(
                new AsyncTask<Object, Object, String>() {
                    @Override
                    protected String doInBackground(Object... params) {
                        int doAmount = 0;
                        int currentProgress;
                        for (int i = mMessages.size() - 1; i > 0; i--) {
                            if (mStopDelete) {
                                break;
                            }
                            Message message = mMessages.get(i);
                            if (message.getIsMessage()) {
                                if (message.getFromSmsDB() != 1) {
                                    smsUtils.deleteSms(message.getSmsId());
                                }
                                message.setReadStatus(1);
                                message.save();
                                doAmount += 1;
                                currentProgress = (int) ((doAmount / (float) mCurrentCaptchasCount) * 100);
                                publishProgress(currentProgress);
                            }
                        }
                        return "" + doAmount;
                    }

                    @Override
                    protected void onPostExecute(String o) {
                        super.onPostExecute(o);
                        deleteDialog.dismiss();
                        ToastUtils.showShort("成功删除" + o + "条验证码短信！");
                        getAllMessage();
                    }

                    @Override
                    protected void onProgressUpdate(Object... values) {
                        super.onProgressUpdate(values);
                        int value = Integer.parseInt(String.valueOf(values[0]));
                        mNumberProgressBar.setProgress(value);
                    }
                }
        );
    }

    @Override
    public void requestDataRefresh() {
        super.requestDataRefresh();
        getAllMessage();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHandler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        getAllMessage();
                    }
                }, 358
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem switchItem = menu.findItem(R.id.menu_show_result);
        MenuItemCompat.setActionView(switchItem, R.layout.view_switchcompat);
        final SwitchCompat switchCompat = (SwitchCompat) switchItem.getActionView()
                .findViewById(R.id.switchCompat);
        switchCompat.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (mIsRefreshing) {
                            switchCompat.setChecked(!isChecked);
                            ToastUtils.showShort("加载中请勿改变开关状态!");
                        } else {
                            SharedPreferences sharedPreferences = getSharedPreferences(
                                    "userinfo", Context.MODE_PRIVATE
                            );
                            sharedPreferences.edit().putBoolean("is_checked", isChecked).apply();
                            mShowResult = isChecked;
                            if (mMainMessageAdapter != null) {
                                mMainMessageAdapter.setShowResult(isChecked);
                                mMainMessageAdapter.notifyDataSetChanged();
                                if (isChecked)
                                    ToastUtils.showShort("开启验证码内容显示简化");
                                else
                                    ToastUtils.showShort("关闭验证码内容显示简化");
                            }
                        }
                    }
                }
        );
        initCheckStatus(switchCompat);
        return true;
    }

    private void initCheckStatus(SwitchCompat switchCompat) {
        SharedPreferences sharedPreferences = getSharedPreferences(
                "userinfo", Context.MODE_PRIVATE
        );
        boolean isChecked = sharedPreferences.getBoolean("is_checked", false);
        mShowResult = isChecked;
        switchCompat.setChecked(isChecked);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.menu_share) {
            onClickShare();
        } else if (id == R.id.menu_guess) {
            if (!mIsRefreshing) {
                showBussinessDialog();
            } else {
                ToastUtils.showShort("加载中请勿操作！");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * on share item click
     */
    public void onClickShare() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, getTitle()));
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
