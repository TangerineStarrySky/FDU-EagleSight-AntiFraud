package com.example.smsdetection;

import android.graphics.Color;
import android.os.Bundle;

import com.example.smsdetection.database.SmsDBHelper;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    private SmsDBHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mDBHelper = SmsDBHelper.getInstance(this);
        List<Long> categories = mDBHelper.queryCategories();

        // 诈骗与普通信息类型占比饼图
        PieChart pieChart = findViewById(R.id.pie_chart);

        // 创建数据项（假设诈骗信息有15条，普通信息有25条）
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(categories.get(1), "诈骗")); // 诈骗 15 条
        entries.add(new PieEntry(categories.get(0), "普通")); // 普通 25 条

        // 创建数据集
        PieDataSet dataSet = new PieDataSet(entries, "短信类型");
        dataSet.setColors(Color.RED, Color.GREEN); // 设置不同类型的颜色
        dataSet.setValueTextColor(Color.WHITE); // 设置文本颜色为白色
        dataSet.setValueTextSize(20f); // 设置文本大小

        // 将数据设置到 PieChart 上
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate(); // 刷新图表

        // 配置饼图样式
        pieChart.setUsePercentValues(true);  // 显示百分比
        pieChart.getDescription().setEnabled(false); // 禁用描述文本
        pieChart.setDrawHoleEnabled(true); // 设置饼图中间为圆孔
        pieChart.setHoleColor(Color.TRANSPARENT); // 圆孔颜色设置为透明
        pieChart.setTransparentCircleColor(Color.TRANSPARENT); // 内部透明圆的颜色
        pieChart.setTransparentCircleAlpha(20); // 内部透明圆的透明度
        pieChart.setCenterText("短信类型"); // 饼图中心的文本
        pieChart.setCenterTextSize(20f);

        pieChart.getLegend().setEnabled(false);
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f%%", value); // 保留一位小数
            }
        };
        dataSet.setValueFormatter(formatter);

        // 设置扇区间距
        dataSet.setSliceSpace(6f); // 扇区之间的间距
        dataSet.setSelectionShift(5f); // 点击扇区时的偏移量

        // 添加动画效果
        pieChart.animateY(2000, Easing.EaseInOutCubic);
        pieChart.animateX(2000, Easing.EaseInOutCubic);

        // 诈骗信息按发信人分类的柱状图
        BarChart barChartSender = findViewById(R.id.bar_chart_sender);

        // 将 Map 转换为 List 并按值降序排序
        Map<String, Integer> senderDeceiveCountMap = mDBHelper.DeceiveSmsDividedBySenders();
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(senderDeceiveCountMap.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // 截取前十个元素
        int limit = Math.min(10, sortedEntries.size());
        List<Map.Entry<String, Integer>> topTenEntries = sortedEntries.subList(0, limit);

        // 创建 BarEntry 列表
        ArrayList<BarEntry> senderEntries = new ArrayList<>();
        ArrayList<String> senderLabels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Integer> entry : topTenEntries) {
            senderEntries.add(new BarEntry(index, entry.getValue()));
            senderLabels.add(entry.getKey()); // 保存发信人标签
            index++;
        }

        // 创建数据集
        BarDataSet senderDataSet = new BarDataSet(senderEntries, "诈骗短信来源分析（Top 10）");
        senderDataSet.setColors(Color.RED, Color.YELLOW, Color.BLUE, Color.GREEN, Color.CYAN, Color.MAGENTA, Color.LTGRAY, Color.DKGRAY, Color.GRAY, Color.WHITE);  // 使用不同颜色代表不同发信人
        senderDataSet.setValueTextColor(Color.WHITE);  // 设置值的文字颜色
        senderDataSet.setValueTextSize(15f);
        senderDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // 返回整数值
            }
        });

        // 将数据设置到 BarChart 上
        BarData senderBarData = new BarData(senderDataSet);
        barChartSender.setData(senderBarData);
        barChartSender.invalidate(); // 刷新图表

        // 设置 X 轴标签
        XAxis xAxis = barChartSender.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return senderLabels.get((int) value); // 返回对应的发信人标签
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // 最小轴步长为1
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelRotationAngle(-80); // 设置 X 轴标签倾斜角度为 -45 度
        xAxis.setTextSize(12f);
        xAxis.setLabelCount(senderLabels.size(), true); // 设置 X 轴标签数量，确保所有标签都显示
//        xAxis.setAvoidFirstLastClipping(true); // 避免首尾标签被裁剪

        // 设置 Y 轴为整数
        YAxis yAxis = barChartSender.getAxisLeft();
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // 返回整数值
            }
        });
        yAxis.setGranularity(1f); // 最小轴步长为1
        yAxis.setGranularityEnabled(true);
        yAxis.setTextSize(16f);
        yAxis.setAxisMinimum(0f); // 设置 Y 轴的最小值为 0

        // 隐藏右侧的 Y 轴
        YAxis yAxisRight = barChartSender.getAxisRight();
        yAxisRight.setEnabled(false);

        // 设置动画效果
        barChartSender.animateY(2000, Easing.EaseInOutCubic); // Y轴方向的动画

        // 配置柱状图样式
        barChartSender.getLegend().setEnabled(false);
        barChartSender.setDescription(null); // 禁用描述文本
        barChartSender.setDrawGridBackground(false); // 不显示网格
        barChartSender.setDrawBarShadow(false); // 不显示阴影
        barChartSender.setDrawValueAboveBar(true); // 在柱状图上方显示值
        barChartSender.setFitBars(true); // 让柱状图适应屏幕宽度
        barChartSender.setPinchZoom(false); // 禁用缩放
        barChartSender.setDrawBorders(false); // 不显示边框
    }
}