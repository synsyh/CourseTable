package com.example.sunning.listviewtest;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nd.leiyi.crims.R;
import nd.leiyi.crims.adapter.CourseInfoAdapter;
import nd.leiyi.crims.appException.AppException;
import nd.leiyi.crims.constant.UserInfo;
import nd.leiyi.crims.db.CourseInfoDBManager;
import nd.leiyi.crims.gallery3D.CourseInfoGallery;
import nd.leiyi.crims.http.CourseInfoFetcher;
import nd.leiyi.crims.model.CourseInfo;
import nd.leiyi.crims.util.CourseSettingUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class CourseTableActivity extends Activity {

    /** 标题栏文字 */
    protected TextView textTitle;
    /** 第一个无内容的格子 */
    protected TextView empty;
    /** 星期一的格子 */
    protected TextView monColum;
    /** 星期二的格子 */
    protected TextView tueColum;
    /** 星期三的格子 */
    protected TextView wedColum;
    /** 星期四的格子 */
    protected TextView thrusColum;
    /** 星期五的格子 */
    protected TextView friColum;
    /** 星期六的格子 */
    protected TextView satColum;
    /** 星期日的格子 */
    protected TextView sunColum;
    /** 课程表body部分布局 */
    protected RelativeLayout course_table_layout;
    /** 选择周数弹出窗口 */
    protected PopupWindow weekListWindow;
    /** 显示周数的listview*/
    protected ListView weekListView;
    /** 选择周数弹出窗口的layout */
    protected View popupWindowLayout;
    /** 课程信息 **/
    protected Map<String, List<CourseInfo>> courseInfoMap;
    /** 保存显示课程信息的TextView **/
    protected List<TextView> courseTextViewList = new ArrayList<TextView>();
    /** 保存每个textview对应的课程信息 map,key为哪一天（如星期一则key为1） **/
    protected Map<Integer, List<CourseInfo>> textviewCourseInfoMap = new HashMap<Integer, List<CourseInfo>>();
    /** 课程格子平均宽度 **/
    protected int aveWidth;
    /** 屏幕宽度 **/
    protected int screenWidth;
    /** 格子高度 **/
    protected int gridHeight = 80;
    /** 最大课程节数 **/
    protected int maxCourseNum;

    protected Button goBackButton;

    protected ProgressDialog pDialog;

    protected Handler mhandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //设置自定义标题栏布局
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.course_table_layout);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.title);
        //设置标题栏周数
        textTitle = (TextView) this.findViewById(R.id.textTile);
        textTitle.setTextSize(20);
        textTitle.setPadding(15, 2, 15, 2);
        //右边白色倒三角
        Drawable down = this.getResources().getDrawable(R.drawable.title_down);
        down.setBounds(0, 0, down.getMinimumWidth(), down.getMinimumHeight());
        textTitle.setCompoundDrawables(null, null, down, null);
        textTitle.setCompoundDrawablePadding(2);
        //获得列头的控件
        empty = (TextView) this.findViewById(R.id.test_empty);
        monColum = (TextView) this.findViewById(R.id.test_monday_course);
        tueColum = (TextView) this.findViewById(R.id.test_tuesday_course);
        wedColum = (TextView) this.findViewById(R.id.test_wednesday_course);
        thrusColum = (TextView) this.findViewById(R.id.test_thursday_course);
        friColum = (TextView) this.findViewById(R.id.test_friday_course);
        satColum  = (TextView) this.findViewById(R.id.test_saturday_course);
        sunColum = (TextView) this.findViewById(R.id.test_sunday_course);
        //返回按钮
        goBackButton = (Button) this.findViewById(R.id.button_go_back);
        goBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //改变返回按钮的背景，体现出被“按下出”的感觉
                goBackButton.setBackgroundDrawable(CourseTableActivity.this
                        .getResources().getDrawable(R.drawable.arrow_left_down));
                // 恢复背景
                mhandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        goBackButton.setBackgroundDrawable(CourseTableActivity.this
                                .getResources().getDrawable(R.drawable.arrow_left));
                    }
                }, 200);
                mhandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 400);

            }
        });
        // 列表布局文件
        course_table_layout = (RelativeLayout) this.findViewById(R.id.test_course_rl);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //屏幕宽度
        int width = dm.widthPixels;

        //平均宽度
        int aveWidth = width / 8;
        //给列头设置宽度
        empty.setWidth(aveWidth * 3/4);
        monColum.setWidth(aveWidth * 33/32 + 1);
        tueColum.setWidth(aveWidth * 33/32 + 1);
        wedColum.setWidth(aveWidth * 33/32 + 1);
        thrusColum.setWidth(aveWidth * 33/32 + 1);
        friColum.setWidth(aveWidth * 33/32 + 1);
        satColum.setWidth(aveWidth * 33/32 + 1);
        sunColum.setWidth(aveWidth * 33/32 + 1);
        this.screenWidth = width;
        this.aveWidth = aveWidth;
        //初始化body部分
        init();
    }
    /**
     * 初始化课程表body部分
     * @param aveWidth
     */
    protected void init(){


        //获取课表配置信息
        final SharedPreferences courseSettings = getSharedPreferences("course_setting", Activity.MODE_PRIVATE);
        //检测是否设置过学期
        if(courseSettings.getString("currentTerm_" + UserInfo.currentUser.getStuNum(), null) == null)
        {
            Toast.makeText(CourseTableActivity.this, "您尚未设置当前学期！快去设置吧！", Toast.LENGTH_SHORT).show();
            return;
        }
        //计算出当前的周数
        final String currentWeekStr = CourseSettingUtil.figureCurrentWeek(courseSettings);
        if(currentWeekStr.equals(""))
        {
            textTitle.setText("全部");
        }
        else
        {
            textTitle.setText("第" + currentWeekStr + "周");

        }
        //设置点击事件
        textTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                //改变背景（体现出被"按下去"的感觉
                textTitle.setBackgroundDrawable(
                        CourseTableActivity.this.getResources().getDrawable(R.drawable.title_text_bg));
                //显示弹出窗口
                showWeekListWindow(textTitle);
            }
        });
        //获取最大课程节数
        String maxCourseNumStr = courseSettings.getString("maxCourseNum_" + UserInfo.currentUser.getStuNum(), "");
        if(maxCourseNumStr.equals(""))
        {
            courseSettings.edit().putString("maxCourseNum_" + UserInfo.currentUser.getStuNum(), "12");
            maxCourseNum = 12;
        }
        else
        {
            maxCourseNum = Integer.parseInt(maxCourseNumStr);
        }
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //屏幕高度
        int height = dm.heightPixels;
        gridHeight = height / maxCourseNum;
        //设置课表界面
        //动态生成12 * maxCourseNum个textview
        for(int i = 1; i <= maxCourseNum; i ++){

            for(int j = 1; j <= 8; j ++){

                TextView tx = new TextView(CourseTableActivity.this);
                tx.setId((i - 1) * 8  + j);
                //除了最后一列，都使用course_text_view_bg背景（最后一列没有右边框）
                if(j < 8)
                    tx.setBackgroundDrawable(CourseTableActivity.this.
                            getResources().getDrawable(R.drawable.course_text_view_bg));
                else
                    tx.setBackgroundDrawable(CourseTableActivity.this.
                            getResources().getDrawable(R.drawable.course_table_last_colum));
                //相对布局参数
                RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(
                        aveWidth * 33 / 32 + 1,
                        gridHeight);
                //文字对齐方式
                tx.setGravity(Gravity.CENTER);
                //字体样式
                tx.setTextAppearance(this, R.style.courseTableText);
                //如果是第一列，需要设置课的序号（1 到 12）
                if(j == 1)
                {
                    tx.setText(String.valueOf(i));
                    rp.width = aveWidth * 3/4;
                    //设置他们的相对位置
                    if(i == 1)
                        rp.addRule(RelativeLayout.BELOW, empty.getId());
                    else
                        rp.addRule(RelativeLayout.BELOW, (i - 1) * 8);
                }
                else
                {
                    rp.addRule(RelativeLayout.RIGHT_OF, (i - 1) * 8  + j - 1);
                    rp.addRule(RelativeLayout.ALIGN_TOP, (i - 1) * 8  + j - 1);
                    tx.setText("");
                }

                tx.setLayoutParams(rp);
                course_table_layout.addView(tx);
            }
        }



        pDialog = new ProgressDialog(this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("正在加载信息。。。。");
        pDialog.setIndeterminate(true);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setCancelable(false);
        pDialog.show();


        //获取当前学期
        final String currentTerm = courseSettings.getString("currentTerm_" + UserInfo.currentUser.getStuNum(), null);
        //查看这个学期的课程信息是否已经抓取过
        final boolean hasSetting = courseSettings.getBoolean("Is_" + currentTerm + "_saved_" + UserInfo.currentUser.getStuNum(), false);
        /**
         这里写你自己的获取课程信息的方法
         */
        new Thread() {
            @Override
            public void run() {

                CourseInfoDBManager dbManager = new CourseInfoDBManager(CourseTableActivity.this);
                //打开数据库
                dbManager.open();

                try {
                    //入果还没抓取过该学期的课程信息，先抓取
                    if(!hasSetting)
                    {
                        //抓取这个学期的课表信息
                        List<CourseInfo> list = CourseInfoFetcher.fetchCourseInfo("", currentTerm, UserInfo.currentUser.getStuNum());
                        //插入课程信息
                        for(CourseInfo courseInfo : list)
                        {
                            dbManager.insertCourse(courseInfo, currentTerm);
                        }
                        //设置该学期的课程已经抓取过的标志
                        Editor editor = courseSettings.edit();
                        editor.putBoolean("Is_" + currentTerm + "_saved_" + UserInfo.currentUser.getStuNum(), true);
                        editor.commit();

                    }

                    //从数据库中读取课程信息，存放在courseInfoMap中，key为星期几，value是这一天的课程信息
                    courseInfoMap  = dbManager.query(currentTerm);
                    // 发送更新界面信息
                    Message msg = new Message();
                    if(courseInfoMap.isEmpty())
                    {
                        msg.what = -2;
                        courseInfoInitMessageHandler.sendMessage(msg);
                        return;
                    }
                    int currentWeek = -1;
                    if(!currentWeekStr.equals(""))
                    {
                        currentWeek = Integer.parseInt(currentWeekStr);
                    }
                    dbManager.close();
                    InitMessageObj msgObj = new InitMessageObj(aveWidth, currentWeek, screenWidth, maxCourseNum);
                    msg.obj = msgObj;
                    courseInfoInitMessageHandler.sendMessage(msg);

                } catch (AppException e) {
                    Message msg = new Message();
                    msg.what = -1;
                    courseInfoInitMessageHandler.sendMessage(msg);
                    Log.e("courseInfo_fetch_exception", e.toString());

                } finally {

                    dbManager.close();
                }
            }
        }.start();

    }

    CourseInfoInitMessageHandler courseInfoInitMessageHandler = new CourseInfoInitMessageHandler(this);

    static class InitMessageObj{

        int aveWidth;
        int currentWeek;
        int screenWidth;
        int maxCourseNum;
        public InitMessageObj(int aveWidth, int currentWeek, int screenWidth, int maxCourseNum) {
            super();
            this.aveWidth = aveWidth;
            this.currentWeek = currentWeek;
            this.screenWidth = screenWidth;
            this.maxCourseNum = maxCourseNum;
        }

    }
    //初始化课程表的messageHandler
    static class CourseInfoInitMessageHandler extends Handler{

        WeakReference<CourseTableActivity> mActivity;

        public CourseInfoInitMessageHandler(CourseTableActivity activity){

            mActivity = new WeakReference<CourseTableActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            mActivity.get().pDialog.dismiss();
            //网络错误
            if(msg.what == -1)
            {
                Toast.makeText(mActivity.get(), "获取课程信息失败！请检查您的网络或者稍后再试", Toast.LENGTH_SHORT).show();
                return;
            }
            //没有课程信息
            if(msg.what == -2)
            {
                Toast.makeText(mActivity.get(), "教务管理系统中无该学期的课程信息···", Toast.LENGTH_SHORT).show();
                return;
            }
            //五种颜色的背景
            int[] background = {R.drawable.course_info_blue, R.drawable.course_info_green,
                    R.drawable.course_info_red, R.drawable.course_info_red,
                    R.drawable.course_info_yellow};
            //获取课程信息的map
            Map<String, List<CourseInfo>> courseInfoMap = mActivity.get().courseInfoMap;
            //一些传过来的参数
            final InitMessageObj msgObj = (InitMessageObj) msg.obj;
            //当前周数
            int currentWeek = msgObj.currentWeek;
            //最大课程节数
            int maxCourseNum = msgObj.maxCourseNum;
            for(Map.Entry<String, List<CourseInfo>> entry: courseInfoMap.entrySet())
            {

                //查找出最顶层的课程信息（顶层课程信息即显示在最上层的课程，最顶层的课程信息满足两个条件 1、当前周数在该课程的周数范围内 2、该课程的节数跨度最大
                CourseInfo upperCourse = null;
                //list里保存的是一周内某 一天的课程
                final List<CourseInfo> list = new ArrayList<CourseInfo>(entry.getValue());
                //
                //按开始的时间（哪一节）进行排序
                Collections.sort(list, new Comparator<CourseInfo>(){
                    @Override
                    public int compare(CourseInfo arg0, CourseInfo arg1) {

                        if(arg0.getBeginIndex() < arg1.getBeginIndex())
                            return -1;
                        else
                            return 1;
                    }

                });
                int lastListSize;
                do {

                    lastListSize = list.size();
                    Iterator<CourseInfo> iter = list.iterator();
                    //先查找出第一个在周数范围内的课
                    while(iter.hasNext())
                    {
                        CourseInfo c = iter.next();//
                        if(((c.getBeginWeek() <= currentWeek && c.getEndWeek() >= currentWeek) || currentWeek == -1) && c.getEndIndex() <= maxCourseNum)
                        {
                            //判断是单周还是双周的课
                            if(c.getCourseType() == CourseInfo.ALL ||
                                    (c.getCourseType() == CourseInfo.EVEN && currentWeek % 2 == 0) ||
                                    (c.getCourseType() == CourseInfo.ODD && currentWeek % 2 != 0) )
                            {
                                //从list中移除该项，并设置这节课为顶层课
                                iter.remove();
                                upperCourse = c;
                                break;
                            }
                        }
                    }
                    if(upperCourse != null)
                    {
                        List<CourseInfo> courseInfoList = new ArrayList<CourseInfo>();
                        courseInfoList.add(upperCourse);
                        int index = 0;
                        iter = list.iterator();
                        //查找这一天有哪些课与刚刚查找出来的顶层课相交
                        while(iter.hasNext())
                        {
                            CourseInfo c = iter.next();
                            //先判断该课程与upperCourse是否相交，如果相交加入courseInfoList中
                            if((c.getBeginIndex() <= upperCourse.getBeginIndex()
                                    &&upperCourse.getBeginIndex() < c.getEndIndex())
                                    ||(upperCourse.getBeginIndex() <= c.getBeginIndex()
                                    && c.getBeginIndex() < upperCourse.getEndIndex()))
                            {
                                courseInfoList.add(c);
                                iter.remove();
                                //在判断哪个跨度大，跨度大的为顶层课程信息
                                if((c.getEndIndex() - c.getEndIndex()) > (upperCourse.getEndIndex() - upperCourse.getBeginIndex())
                                        && ((c.getBeginWeek() <= currentWeek && c.getEndWeek() >= currentWeek) || currentWeek == -1))
                                {
                                    upperCourse = c;
                                    index ++;
                                }

                            }

                        }
                        //记录顶层课程在courseInfoList中的索引位置
                        final int upperCourseIndex = index;
                        // 动态生成课程信息TextView
                        TextView courseInfo = new TextView(mActivity.get());
                        courseInfo.setId(1000 + upperCourse.getDay() * 100 + upperCourse.getBeginIndex() * 10 + upperCourse.getId());
                        int id = courseInfo.getId();
                        mActivity.get().textviewCourseInfoMap.put(id, courseInfoList);
                        courseInfo.setText(upperCourse.getCourseName() + "\n@" + upperCourse.getClassRoom());
                        //该textview的高度根据其节数的跨度来设置
                        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                                msgObj.aveWidth * 31 / 32,
                                (mActivity.get().gridHeight - 5) * 2 + (upperCourse.getEndIndex() - upperCourse.getBeginIndex() - 1) * mActivity.get().gridHeight);
                        //textview的位置由课程开始节数和上课的时间（day of week）确定
                        rlp.topMargin = 5 + (upperCourse.getBeginIndex() - 1) * mActivity.get().gridHeight;
                        rlp.leftMargin = 1;
                        // 前面生成格子时的ID就是根据Day来设置的
                        rlp.addRule(RelativeLayout.RIGHT_OF, upperCourse.getDay());
                        //字体居中中
                        courseInfo.setGravity(Gravity.CENTER);
                        //选择一个颜色背景
                        int colorIndex = ((upperCourse.getBeginIndex() - 1) * 8 + upperCourse.getDay()) % (background.length - 1);
                        courseInfo.setBackgroundResource(background[colorIndex]);
                        courseInfo.setTextSize(12);
                        courseInfo.setLayoutParams(rlp);
                        courseInfo.setTextColor(Color.WHITE);
                        //设置不透明度
                        courseInfo.getBackground().setAlpha(222);
                        // 设置监听事件
                        courseInfo.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                Log.i("text_view", String.valueOf(arg0.getId()));
                                Map<Integer, List<CourseInfo>> map = mActivity.get().textviewCourseInfoMap;
                                final List<CourseInfo> tempList = map.get(arg0.getId());
                                if(tempList.size() > 1)
                                {
                                    //如果有多个课程，则设置点击弹出gallery 3d 对话框
                                    LayoutInflater layoutInflater = (LayoutInflater) mActivity.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                    View galleryView = layoutInflater.inflate(R.layout.course_info_gallery_layout, null);
                                    final Dialog coursePopupDialog = new AlertDialog.Builder(mActivity.get()).create();
                                    coursePopupDialog.setCanceledOnTouchOutside(true);
                                    coursePopupDialog.setCancelable(true);
                                    coursePopupDialog.show();
                                    WindowManager.LayoutParams params = coursePopupDialog.getWindow().getAttributes();
                                    params.width = LayoutParams.FILL_PARENT;
                                    coursePopupDialog.getWindow().setAttributes(params);
                                    CourseInfoAdapter adapter = new CourseInfoAdapter(mActivity.get(), tempList, msgObj.screenWidth, msgObj.currentWeek);
                                    CourseInfoGallery gallery = (CourseInfoGallery) galleryView.findViewById(R.id.course_info_gallery);
                                    gallery.setSpacing(10);
                                    gallery.setAdapter(adapter);
                                    gallery.setSelection(upperCourseIndex);
                                    gallery.setOnItemClickListener(new OnItemClickListener() {
                                        @Override
                                        public void onItemClick(
                                                AdapterView<?> arg0, View arg1,
                                                int arg2, long arg3) {
                                            CourseInfo courseInfo = tempList.get(arg2);
                                            Intent intent = new Intent();
                                            Bundle mBundle = new Bundle();
                                            mBundle.putSerializable("courseInfo", courseInfo);
                                            intent.putExtras(mBundle);
                                            intent.setClass(mActivity.get(), DetailCourseInfoActivity.class);
                                            mActivity.get().startActivity(intent);
                                            coursePopupDialog.dismiss();
                                        }
                                    });
                                    coursePopupDialog.setContentView(galleryView);
                                }
                                else
                                {
                                    Intent intent = new Intent();
                                    Bundle mBundle = new Bundle();
                                    mBundle.putSerializable("courseInfo", tempList.get(0));
                                    intent.putExtras(mBundle);
                                    intent.setClass(mActivity.get(), DetailCourseInfoActivity.class);
                                    mActivity.get().startActivity(intent);
                                }
                            }

                        });
                        mActivity.get().course_table_layout.addView(courseInfo);
                        mActivity.get().courseTextViewList.add(courseInfo);
                        upperCourse = null;
                    }
                } while(list.size() < lastListSize && list.size() != 0);
            }
            super.handleMessage(msg);
        }

    }
    /**
     * 显示周数下拉列表悬浮窗
     * @param parent
     */
    private void showWeekListWindow(View parent){

        if(weekListWindow == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //获取layout
            popupWindowLayout = layoutInflater.inflate(R.layout.week_list_layout, null);
            weekListView = (ListView) popupWindowLayout.findViewById(R.id.week_list_view_body);
            //禁用滚动条（貌似没用··）
            weekListView.setVerticalScrollBarEnabled(false);
            List<Map<String, Object>> weekList = new ArrayList<Map<String, Object>>();
            //默认25周
            for(int i = 1; i <= 25; i ++)
            {
                Map<String, Object> rowData = new HashMap<String, Object>();
                rowData.put("week_index", "第" + i + "周");
                weekList.add(rowData);
            }

            //设置listview的adpter
            SimpleAdapter listAdapter = new SimpleAdapter(this,
                    weekList, R.layout.week_list_item_layout,
                    new String[]{"week_index"},
                    new int[]{R.id.week_list_item});
            weekListView.setAdapter(listAdapter);
            weekListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adpater, View arg1,
                                        int arg2, long arg3) {
                    int index = 0;
                    String indexStr = textTitle.getText().toString();
                    indexStr = indexStr.replaceAll("第", "").replaceAll("周", "");
                    if(!indexStr.equals("全部"))
                        index = Integer.parseInt(indexStr);
                    textTitle.setText("第" + (arg2 + 1) + "周");
                    weekListWindow.dismiss();
                    if((arg2 + 1) != index)
                    {
                        Log.i("courseTableActivity", "清空当前课程信息");
                        for(TextView tx : courseTextViewList)
                        {
                            course_table_layout.removeView(tx);
                        }
                        courseTextViewList.clear();
                        //重新设置课程信息
                        Message msg = new Message();
                        InitMessageObj msgObj = new InitMessageObj(aveWidth, arg2 + 1, screenWidth, maxCourseNum);
                        msg.obj = msgObj;
                        courseInfoInitMessageHandler.sendMessage(msg);
                    }
                }
            });
            int width = textTitle.getWidth();
            //实例化一个popupwindow
            weekListWindow = new PopupWindow(popupWindowLayout, width + 100, width + 120);

        }

        weekListWindow.setFocusable(true);
        //设置点击外部可消失
        weekListWindow.setOutsideTouchable(true);
        weekListWindow.setBackgroundDrawable(new BitmapDrawable());
        //消失的时候恢复按钮的背景（消除"按下去"的样式）
        weekListWindow.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                textTitle.setBackgroundDrawable(null);
            }
        });
        weekListWindow.showAsDropDown(parent, -50, 0);
    }

}
