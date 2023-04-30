package com.example.Service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.Entity.Report;

import java.util.List;

public interface ReportService  {
    //返回所有举报
    public List<Report> getAllReport();
    //通过举报的id查找举报
    public Report getReportById(Long id);
    //插入一个举报信息
    public int InsertReport(Report report);
}
