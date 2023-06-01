package com.example.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Entity.Report;
import com.example.Mapper.ReportMapper;
import com.example.Service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
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

    @Override
    //插入一个举报信息
    public int InsertReport(Report report)
    {
        return baseMapper.insert(report);
    }

    @Override
    public List<Report> findPageByStatus(Integer pageNo,Integer pageSize,int status)
    {
        //PageHelper.startPage(pageNo,pageSize);
        LambdaQueryWrapper<Report> lqw1=new LambdaQueryWrapper<Report>();
        lqw1.eq(Report::getStatus,status);
        return reportMapper.selectList(lqw1);
    }

    @Override
    public List<Report> findPageByUserId(Integer pageNo,Integer pageSize,Long user_id)
    {
        //PageHelper.startPage(pageNo,pageSize);
        LambdaQueryWrapper<Report> lqw1=new LambdaQueryWrapper<Report>();
        lqw1.eq(Report::getReporter_id,user_id);
        return reportMapper.selectList(lqw1);
    }

    @Override
    public Map<String,Object> ReportMap(Report report)
    {
        Map<String,Object> map=new HashMap<>();
        map.put("id",report.getId());
        map.put("reported_id",report.getReported_id());
        map.put("report_order",report.getReport_order());
        map.put("reporter_id",report.getReported_id());
        map.put("status",report.getStatus());
        map.put("content",report.getContent());
        return map;
    }

    @Override
    public List<Report> getByType(int type)
    {
        LambdaQueryWrapper<Report> lqw1=new LambdaQueryWrapper<Report>();
        lqw1.eq(Report::getType,type);
        return reportMapper.selectList(lqw1);
    }

}
