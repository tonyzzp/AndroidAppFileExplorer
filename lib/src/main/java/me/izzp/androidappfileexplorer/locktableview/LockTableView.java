package me.izzp.androidappfileexplorer.locktableview;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.izzp.androidappfileexplorer.R;

/**
 * 说明 可锁定首行和首列的表格视图
 * 作者 郭翰林
 * 创建时间 2017/3/29.
 * <p>
 *
 * <a href="https://github.com/RmondJone/LockTableView">源码来自 RmondJone/LockTableView </a>
 */

public class LockTableView {
    /**
     * 上下文
     */
    private Context mContext;
    /**
     * 表格父视图
     */
    private ViewGroup mContentView;
    /**
     * 表格数据，每一行为一条数据，从表头计算
     */
    private ArrayList<ArrayList<String>> mTableDatas = new ArrayList<ArrayList<String>>();
    /**
     * 表格视图
     */
    private View mTableView;
    /**
     * 是否锁定首行
     */
    private boolean isLockFristRow = true;
    /**
     * 是否锁定首列
     */
    private boolean isLockFristColumn = true;
    /**
     * 最大列宽(dp)
     */
    private int maxColumnWidth;
    /**
     * 最小列宽(dp)
     */
    private int minColumnWidth;
    /**
     * 最大行高(dp)
     */
    private int maxRowHeight;
    /**
     * 最小行高dp)
     */
    private int minRowHeight;
    /**
     * 第一行背景颜色
     */
    private int mFristRowBackGroudColor;
    /**
     * 数据为空时的缺省值
     */
    private String mNullableString;
    /**
     * 单元格字体大小
     */
    private int mTextViewSize;
    /**
     * 表格头部字体颜色
     */
    private int mTableHeadTextColor;
    /**
     * 表格内容字体颜色
     */
    private int mTableContentTextColor;
    /**
     * 表格监听事件
     */
    private OnTableViewListener mTableViewListener;


    //表格数据
    /**
     * 表格第一行数据,不包括第一个元素
     */
    private ArrayList<String> mTableFristData = new ArrayList<>();
    /**
     * 表格第一列数据，不包括第一个元素
     */
    private ArrayList<String> mTableColumnDatas = new ArrayList<>();
    /**
     * 表格左上角数据
     */
    private String mColumnTitle;
    /**
     * 表格每一行数据，不包括第一行和第一列
     */
    private ArrayList<ArrayList<String>> mTableRowDatas = new ArrayList<ArrayList<String>>();
    /**
     * 记录每列最大宽度
     */
    private ArrayList<Integer> mColumnMaxWidths = new ArrayList<Integer>();
    /**
     * 记录每行最大高度
     */
    private ArrayList<Integer> mRowMaxHeights = new ArrayList<Integer>();
    /**
     * 把所有的滚动视图放图列表，后面实现联动效果
     */
    private ArrayList<HorizontalScrollView> mScrollViews = new ArrayList<HorizontalScrollView>();


    //表格视图
    /**
     * 表格左上角视图
     */
    private TextView mColumnTitleView;
    /**
     * 第一行布局（锁状态）
     */
    private LinearLayout mLockHeadView;
    /**
     * 第一行布局（未锁状态）
     */
    private LinearLayout mUnLockHeadView;
    /**
     * 第一行滚动视图（锁状态）
     */
    private CustomHorizontalScrollView mLockScrollView;
    /**
     * 第一行滚动视图（未锁状态）
     */
    private CustomHorizontalScrollView mUnLockScrollView;
    /**
     * 表格主视图
     */
    private ScrollView mTableScrollView;

