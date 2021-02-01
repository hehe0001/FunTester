package com.fun.frame.execute;

import com.fun.base.constaint.FixedQpsThread;
import com.fun.base.constaint.ThreadBase;
import com.fun.base.constaint.ThreadLimitTimeCount;
import com.fun.base.constaint.ThreadLimitTimesCount;
import com.fun.base.exception.ParamException;
import com.fun.config.HttpClientConstant;
import com.fun.frame.SourceCode;
import com.fun.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于异步展示性能测试进度的多线程类
 */
public class Progress extends SourceCode implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(Progress.class);

    /**
     * 进度条的长度
     */
    private static final int LENGTH = 67;

    /**
     * 标志符号
     */
    private static final String ONE = getPart(3);

    /**
     * 总开关,是否运行,默认true
     */
    private boolean st = true;

    /**
     * 是否次数模型
     */
    public boolean isTimesMode;

    /**
     * 多线程任务基类对象,本类中不处理,只用来获取值,若使用的话请调用clone()方法
     */
    private ThreadBase base;

    /**
     * 限制条件
     */
    private int limit;

    /**
     * 非精确时间,误差可以忽略
     */
    private long startTime = Time.getTimeStamp();

    /**
     * 描述
     */
    private String taskDesc;

    public Progress(ThreadBase base, String desc) {
        this.base = base;
        this.taskDesc = desc;
        init();
    }

    /**
     * 初始化对象,对istimesMode和limit赋值
     */
    private void init() {
        if (base instanceof ThreadLimitTimeCount) {
            this.isTimesMode = false;
            this.limit = ((ThreadLimitTimeCount) base).time;
        } else if (base instanceof ThreadLimitTimesCount) {
            this.isTimesMode = true;
            this.limit = ((ThreadLimitTimesCount) base).times;
        } else if (base instanceof FixedQpsThread) {
            FixedQpsThread fix = (FixedQpsThread) base;
            this.isTimesMode = fix.isTimesMode;
            this.limit = fix.limit;
        } else {
            ParamException.fail("创建进度条对象失败!");
        }
    }

    @Override
    public void run() {
        int pro = 0;
        while (st) {
            sleep(HttpClientConstant.LOOP_INTERVAL);
            if (isTimesMode) {
                pro = (int) (base.executeNum * 1.0 / limit * LENGTH);
            } else {
                pro = (int) ((Time.getTimeStamp() - startTime) * 1.0 / limit * LENGTH);
            }
            if (pro >= LENGTH) break;
            logger.info("{}进度:{}  {}", taskDesc, getManyString(ONE, pro), getPercent(getPercent(LENGTH, pro)));
        }
    }

    /**
     * 关闭线程,防止死循环
     */
    public void stop() {
        st = false;
        logger.info("{}进度:{}  {}", taskDesc, getManyString(ONE, LENGTH), "100%");
    }


}
