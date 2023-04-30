package com.example.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Entity.Report;
import com.example.Mapper.ReportMapper;
import com.example.Service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {
    @Autowired
    ReportMapper reportMapper;
    private Report report;

    @Override
    //返回所有举报
    public List<Report> getAllReport()
    {
        return reportMapper.selectList(null);
    }

    @Override
    //通过举报的id查找举报
    public Report getReportById(Long id)
    {
        LambdaQueryWrapper<Report> lqw1=new LambdaQueryWrapper<Report>();
        lqw1.eq(Report::getId,id);
        Report report1=reportMapper.selectOne(lqw1);
        return report1;
    }

    //插入一个举报信息
    public int InsertReport(Report report)
    {
        return reportMapper.insert(report);
    }

}