    private View.OnClickListener onTextClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mTableViewListener != null) {
                mTableViewListener.onItemClick((TextView) v);
            }
        }
    };


    /**
     * 构造方法
     *
     * @param mContext     上下文
     * @param mContentView 表格父视图
     * @param mTableDatas  表格数据
     */
    public LockTableView(Context mContext, ViewGroup mContentView, ArrayList<ArrayList<String>> mTableDatas) {
        this.mContext = mContext;
        this.mContentView = mContentView;
        this.mTableDatas = mTableDatas;
        initAttrs();
    }

    /**
     * 初始化属性
     */
    private void initAttrs() {
        mTableView = LayoutInflater.from(mContext).inflate(R.layout.afe_locktableview, null);
        maxColumnWidth = 100;
        minColumnWidth = 70;
        minRowHeight = 20;
        maxRowHeight = 60;
        mNullableString = "N/A";
        mTableHeadTextColor = R.color.afe_bg;
        mTableContentTextColor = R.color.afe_border_color;
        mFristRowBackGroudColor = R.color.afe_table_head;
        mTextViewSize = 16;
    }

    /**
     * 展现视图
     */
    public void show() {
        initData();
        initView();
        mContentView.removeAllViews();//清空视图
        mContentView.addView(mTableView);
    }


    /**
     * 初始化表格数据
     */
    private void initData() {
        if (mTableDatas != null && mTableDatas.size() > 0) {
            //检查数据，如果有一行数据长度不一致，以最长为标准填"N/A"字符串,如果有null也替换
            int maxLength = 0;
            for (int i = 0; i < mTableDatas.size(); i++) {
                if (mTableDatas.get(i).size() >= maxLength) {
                    maxLength = mTableDatas.get(i).size();
                }
                ArrayList<String> rowDatas = mTableDatas.get(i);
                for (int j = 0; j < rowDatas.size(); j++) {
                    if (rowDatas.get(j) == null || rowDatas.get(j).equals("")) {
                        rowDatas.set(j, mNullableString);
                    }
                }
                mTableDatas.set(i, rowDatas);
            }
//            Log.e("每行最多个数",maxLength+"");
            for (int i = 0; i < mTableDatas.size(); i++) {
                ArrayList<String> rowDatas = mTableDatas.get(i);
                if (rowDatas.size() < maxLength) {
                    int size = maxLength - rowDatas.size();
                    for (int j = 0; j < size; j++) {
                        rowDatas.add(mNullableString);
                    }
                    mTableDatas.set(i, rowDatas);
                }
            }

//            //测试
//            for (int i=0;i<mTableDatas.size();i++){
//                ArrayList<String> rowDatas=mTableDatas.get(i);
//                StringBuffer b=new StringBuffer();
//                for (String str:rowDatas){
//                    b.append("["+str+"]");
//                }
//                Log.e("第"+i+"行数据",b.toString()+"/"+rowDatas.size()+"个");
//            }
            //初始化每列最大宽度
            for (int i = 0; i < mTableDatas.size(); i++) {
                ArrayList<String> rowDatas = mTableDatas.get(i);
                StringBuffer buffer = new StringBuffer();
                for (int j = 0; j < rowDatas.size(); j++) {
                    TextView textView = new TextView(mContext);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextViewSize);
                    textView.setText(rowDatas.get(j));
                    textView.setGravity(Gravity.CENTER);
                    //设置布局
                    LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    textViewParams.setMargins(45, 45, 45, 45);//android:layout_margin="15dp"
                    textView.setLayoutParams(textViewParams);
                    if (i == 0) {
                        mColumnMaxWidths.add(measureTextWidth(textView, rowDatas.get(j)));
                        buffer.append("[" + measureTextWidth(textView, rowDatas.get(j)) + "]");
                    } else {
                        int length = mColumnMaxWidths.get(j);
                        int current = measureTextWidth(textView, rowDatas.get(j));
                        if (current > length) {
                            mColumnMaxWidths.set(j, current);
                        }
                        buffer.append("[" + measureTextWidth(textView, rowDatas.get(j)) + "]");
                    }
                }
//                Log.e("第"+i+"行列最大宽度",buffer.toString());
            }
//            Log.e("每列最大宽度dp:",mColumnMaxWidths.toString());


            //初始化每行最大高度
            for (int i = 0; i < mTableDatas.size(); i++) {
                ArrayList<String> rowDatas = mTableDatas.get(i);
                StringBuffer buffer = new StringBuffer();

                TextView textView = new TextView(mContext);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextViewSize);
                textView.setGravity(Gravity.CENTER);
                //设置布局
                LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                textViewParams.setMargins(45, 45, 45, 45);//android:layout_margin="15dp"
                textView.setLayoutParams(textViewParams);
                int maxHeight = measureTextHeight(textView, rowDatas.get(0));
                mRowMaxHeights.add(maxHeight);
                for (int j = 0; j < rowDatas.size(); j++) {
                    int currentHeight = measureTextHeight(textView, rowDatas.get(j));
                    buffer.append("[" + currentHeight + "]");
                    if (currentHeight > maxHeight) {
                        mRowMaxHeights.set(i, currentHeight);
                    }
                }
//                Log.e("第"+i+"行高度",buffer.toString());
            }
//            Log.e("每行最大高度dp:",mRowMaxHeights.toString());

            if (isLockFristRow) {
                ArrayList<String> fristRowDatas = mTableDatas.get(0);
                if (isLockFristColumn) {
                    //锁定第一列
                    mColumnTitle = fristRowDatas.get(0);
                    fristRowDatas.remove(0);
                    mTableFristData.addAll(fristRowDatas);
                    //构造第一列数据,并且构造表格每行数据
                    for (int i = 1; i < mTableDatas.size(); i++) {
                        ArrayList<String> rowDatas = mTableDatas.get(i);
                        mTableColumnDatas.add(rowDatas.get(0));
                        rowDatas.remove(0);
                        mTableRowDatas.add(rowDatas);
                    }
                } else {
                    mTableFristData.addAll(fristRowDatas);
                    for (int i = 1; i < mTableDatas.size(); i++) {
                        mTableRowDatas.add(mTableDatas.get(i));
                    }
                }
            } else {
                if (isLockFristColumn) {
                    //锁定第一列
                    //构造第一列数据,并且构造表格每行数据
                    for (int i = 0; i < mTableDatas.size(); i++) {
                        ArrayList<String> rowDatas = mTableDatas.get(i);
                        mTableColumnDatas.add(rowDatas.get(0));
                        rowDatas.remove(0);
                        mTableRowDatas.add(rowDatas);
                    }
                } else {
                    for (int i = 0; i < mTableDatas.size(); i++) {
                        mTableRowDatas.add(mTableDatas.get(i));
                    }
                }
            }

//            Log.e("第一行数据",mTableFristData.toString());
//            Log.e("第一列数据",mTableColumnDatas.toString());
//            Log.e("每行数据",mTableRowDatas.toString());
        } else {
            Toast.makeText(mContext, "表格数据为空！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 初始化表格视图
     */
    private void initView() {
        mColumnTitleView = (TextView) mTableView.findViewById(R.id.afe_lockHeadView_Text);
        mLockHeadView = (LinearLayout) mTableView.findViewById(R.id.afe_lockHeadView);
        mUnLockHeadView = (LinearLayout) mTableView.findViewById(R.id.afe_unLockHeadView);
        mLockScrollView = (CustomHorizontalScrollView) mTableView.findViewById(R.id.afe_lockHeadView_ScrollView);
        mUnLockScrollView = (CustomHorizontalScrollView) mTableView.findViewById(R.id.afe_unlockHeadView_ScrollView);
        mTableScrollView = (ScrollView) mTableView.findViewById(R.id.afe_table_scrollView);
        mLockHeadView.setBackgroundColor(ContextCompat.getColor(mContext, mFristRowBackGroudColor));
        mUnLockHeadView.setBackgroundColor(ContextCompat.getColor(mContext, mFristRowBackGroudColor));
        if (isLockFristRow) {
            if (isLockFristColumn) {
                mLockHeadView.setVisibility(View.VISIBLE);
                mUnLockHeadView.setVisibility(View.GONE);
            } else {
                mLockHeadView.setVisibility(View.GONE);
                mUnLockHeadView.setVisibility(View.VISIBLE);
            }
            creatHeadView();
        } else {
            mLockHeadView.setVisibility(View.GONE);
            mUnLockHeadView.setVisibility(View.GONE);
        }
        createTableView();
    }

    /**
     * 创建头部视图
     */
    private void creatHeadView() {
        if (isLockFristColumn) {
            mColumnTitleView.setTextColor(ContextCompat.getColor(mContext, mTableHeadTextColor));
            mColumnTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextViewSize);
            mColumnTitleView.setText(mColumnTitle);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mColumnTitleView.getLayoutParams();
            layoutParams.width = DisplayUtil.dip2px(mContext, mColumnMaxWidths.get(0));
            layoutParams.height = DisplayUtil.dip2px(mContext, mRowMaxHeights.get(0));
            layoutParams.setMargins(45, 45, 45, 45);
            mColumnTitleView.setLayoutParams(layoutParams);
            //构造滚动视图
            createScollview(mLockScrollView, mTableFristData, true);
            mScrollViews.add(mLockScrollView);
            mLockScrollView.setOnScrollChangeListener(new CustomHorizontalScrollView.onScrollChangeListener() {
                @Override
                public void onScrollChanged(HorizontalScrollView scrollView, int x, int y) {
                    changeAllScrollView(x, y);
                }
            });
        } else {
            createScollview(mUnLockScrollView, mTableFristData, true);
            mScrollViews.add(mUnLockScrollView);
            mUnLockScrollView.setOnScrollChangeListener(new CustomHorizontalScrollView.onScrollChangeListener() {
                @Override
                public void onScrollChanged(HorizontalScrollView scrollView, int x, int y) {
                    changeAllScrollView(x, y);
                }
            });
        }
    }

    /**
     * 构造表格主视图
     */
    private void createTableView() {
        if (isLockFristColumn) {
            createLockColumnView();
        } else {
            createUnLockColumnView();
        }
    }

    /**
     * 创建锁定列视图
     */
    private void createLockColumnView() {
        View lockTableViewContent = LayoutInflater.from(mContext).inflate(R.layout.afe_locktablecontentview, null);
        LinearLayout lockViewParent = (LinearLayout) lockTableViewContent.findViewById(R.id.afe_lockView_parent);
        CustomHorizontalScrollView lockScrollViewParent = (CustomHorizontalScrollView) lockTableViewContent.findViewById(R.id.afe_lockScrollView_parent);
        //构造锁定视图
        for (int i = 0; i < mTableColumnDatas.size(); i++) {
            //构造TextView容器，如果要设置背景色设置这一个而不是设置TextView，TextView有外边距设置不全
            LinearLayout textViewContainer = new LinearLayout(mContext);
            LinearLayout.LayoutParams textLinearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            textViewContainer.setOrientation(LinearLayout.HORIZONTAL);
            textViewContainer.setLayoutParams(textLinearParams);
            //构造TextView
            TextView textView = new TextView(mContext);
            //设置布局
            LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            textViewParams.setMargins(45, 45, 45, 45);
            textView.setLayoutParams(textViewParams);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextViewSize);
            textView.setText(mTableColumnDatas.get(i));
            textView.setGravity(Gravity.CENTER);
            ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();
            layoutParams.width = DisplayUtil.dip2px(mContext, mColumnMaxWidths.get(0));
            if (isLockFristRow) {
                layoutParams.height = DisplayUtil.dip2px(mContext, mRowMaxHeights.get(i + 1));
            } else {
                layoutParams.height = DisplayUtil.dip2px(mContext, mRowMaxHeights.get(i));
            }
            textView.setLayoutParams(layoutParams);
            textView.setOnClickListener(onTextClickListener);
            if (!isLockFristRow) {
                if (i == 0) {
                    textViewContainer.setBackgroundColor(ContextCompat.getColor(mContext, mFristRowBackGroudColor));
                    textView.setTextColor(ContextCompat.getColor(mContext, mTableHeadTextColor));
                } else {
                    textView.setTextColor(ContextCompat.getColor(mContext, mTableContentTextColor));
                }
            } else {
                textView.setTextColor(ContextCompat.getColor(mContext, mTableContentTextColor));
            }
            textViewContainer.addView(textView);
            //表格线
            View splite = new View(mContext);
            ViewGroup.LayoutParams spliteLayoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    DisplayUtil.dip2px(mContext, 1));
            splite.setLayoutParams(spliteLayoutParam);
            splite.setBackgroundColor(ContextCompat.getColor(mContext, R.color.afe_light_gray));
            lockViewParent.addView(textViewContainer);
            lockViewParent.addView(splite);
        }
        //构造滚动视图
        LinearLayout scollViewItemContentView = new LinearLayout(mContext);
        LinearLayout.LayoutParams scollViewItemContentViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        scollViewItemContentView.setLayoutParams(scollViewItemContentViewParams);
        scollViewItemContentView.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < mTableRowDatas.size(); i++) {
            ArrayList<String> datas = mTableRowDatas.get(i);
            //设置LinearLayout
            LinearLayout linearLayout = new LinearLayout(mContext);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            if (!isLockFristRow) {
                if (i == 0) {
                    linearLayout.setBackgroundColor(ContextCompat.getColor(mContext, mFristRowBackGroudColor));
                }
            }
            for (int j = 0; j < datas.size(); j++) {
                //构造单元格
                TextView textView = new TextView(mContext);
                if (!isLockFristRow) {
                    if (i == 0) {
                        textView.setTextColor(ContextCompat.getColor(mContext, mTableHeadTextColor));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(mContext, mTableContentTextColor));
                    }
                } else {
                    textView.setTextColor(ContextCompat.getColor(mContext, mTableContentTextColor));
                }
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextViewSize);
                textView.setGravity(Gravity.CENTER);
                textView.setText(datas.get(j));
                textView.setOnClickListener(onTextClickListener);
                //设置布局
                LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                textViewParams.setMargins(45, 45, 45, 45);
                if (isLockFristRow) {
                    textViewParams.height = DisplayUtil.dip2px(mContext, mRowMaxHeights.get(i + 1));
                } else {
                    textViewParams.height = DisplayUtil.dip2px(mContext, mRowMaxHeights.get(i));
                }
                textView.setLayoutParams(textViewParams);
                ViewGroup.LayoutParams textViewParamsCopy = textView.getLayoutParams();
                textViewParamsCopy.width = DisplayUtil.dip2px(mContext, mColumnMaxWidths.get(j + 1));
                linearLayout.addView(textView);
                //右侧分隔线
                if (j != datas.size() - 1) {
                    View splitView = new View(mContext);
                    ViewGroup.LayoutParams splitViewParmas = new ViewGroup.LayoutParams(DisplayUtil.dip2px(mContext, 1),
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    splitView.setLayoutParams(splitViewParmas);
                    if (!isLockFristRow) {
                        if (i == 0) {
                            splitView.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white));
                        } else {
                            splitView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.afe_light_gray));
                        }
                    } else {
                        splitView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.afe_light_gray));
                    }
                    linearLayout.addView(splitView);
                }
            }
            scollViewItemContentView.addView(linearLayout);
            //底部分隔线
            View splite = new View(mContext);
            ViewGroup.LayoutParams spliteLayoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    DisplayUtil.dip2px(mContext, 1));
            splite.setLayoutParams(spliteLayoutParam);
            splite.setBackgroundColor(ContextCompat.getColor(mContext, R.color.afe_light_gray));
            scollViewItemContentView.addView(splite);
        }
        lockScrollViewParent.addView(scollViewItemContentView);
        mScrollViews.add(lockScrollViewParent);
        lockScrollViewParent.setOnScrollChangeListener(new CustomHorizontalScrollView.onScrollChangeListener() {
            @Override
            public void onScrollChanged(HorizontalScrollView scrollView, int x, int y) {
                changeAllScrollView(x, y);
            }
        });
        mTableScrollView.addView(lockTableViewContent);
    }

    /**
     * 创建不锁定列视图
     */
    private void createUnLockColumnView() {
        View lockTableViewContent = LayoutInflater.from(mContext).inflate(R.layout.afe_customscrollview, null);
        CustomHorizontalScrollView lockScrollViewParent = (CustomHorizontalScrollView) lockTableViewContent.findViewById(R.id.afe_unlockScrollView_parent);
        //构造滚动视图
        LinearLayout scollViewItemContentView = new LinearLayout(mContext);
        LinearLayout.LayoutParams scollViewItemContentViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        scollViewItemContentView.setLayoutParams(scollViewItemContentViewParams);
        scollViewItemContentView.setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < mTableRowDatas.size(); i++) {
            ArrayList<String> datas = mTableRowDatas.get(i);
            //设置LinearLayout
            LinearLayout linearLayout = new LinearLayout(mContext);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            if (!isLockFristRow) {
                if (i == 0) {
                    linearLayout.setBackgroundColor(ContextCompat.getColor(mContext, mFristRowBackGroudColor));
                }
            }
            for (int j = 0; j < datas.size(); j++) {
                //构造单元格
                TextView textView = new TextView(mContext);
                if (!isLockFristRow) {
                    if (i == 0) {
                        textView.setTextColor(ContextCompat.getColor(mContext, mTableHeadTextColor));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(mContext, mTableContentTextColor));
                    }
                } else {
                    textView.setTextColor(ContextCompat.getColor(mContext, mTableContentTextColor));
                }
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextViewSize);
                textView.setGravity(Gravity.CENTER);
                textView.setText(datas.get(j));
                //设置布局
                LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                textViewParams.setMargins(45, 45, 45, 45);
                if (isLockFristRow) {
                    textViewParams.height = DisplayUtil.dip2px(mContext, mRowMaxHeights.get(i + 1));
                } else {
                    textViewParams.height = DisplayUtil.dip2px(mContext, mRowMaxHeights.get(i));
                }
                textView.setLayoutParams(textViewParams);
                textView.setOnClickListener(onTextClickListener);
                ViewGroup.LayoutParams textViewParamsCopy = textView.getLayoutParams();
                textViewParamsCopy.width = DisplayUtil.dip2px(mContext, mColumnMaxWidths.get(j));
                linearLayout.addView(textView);
                //右侧分隔线
                if (j != datas.size() - 1) {
                    View splitView = new View(mContext);
                    ViewGroup.LayoutParams splitViewParmas = new ViewGroup.LayoutParams(DisplayUtil.dip2px(mContext, 1),
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    splitView.setLayoutParams(splitViewParmas);
                    if (!isLockFristRow) {
                        if (i == 0) {
                            splitView.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white));
                        } else {
                            splitView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.afe_light_gray));
                        }
                    } else {
                        splitView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.afe_light_gray));
                    }
                    linearLayout.addView(splitView);
                }
            }
            scollViewItemContentView.addView(linearLayout);
            //底部分隔线
            View splite = new View(mContext);
            ViewGroup.LayoutParams spliteLayoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    DisplayUtil.dip2px(mContext, 1));
            splite.setLayoutParams(spliteLayoutParam);
            splite.setBackgroundColor(ContextCompat.getColor(mContext, R.color.afe_light_gray));
            scollViewItemContentView.addView(splite);
        }
        lockScrollViewParent.addView(scollViewItemContentView);
        mScrollViews.add(lockScrollViewParent);
        lockScrollViewParent.setOnScrollChangeListener(new CustomHorizontalScrollView.onScrollChangeListener() {
            @Override
            public void onScrollChanged(HorizontalScrollView scrollView, int x, int y) {
                changeAllScrollView(x, y);
            }
        });
        mTableScrollView.addView(lockTableViewContent);
    }

    /**
     * 改变所有滚动视图位置
     *
     * @param x
     * @param y
     */
    private void changeAllScrollView(int x, int y) {
        if (mScrollViews.size() > 0) {
            if (mTableViewListener != null) {
                mTableViewListener.onTableViewScrollChange(x, y);
            }
            for (int i = 0; i < mScrollViews.size(); i++) {
                HorizontalScrollView scrollView = mScrollViews.get(i);
                scrollView.scrollTo(x, y);
            }
        }
    }

    /**
     * 根据最大最小值，计算TextView的宽度
     *
     * @param textView
     * @param text
     * @return
     */
    private int measureTextWidth(TextView textView, String text) {
        if (textView != null) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
            int width = DisplayUtil.px2dip(mContext, layoutParams.leftMargin) +
                    DisplayUtil.px2dip(mContext, layoutParams.rightMargin) +
                    getTextViewWidth(textView, text);
            if (width <= minColumnWidth) {
                return minColumnWidth;
            } else if (width > minColumnWidth && width <= maxColumnWidth) {
                return width;
            } else {
                return maxColumnWidth;
            }
        }
        return 0;
    }

    /**
     * 计算TextView高度
     *
     * @param textView
     * @param text
     * @return
     */
    private int measureTextHeight(TextView textView, String text) {
        if (textView != null) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
            int height = getTextViewHeight(textView, text);
            if (height < minRowHeight) {
                return minRowHeight;
            } else if (height > minRowHeight && height < maxRowHeight) {
                return height;
            } else {
                return maxRowHeight;
            }
        }
        return 0;
    }

    /**
     * 根据文字计算textview的高度
     *
     * @param textView
     * @param text
     * @return
     */
    private int getTextViewHeight(TextView textView, String text) {
        if (textView != null) {
            int width = measureTextWidth(textView, text);
            TextPaint textPaint = textView.getPaint();
            StaticLayout staticLayout = new StaticLayout(text, textPaint, DisplayUtil.dip2px(mContext, width), Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
            int height = DisplayUtil.px2dip(mContext, staticLayout.getHeight());
            return height;
        }
        return 0;
    }

    /**
     * 根据文字计算textview的高度
     *
     * @param view
     * @param text
     * @return
     */
    private int getTextViewWidth(TextView view, String text) {
        if (view != null) {
            TextPaint paint = view.getPaint();
            return DisplayUtil.px2dip(mContext, (int) paint.measureText(text));
        }
        return 0;
    }


    /**
     * 构造滚动视图
     *
     * @param scrollView
     * @param datas
     * @param isFristRow 是否是第一行
     */
    private void createScollview(HorizontalScrollView scrollView, List<String> datas, boolean isFristRow) {
        //设置LinearLayout
        LinearLayout linearLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < datas.size(); i++) {
            //构造单元格
            TextView textView = new TextView(mContext);
            if (isFristRow) {
                textView.setTextColor(ContextCompat.getColor(mContext, mTableHeadTextColor));
            } else {
                textView.setTextColor(ContextCompat.getColor(mContext, mTableContentTextColor));
            }
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextViewSize);
            textView.setGravity(Gravity.CENTER);
            textView.setText(datas.get(i));
            //设置布局
            LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            textViewParams.setMargins(45, 45, 45, 45);
            textView.setLayoutParams(textViewParams);
            ViewGroup.LayoutParams textViewParamsCopy = textView.getLayoutParams();
            if (isLockFristColumn) {
                textViewParamsCopy.width = DisplayUtil.dip2px(mContext, mColumnMaxWidths.get(i + 1));
            } else {
                textViewParamsCopy.width = DisplayUtil.dip2px(mContext, mColumnMaxWidths.get(i));
            }
            linearLayout.addView(textView);
            //画分隔线
            if (i != datas.size() - 1) {
                View splitView = new View(mContext);
                ViewGroup.LayoutParams splitViewParmas = new ViewGroup.LayoutParams(DisplayUtil.dip2px(mContext, 1),
                        ViewGroup.LayoutParams.MATCH_PARENT);
                splitView.setLayoutParams(splitViewParmas);
                if (isFristRow) {
                    splitView.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white));
                } else {
                    splitView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.afe_light_gray));
                }
                linearLayout.addView(splitView);
            }
        }
        scrollView.addView(linearLayout);
    }


    //属性设置
    public LockTableView setLockFristRow(boolean lockFristRow) {
        isLockFristRow = lockFristRow;
        return this;
    }

    public LockTableView setLockFristColumn(boolean lockFristColumn) {
        isLockFristColumn = lockFristColumn;
        return this;
    }

    public LockTableView setMaxColumnWidth(int maxColumnWidth) {
        this.maxColumnWidth = maxColumnWidth;
        return this;
    }

    public LockTableView setMinColumnWidth(int minColumnWidth) {
        this.minColumnWidth = minColumnWidth;
        return this;
    }

    public LockTableView setFristRowBackGroudColor(int mFristRowBackGroudColor) {
        this.mFristRowBackGroudColor = mFristRowBackGroudColor;
        return this;
    }

    public LockTableView setNullableString(String mNullableString) {
        this.mNullableString = mNullableString;
        return this;
    }

    public LockTableView setTextViewSize(int mTextViewSize) {
        this.mTextViewSize = mTextViewSize;
        return this;
    }

    public LockTableView setTableHeadTextColor(int mTableHeadTextColor) {
        this.mTableHeadTextColor = mTableHeadTextColor;
        return this;
    }

    public LockTableView setTableContentTextColor(int mTableContentTextColor) {
        this.mTableContentTextColor = mTableContentTextColor;
        return this;
    }

    public LockTableView setMaxRowHeight(int maxRowHeight) {
        this.maxRowHeight = maxRowHeight;
        return this;
    }

    public LockTableView setMinRowHeight(int minRowHeight) {
        this.minRowHeight = minRowHeight;
        return this;
    }

    public LockTableView setTableViewListener(OnTableViewListener mTableViewListener) {
        this.mTableViewListener = mTableViewListener;
        return this;
    }

    //值获取
    public ArrayList<Integer> getColumnMaxWidths() {
        return mColumnMaxWidths;
    }

    public ArrayList<Integer> getRowMaxHeights() {
        return mRowMaxHeights;
    }

    public LinearLayout getLockHeadView() {
        return mLockHeadView;
    }

    public LinearLayout getUnLockHeadView() {
        return mUnLockHeadView;
    }

    public ArrayList<HorizontalScrollView> getScrollViews() {
        return mScrollViews;
    }

    //回调监听
    public interface OnTableViewListener {
        /**
         * 滚动监听
         *
         * @param x
         * @param y
         */
        void onTableViewScrollChange(int x, int y);

        void onItemClick(TextView tv);
    }
}
